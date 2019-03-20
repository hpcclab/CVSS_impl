package Scheduler;

import ResourceManagement.MachineInterface;
import Streampkg.StreamGOP;
import TimeEstimatorpkg.TimeEstimator;
import TimeEstimatorpkg.retStat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Merger {

    private static HashMap<Streampkg.TaskRequest,StreamGOP> mergePending_tasklvl =new HashMap<Streampkg.TaskRequest,StreamGOP>();;
    private static HashMap<Streampkg.TaskRequest,StreamGOP> mergePending_operationlvl =new HashMap<Streampkg.TaskRequest,StreamGOP>();
    private static HashMap<Streampkg.TaskRequest,StreamGOP> mergePending_datalvl =new HashMap<Streampkg.TaskRequest,StreamGOP>();
    public static long merged_tasklvl_count =0;
    public static long probecounter=0;
    private miscTools.SortableList pendingqueue;
    private miscTools.SortableList Batchqueue;
    public static ArrayList<MachineInterface> machineInterfaces;
    public Merger(miscTools.SortableList bq,miscTools.SortableList pq,ArrayList<MachineInterface> vi) {
        Batchqueue=bq;
        pendingqueue=pq;
        machineInterfaces =vi;
    }

    //binary search version, only work if original miss ==0
    public int Bsearch_trybetweenPositions(Streampkg.TaskRequest key,HashMap<Streampkg.TaskRequest,StreamGOP> thislvlmap,StreamGOP X,StreamGOP original,StreamGOP merged,int originalmiss) {
        miscTools.SortableList newVQ = new miscTools.SortableList(Batchqueue);
        int firstpos=0;
        newVQ.remove(original);

        int newfirstpos=firstpos,searchlimit=newVQ.size()-1;
        while(newfirstpos<searchlimit-1){
            int tryposition=(searchlimit+newfirstpos)/2;
            newVQ.add(tryposition,merged);
            int test=countDLMiss( newVQ,merged,Integer.MAX_VALUE,GOPTaskScheduler_mergable.SDco);
            if(Math.abs(test)<originalmiss){
                System.out.println("found a position");
                return tryposition; //found perfect condition
            }
            if(test<-1){
                return -1; //both merged one and other have miss their deadline, no perfect exist
            }
            if(test==-1){
                newfirstpos=tryposition; //x need to move forward
            }else{
                searchlimit=tryposition; // x need to move backward
            }
            newVQ.remove(tryposition); //it fail, so remove this
        }
        return -1;
    }
    public int linearsearch_trybetweenPositions(Streampkg.TaskRequest key,HashMap<Streampkg.TaskRequest,StreamGOP> thislvlmap,StreamGOP X,StreamGOP original,StreamGOP merged,int originalmiss) {
        miscTools.SortableList newVQ = new miscTools.SortableList(Batchqueue);
        //put merged to latest position (at the moment)
        newVQ.remove(original);
        //get latest position for X not to miss their deadline
        int latestpos=findlatestposition(newVQ, merged); //note, we won't have merged task in the queue
        System.out.println("latestpos="+latestpos);
        //put merged to position that it'll not miss
        newVQ.add(latestpos,merged);
        long check=countDLMiss( newVQ,Math.abs(originalmiss),GOPTaskScheduler_mergable.SDco);
        System.out.println(check +" vs "+originalmiss);
        if ( Math.abs(check)<= Math.abs(originalmiss)) {
            //make change to real queue
            System.out.println("merged");
            Batchqueue.remove(original); //remove from old position
            original.getAllCMD(X);
            Batchqueue.add(latestpos,original); //re add at new specific position
            return latestpos;
        }
        return -1;
    }


    private int virtualQueueCheckReplace(StreamGOP Original,StreamGOP merged,int threshold){
        miscTools.SortableList newVQ = new miscTools.SortableList(Batchqueue);
        int indexofOriginal=newVQ.indexOf(Original);
        if(indexofOriginal<0||indexofOriginal>newVQ.size()){
            System.out.println("anomaly in virtualQueueCheckReplace index="+indexofOriginal);
            return -999;
        }else {
            newVQ.set(newVQ.indexOf(Original), merged); //can not find original???
            return countDLMiss(newVQ, null,threshold,GOPTaskScheduler_mergable.SDco);
        }
    }

    private MachineInterface v_selectMachine(StreamGOP x, int[] queuelength, long[] executiontime){
        //System.out.println("virtual estimation");
        if(ServerConfig.schedulerPolicy.equalsIgnoreCase("minmin")){
            //minimum expectedTime is basically ShortestQueueFirst but calculate using TimeEstimator, and QueueExpectedTime
            return GOPTaskScheduler_mergable.shortestQueueFirst(x,queuelength,executiontime,true,1,false);
        }else { //default way, shortestQueueFirst
            return GOPTaskScheduler_mergable.shortestQueueFirst(x,queuelength,executiontime,false,1,false); //false for not using TimeEstimator
        }
    }
    private int countOriginalMiss(StreamGOP X,double SDco){
        miscTools.SortableList newVQ = new miscTools.SortableList(Batchqueue);
        newVQ.add(X);
        //System.out.println("countOriginalMiss");
        //System.out.println(newVQ.toArray()[0]);
        return countDLMiss(newVQ,Integer.MAX_VALUE,SDco);

    }
    private int countDLMiss(miscTools.SortableList virtualQueue,int rthreshold,double SDco) {
        return countDLMiss(virtualQueue,null,rthreshold,SDco);
    }

    private void fillEstimatorArray(int[] ql,long[] et){
        for(int i = 0; i< machineInterfaces.size(); i++){ //this queuelength of already assigned tasks use SD=2
            ql[i]= machineInterfaces.get(i).estimatedQueueLength;
            et[i]= machineInterfaces.get(i).estimatedExecutionTime;
        }
    }
    //return 0 for no miss, x for x missed deadline
    private int countDLMiss(miscTools.SortableList virtualQueue,StreamGOP target,int rthreshold,double SDco){

        // perform check
        int missed=0,targetmiss=0;
        int threshold=Math.abs(rthreshold);
        miscTools.SortableList virtualQueue_copy = new miscTools.SortableList(virtualQueue);
        //copy virtual queue
        int[] queuelength=new int[machineInterfaces.size()];
        long[] executiontime=new long[machineInterfaces.size()];
        fillEstimatorArray(queuelength,executiontime);

        for(int i=0;i<virtualQueue_copy.size();i++){
            probecounter++;
            //get a GOP
            StreamGOP aGOP=virtualQueue_copy.removeDefault();
            //get a machine
            MachineInterface machine= v_selectMachine(aGOP,queuelength,executiontime);
            int machine_index= machineInterfaces.indexOf(machine);
            //update our queue
            retStat thestat=TimeEstimator.getHistoricProcessTime(machine.VM_class,machine.port,aGOP);
            executiontime[machine_index]+=thestat.mean+thestat.SD*SDco; //thestat.SD;
            queuelength[machine_index]++;
            long finishTimeofX= executiontime[machine_index];
            if(finishTimeofX>aGOP.deadLine){
                missed++;
                if(aGOP==target){
                    targetmiss=1;
                }
                if(missed>threshold){
                    //miss over the limit, no need to consider further
                    return (1-targetmiss*2)*missed; //1 for no target miss, -1 for target miss
                }
            }
        }
        return missed;
    }

    //find latest position possible to inject task to the unsorted queue
    private int findlatestposition(miscTools.SortableList  virtualQueue,StreamGOP themergedtask){

        // perform check
        miscTools.SortableList virtualQueue_copy = new miscTools.SortableList(virtualQueue);
        //copy virtual queue
        int[] queuelength=new int[machineInterfaces.size()];
        long[] executiontime=new long[machineInterfaces.size()];
        fillEstimatorArray(queuelength,executiontime);

        for(int i=0;i<virtualQueue_copy.size();i++) {
            probecounter++;
            // try themergedtask first, if fail then return
            MachineInterface machine = v_selectMachine(themergedtask, queuelength, executiontime);
            int machine_index = machineInterfaces.indexOf(machine);
            retStat thestat=TimeEstimator.getHistoricProcessTime(machine.VM_class, machine.port, themergedtask);
            if(executiontime[machine_index]+thestat.mean>themergedtask.deadLine){
                return i-1;
            }

            //not missing, therefore assign a new task
            //get a GOP
            StreamGOP aGOP = virtualQueue_copy.removeDefault();
            //get a machine
            machine = v_selectMachine(aGOP, queuelength, executiontime);
            machine_index = machineInterfaces.indexOf(machine);
            //update our queue
            thestat = TimeEstimator.getHistoricProcessTime(machine.VM_class, machine.port, aGOP);
            executiontime[machine_index] += thestat.mean; //thestat.SD;
            queuelength[machine_index]++;
            long finishTimeofX = executiontime[machine_index];
            if (finishTimeofX > aGOP.deadLine) {
                //do nothing for now
            }
        }
        return virtualQueue_copy.size()-1; // nowhere is missed
    }

    public void removeStreamGOPfromTable(StreamGOP X){
        //remove anything with this value (need removeAll, not remove or it'll only remove the first one)
        mergePending_tasklvl.values().removeAll(Collections.singleton(X));
        mergePending_operationlvl.values().removeAll(Collections.singleton(X));
        mergePending_datalvl.values().removeAll(Collections.singleton(X));
        pendingqueue.remove(X); //only one record here
        //can try this way too
        //mergePending_tasklvl.values().removeIf(val -> X.equals(val));
    }
    public void updateTable(){

    }

    public boolean trymerge(StreamGOP X,int originalmiss,Streampkg.TaskRequest requestSig,HashMap<Streampkg.TaskRequest,StreamGOP> LVmap_pending) {

        StreamGOP itspair = LVmap_pending.get(requestSig);
        if (itspair.dispatched) {
            System.out.println("too late to merge, pair already dispatched");
            return false;
        } else {
            //System.out.println("not too late to merge");
            // create merged StreamGOP
            StreamGOP merged = new StreamGOP(itspair); //create a copy of the old one for evaluation, but don't use this object
            merged.getAllCMD(X);
            // do merging
            if(!ServerConfig.mergeOverwriteQueuePolicy){ //if we obey queuing policy
                long checked = 0;
                if(ServerConfig.consideratemerge){ //if we want to evaluate merge impact...
                    checked = virtualQueueCheckReplace(itspair, merged, Math.abs(originalmiss)); //assume direct replace to the object
                }
                /*
                System.out.println(checked + " vs " + originalmiss);
                retStat chkmerged = TimeEstimator.getHistoricProcessTime(ServerConfig.VM_class.get(0), merged);
                System.out.println("runtime merged:" + chkmerged.mean + "(" + chkmerged.SD + ")");
                retStat chkX = TimeEstimator.getHistoricProcessTime(ServerConfig.VM_class.get(0), X);
                System.out.println("runtime X:" + chkX.mean + "(" + chkX.SD + ")");
                retStat chkoriginal = TimeEstimator.getHistoricProcessTime(ServerConfig.VM_class.get(0), itspair);
                System.out.println("runtime itspair:" + chkoriginal.mean + "(" + chkoriginal.SD + ")");
                */
                if (Math.abs(checked) <= Math.abs(originalmiss)) { //worth it, merge!
                    itspair.getAllCMD(X); //reuse itspair object, add new parameters
                    System.out.println("Merge");
                    //don't update execution time yet, lets do that on dispatch event
                    /*
                    if(...){ //if task need to be reinsert to resort the queue
                        Batchqueue.remove(itspair);
                        Batchqueue.add(itspair);
                    }
                    */
                    return true;
                }else{
                    System.out.println("no Merge");
                    return false;
                }
            }else{
                System.out.println("try merge with overwriting queue positioning, work with FIFO queue only");
                if(ServerConfig.consideratemerge){
                    ///////////////////////////////////////////// redo code both upper and below
                    if (ServerConfig.overwriteQueuePolicyHeuristic.equalsIgnoreCase("logarithmic")) {
                        // logarithmic probe
                        if (Bsearch_trybetweenPositions(requestSig, LVmap_pending, X, itspair, merged, originalmiss) == -1) {
                            System.out.println("don't merge");
                            return false;
                        }
                    }else { //default to linearProbe
                        if (linearsearch_trybetweenPositions(requestSig, LVmap_pending, X, itspair, merged, originalmiss) == -1) {
                            System.out.println("don't merge");
                            return false;
                        }
                    }
                }else{
                    System.out.println("merge");
                    itspair.getAllCMD(X);
                    return true;
                }
            }
        }
        return false;
    }


    public void mergeifpossible(StreamGOP X,double SDco){
        //HOLD UP! check for duplication first
        Streampkg.TaskRequest aRequestlvl1 = new Streampkg.TaskRequest(X,1); //= ... derive from X
        System.out.println(X.videoname+" ");
        if (mergePending_tasklvl.containsKey(aRequestlvl1)) {
            merged_tasklvl_count++;
            System.out.println("match Task lvl (exactly the same request) -> dropping this request");
            //don't even need to check if it is not null or state is not dispatched
        }else {
            System.out.println("not duplicated");
            //count how many miss would happen if not merging
            int originalmiss = countOriginalMiss(X,SDco);
            //System.out.println("Original miss=" + originalmiss);

            //create task signature, to check for matching
            Streampkg.TaskRequest aRequestlvl2 = new Streampkg.TaskRequest(X, 2);
            Streampkg.TaskRequest aRequestlvl3 = new Streampkg.TaskRequest(X, 3);

            //check level 2
            if (mergePending_operationlvl.containsKey(aRequestlvl2)) {
                if (trymerge(X, originalmiss, aRequestlvl2, mergePending_operationlvl)) {
                    System.out.println("Merged in lvl2");
                    return;
                }
            }
            if (mergePending_datalvl.containsKey(aRequestlvl3)) {
                if (trymerge(X, originalmiss, aRequestlvl3, mergePending_datalvl)) {
                    System.out.println("Merged in lvl3");
                    return;
                }
            }
            System.out.println("add to queue directly, not matching anything");
            mergePending_tasklvl.put(aRequestlvl1, X); //or replace???
            mergePending_operationlvl.put(aRequestlvl2, X);
            mergePending_operationlvl.put(aRequestlvl3, X);
            Batchqueue.add(X);
        }
    }

}
