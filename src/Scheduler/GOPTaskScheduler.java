package Scheduler;

import Stream.*;
import TranscodingVM.*;
import com.amazonaws.services.opsworkscm.model.Server;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


import java.util.*;

class request{ //fill every thing for lvl1 mapping constructor, skip resolution for lvl2 mapping, ...
    private final String command;
    private final String param;
    private final String Path;
    //constructor with StreamGOP and intended level of matching
    //command list will only contain ONE command and param, no merge
    request(StreamGOP original,int level){
        String thecmd="";
        String theparam="";
        int i=0;
        for(String cmd : original.cmdSet.keySet()){
            thecmd=cmd;
            theparam=original.cmdSet.get(thecmd).get(0);
            i++;
            if(i>1){
                System.out.println("why do we have merged command here?");
            }
        }
        if(level==3) { //Type C
            Path=original.getPath(); //match video segment
            command="";
            param="";
        }else if(level==2){ //Type B
            Path=original.getPath();
            command=thecmd; //match command
            param="";
        }else{ // Type A
            Path=original.getPath();
            command=thecmd;
            param=theparam; //match resolution too

        }

    }
    public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
                // if deriving: appendSuper(super.hashCode()).
                        append(command).
                        append(param).
                        append(Path).
                        toHashCode();
    }

    //return true if it's match, design to compare to request constructed with the SAME level
    public boolean equals(Object obj){
        if (!(obj instanceof request)) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        request X=(request)obj;
        return new EqualsBuilder().
                append(command,X.command).
                append(param,X.param).
                append(Path,X.Path).
                isEquals();
        }
}

public class GOPTaskScheduler {
    private miscTools.SortableList Batchqueue=new miscTools.SortableList();
    private miscTools.SortableList pendingqueue=new miscTools.SortableList();
    private int working=0;
    private HashMap<request,StreamGOP> LV1map_pending=new HashMap<request,StreamGOP>();;
    private HashMap<request,StreamGOP> LV2map_pending=new HashMap<request,StreamGOP>();
    private HashMap<request,StreamGOP> LV3map_pending=new HashMap<request,StreamGOP>();
    //private HashMap<request,List<StreamGOP>> LV2map_pending=new HashMap<request,List<StreamGOP>>(); //level2's request record skip resolution so more matches


    public static long maxElapsedTime; //use for setting Deadline
    public static ArrayList<VMinterface> VMinterfaces =new ArrayList<VMinterface>();
    private static int maxpending=0;
    public static int workpending=0;
    public GOPTaskScheduler(){
        if(ServerConfig.mapping_mechanism.equalsIgnoreCase("ShortestQueueFirst")){
            //add server list to ShortestQueueFirst list too
        }
    }

    public static boolean add_VM(String VM_class,String addr,int port,int id){
        VMinterface t=new VMinterface(VM_class,addr,port,id);
        maxpending+= ServerConfig.localqueuelengthperVM; //4?
        VMinterfaces.add(t);
        return true; //for success
    }
    public static boolean remove_VM(int which){
        VMinterfaces.remove(which);
        maxpending-= ServerConfig.localqueuelengthperVM; //4?
        return true;
    }


    public int tryOriginalUnmerged(StreamGOP X){
        ArrayList<StreamGOP> newVQ = new ArrayList<StreamGOP>(Batchqueue);
        newVQ.add(X);
        //System.out.println("tryOriginalUnmerged");
        //System.out.println(newVQ.toArray()[0]);
        return virtualQueueCheckReplace(newVQ, X,Integer.MAX_VALUE,false);

    }
    //return -1, can't merge
    //1, tail
    //2, head
    //3, between
    public int try2Positions(request key,HashMap<request,StreamGOP> thislvlmap,StreamGOP X,StreamGOP original,StreamGOP merged,int originalmiss){
        //batch queue is not sorted, try 3+ location
        ArrayList<StreamGOP> newVQ = new ArrayList<StreamGOP>(Batchqueue);

        //try latest location
        newVQ.remove(original);
        newVQ.add(merged);
        if (virtualQueueCheckReplace( newVQ, merged,Math.abs(originalmiss),false) <= Math.abs(originalmiss)) { //succesful, no miss
            Batchqueue.remove(original);
            //and need to update table?
            Batchqueue.add(original); //so that we don't need to update table, rather than add merged and update table to point to merged
            original.getAllCMD(X);
            return 1;
        }

        //try early location
        newVQ = new ArrayList<StreamGOP>(Batchqueue);
        newVQ.set(newVQ.indexOf(original), merged);
        if (virtualQueueCheckReplace(newVQ, merged,Math.abs(originalmiss),false) <= Math.abs(originalmiss)) {
            //Batchqueue.set(Batchqueue.indexOf(original),merged); //not like this! that would mean we need to update table a b c!
            original.getAllCMD(X);
            return 2;
        }
        //nothing success, return -1;
        return -1;
    }
    public int linearsearch_trybetweenPositions(request key,HashMap<request,StreamGOP> thislvlmap,StreamGOP X,StreamGOP original,StreamGOP merged,int originalmiss) {
        ArrayList<StreamGOP> newVQ = new ArrayList<StreamGOP>(Batchqueue);
        newVQ.remove(original);
        newVQ.add(merged);
        //get latest position for X not to miss their deadline
        int latestpos=virtualQueueCheckReplace(newVQ, merged,Math.abs(originalmiss),true);
        newVQ.remove(merged);
        newVQ.add(latestpos,merged);
        if (virtualQueueCheckReplace( newVQ, merged,Math.abs(originalmiss),false) <= Math.abs(originalmiss)) {
            Batchqueue.remove(original); //remove from old position
            original.getAllCMD(X);
            Batchqueue.add(latestpos,original); //re add at new specific position
            return latestpos;
        }
        return -1;
    }

        //binary search version, only work if original miss ==0
    public int Bsearch_trybetweenPositions(request key,HashMap<request,StreamGOP> thislvlmap,StreamGOP X,StreamGOP original,StreamGOP merged,int originalmiss) {
        ArrayList<StreamGOP> newVQ = new ArrayList<StreamGOP>(Batchqueue);
        int Xpos=newVQ.size()-1;
        int originalpos=newVQ.indexOf(original);
        newVQ.remove(original);
        if(originalpos<0){
            System.out.println("trybetweenPositions pos err");
            return -1;
        }
        int newXpos=Xpos,searchlimit=originalpos;
        while(newXpos<searchlimit-1){
            int tryposition=(searchlimit+newXpos)/2;
            newVQ.add(tryposition,merged);
            int test=virtualQueueCheckReplace( newVQ, merged,Integer.MAX_VALUE,false);
            if(test==0){
                return tryposition; //found perfect condition
            }
            if(test<-1){
                return -1; //both merged one and other have miss their deadline, no perfect exist
            }
            if(test==-1){
                newXpos=tryposition; //x need to move forward
            }else{
                searchlimit=tryposition; // x need to move backward
            }
            newVQ.remove(tryposition); //it fail, so remove this
        }
        return -1;
    }


        public void removeStreamGOPfromTable(StreamGOP X){
        //remove anything with this value (need removeAll, not remove or it'll only remove the first one)
        LV1map_pending.values().removeAll(Collections.singleton(X));
        LV2map_pending.values().removeAll(Collections.singleton(X));
        LV3map_pending.values().removeAll(Collections.singleton(X));
        pendingqueue.remove(X); //only one record here
        //can try this way too
        //LV1map_pending.values().removeIf(val -> X.equals(val));
    }

    //bloated version of addStream, check duplication and similarity first
    public void addStream(Stream ST){
        //Batchqueue.addAll(ST.streamGOPs); // can not just mass add without checking each piece if exist
        for(StreamGOP X:ST.streamGOPs) {
            //HOLD UP! check for duplication first
            request aRequestlvl1 = new request(X,1); //= ... derive from X
            System.out.println(X.getPath() +" "+X.videoname);
            if (LV1map_pending.containsKey(aRequestlvl1)) {
                System.out.println("match Type A (exactly the same request) -> dropping this request, no question!");
                //don't even need to check if it is not null or state is not dispatched
            }else{
                //System.out.println("not match A");
                LV1map_pending.put(aRequestlvl1,X); //add to the level0 map, no need to be a list as there aren't any object that is not merged
                int originalmiss=tryOriginalUnmerged(X);
                //check level 2
                request aRequestlvl2 = new request(X,2);
                if (LV2map_pending.containsKey(aRequestlvl2)) {
                    System.out.println("match Type B (request on the same video and same command, dif resolution)");
                    StreamGOP original=LV2map_pending.get(aRequestlvl2);
                    if(original.dispatched){
                        System.out.println("too late to merge, already dispatched");
                    }else {
                        System.out.println("not too late to merge");

                        // create merged StreamGOP
                        StreamGOP merged = new StreamGOP(original);
                        merged.getAllCMD(X);
                        // do merging
                        if (ServerConfig.sortedBatchQueue) {
                            System.out.println("merge on sorted batch queue");
                            //batch queue sorted by deadline, no need to change anything
                            if (virtualQueueCheckReplace(original, merged,Math.abs(originalmiss)) <=Math.abs(originalmiss)) {
                                Batchqueue.remove(original);
                                Batchqueue.add(merged);
                                System.out.println("merged");
                            } else {
                                System.out.println("don't merge");
                                // IMPORTANT, if we don't merge, redo LV2map_pending.put(aRequestlvl2,X); to say we are the candidate for merge not the one we fail to merge with
                                Batchqueue.add(X);
                                LV2map_pending.replace(aRequestlvl2, X);
                            }
                        } else {
                            System.out.println("merge on unsorted batch queue");
                            if(try2Positions(aRequestlvl2,LV2map_pending,X,original,merged,originalmiss)==-1){
                                //try hard to merge
                                    if (linearsearch_trybetweenPositions(aRequestlvl2, LV2map_pending, X, original, merged, originalmiss) == -1) {
                                        Batchqueue.add(X);
                                        LV2map_pending.replace(aRequestlvl2, X);
                                    }
                                    // IMPORTANT, if we don't merge, redo LV2map_pending.put(aRequestlvl2,X); to say we are the candidate for merge not the one we fail to merge with
                            }
                        }
                    }
                }else{
                    //System.out.println("not match B");
                    //didn't see in lvl2, try lvl3 merge, DO NOT TRY HARD TO FIND BETWEEN POSITION, IT"S NOT WORTH IT FOR THIS LVL
                    LV2map_pending.put(aRequestlvl2,X);
                    request aRequestlvl3 = new request(X,3);

                    if(LV3map_pending.containsKey(aRequestlvl3)){
                        System.out.println("match Type C (same video)");
                        StreamGOP original=LV3map_pending.get(aRequestlvl3);
                        if(original.dispatched){
                            System.out.println("too late to merge, already dispatched");
                        }else {
                            System.out.println("not too late to merge (lvl3, is not doing yet");
                            // create merged StreamGOP
                            StreamGOP merged = new StreamGOP(original);
                            merged.getAllCMD(X);
                            // do merging
                            if (ServerConfig.sortedBatchQueue) {
                                System.out.println("merge on sorted batch queue");
                                //batch queue sorted by deadline, no need to change anything
                                if (virtualQueueCheckReplace(original, merged,Math.abs(originalmiss)) <=Math.abs(originalmiss)) {
                                    Batchqueue.remove(original);
                                    Batchqueue.add(merged);
                                    System.out.println("merged");
                                } else {
                                    System.out.println("don't merge");
                                     Batchqueue.add(X);
                                    LV3map_pending.replace(aRequestlvl3, X);
                                }
                            } else {
                                System.out.println("merge on unsorted batch queue");
                                if(try2Positions(aRequestlvl3,LV3map_pending,X,original,merged,originalmiss)==-1){
                                    //try hard to merge
                                   // if(trybetweenPositions(aRequestlvl3,LV3map_pending,X,original,merged,originalmiss)==-1) {
                                        Batchqueue.add(X);
                                        LV3map_pending.replace(aRequestlvl3, X);
                                  //  }
                                    // IMPORTANT, if we don't merge, redo LV2map_pending.put(aRequestlvl2,X); to say we are the candidate for merge not the one we fail to merge with
                                }
                            }
                        }

                    }else{
                        System.out.println("add to queue directly, not matching anything");
                        LV3map_pending.put(aRequestlvl3,X);
                        Batchqueue.add(X); //only add directly if no match at all
                    }
                   // ArrayList<StreamGOP> listofGOP=new ArrayList<StreamGOP>();
                   // listofGOP.add(X);
                   // LV2map_pending.put(aRequestlvl2,listofGOP);

                    // check level 3

                    //...
                }
            }
        }
            //assignwork thread start
            submitworks();
    }

    private int virtualQueueCheckReplace(StreamGOP Original,StreamGOP merged,int threshold){
        ArrayList<StreamGOP> newVQ = new ArrayList<StreamGOP>(Batchqueue);
        newVQ.set(newVQ.indexOf(Original),merged);
        return virtualQueueCheckReplace(newVQ,Original,threshold,false);
    }
    //return 0 for no miss, x for x missed deadline, -x for x missed deadline which include the original GOP miss their deadline too
    private int virtualQueueCheckReplace(ArrayList<StreamGOP> virtualQueue,StreamGOP focussed,int threshold,boolean find_Xpos){
        // copy machine queue
        double[] VM_Q=new double[VMinterfaces.size()];
        for (int i = 0; i < VMinterfaces.size(); i++) {
            VM_Q[i]=(double)VMinterfaces.get(i).estimatedExecutionTime;
            if(ServerConfig.run_mode.equalsIgnoreCase("dry")){
                VM_Q[i]+=VMinterfaces.get(i).elapsedTime; //dry mode need to measure elapsedTime too
            }
        }
        // perform check
        int missed=0;
        int originalmissed=1;
        int latestXpos=0;
        for(int i=0;i<virtualQueue.size();i++){
                int choice=virtualShortestExeFirst(VM_Q,focussed,1);
                double exeT=VM_Q[choice]+TimeEstimator.getHistoricProcessTime(ServerConfig.VM_class.get(choice), virtualQueue.get(i), 1);
                if(exeT>virtualQueue.get(i).getDeadLine()){
                    //deadline miss
                    if(virtualQueue.get(i)==focussed){
                        originalmissed=-1;
                    }
                    missed++;
                    if(missed>threshold){
                        return missed*originalmissed;
                    }
                }else{
                    //update VMQ
                    VM_Q[choice]=exeT;
                }
            if(find_Xpos){
                    if(VM_Q[choice]+TimeEstimator.getHistoricProcessTime(ServerConfig.VM_class.get(choice), focussed, 1) <focussed.getDeadLine()) {
                        latestXpos=i;
                    }
            }
        }
        //
        if(find_Xpos){ //finding latest position x can resides
            return latestXpos;
        }
        return missed*originalmissed;
    }
    private int virtualShortestExeFirst(double[] VMQlength,StreamGOP x,double SDcoefficient){
        double shortest=VMQlength[0]+TimeEstimator.getHistoricProcessTime(ServerConfig.VM_class.get(0), x, SDcoefficient);
        int choice=0;
        for(int i=1;i<VMQlength.length;i++){
            double sample=VMQlength[i]+TimeEstimator.getHistoricProcessTime(ServerConfig.VM_class.get(i), x, SDcoefficient);
            if(sample<shortest){
                choice=i;
                shortest=sample;
            }
        }
    return choice;
    }
    private VMinterface shortestQueueFirst(StreamGOP x,boolean useTimeEstimator,double SDcoefficient){
        int addedConstForDeadLine=50;

        if(VMinterfaces.size()>0) {
            VMinterface answer=VMinterfaces.get(0);
            long min;
            if(useTimeEstimator){
                    x.estimatedExecutionTime = TimeEstimator.getHistoricProcessTime(ServerConfig.VM_class.get(0), x, SDcoefficient);
                min=answer.estimatedExecutionTime+x.estimatedExecutionTime;
                if(ServerConfig.run_mode.equalsIgnoreCase("dry")){
                    min+=answer.estimatedQueueLength;
                }

            }else{
                min = answer.estimatedQueueLength;
            }
            for (int i = 1; i < VMinterfaces.size(); i++) {
                VMinterface aMachine = VMinterfaces.get(i);
                if (aMachine.isWorking()) {
                    long estimatedT;
                    long savedmean=0;
                    //calculate new choice
                    if (useTimeEstimator) {
                        savedmean=TimeEstimator.getHistoricProcessTime(ServerConfig.VM_class.get(i), x,SDcoefficient);;
                        estimatedT = aMachine.estimatedExecutionTime + savedmean;
                        if(ServerConfig.run_mode.equalsIgnoreCase("dry")){
                            min+=answer.estimatedQueueLength;
                        }
                    } else {
                        estimatedT = aMachine.estimatedQueueLength;
                    }
                    //decide
                    if (estimatedT < min) {
                        if (useTimeEstimator){ //update estimatedExecutionTime
                            x.estimatedExecutionTime = savedmean;
                        }
                        answer = aMachine;
                        min = estimatedT;
                    }
                }
            }
            /*
            // set up (pseudo) deadline ?
            if(ServerConfig.run_mode.equalsIgnoreCase("dry")) {
                x.setDeadline(min);
            }else {
                x.setDeadline(System.currentTimeMillis() + min + addedConstForDeadLine);
            }
            */
            return answer;
        }
        System.out.println("BUG: try to schedule to 0 VM");
        return null;
    }

    //will have more ways to assign works later
    private VMinterface assignworks(StreamGOP x){
        //System.out.println("assigning works");
        if(ServerConfig.schedulerPolicy.equalsIgnoreCase("minmin")){
            //minimum expectedTime is basically ShortestQueueFirst but calculate using TimeEstimator, and QueueExpectedTime
            return shortestQueueFirst(x,true,1);
        }else { //default way, shortestQueueFirst
            return shortestQueueFirst(x,false,1); //false for not using TimeEstimator, not virtual assign
        }
    }
    //function to test if virtually assign and nothing miss their deadline
    private boolean virtualTest(){
    //update Data, need updated Estimated ExecutionTime
        //...

        return false;
    }
    public void submitworks(){ //will be a thread
        //read through list and assign to TranscodingVM
        //now we only assign task in round robin
        if(working!=1) {
            working = 1;
            while ((!Batchqueue.isEmpty()) && workpending < maxpending) {
                StreamGOP X;
                if(ServerConfig.sortedBatchQueue) {
                    //X=Batchqueue.pollHighestPrio(); //don't forget to remove them later
                    X=Batchqueue.removeHighestPrio();
                }else{
                    //X= Batchqueue.poll();
                    X= Batchqueue.remove();
                }
                pendingqueue.add(X);
                //
                //mapping_policy function
                //
                VMinterface chosenVM = assignworks(X);
                if (ServerConfig.enableVMscalingoutofInterval && (chosenVM.estimatedQueueLength > ServerConfig.maxVMqueuelength)) {
                    //do reprovisioner, we need more VM!
                    System.out.println("queue too long");
                    //VMProvisioner.EvaluateClusterSize(0.8,Batchqueue.size());
                    if (ServerConfig.enableVMscalingoutofInterval) {
                        VMProvisioner.EvaluateClusterSize(-2);
                    }
                    //re-assign works
                    chosenVM = assignworks(X);
                }

                //change deadLine to absolute value
                X.setDeadline(X.getDeadLine());
                //change StreamGOP type to Dispatched
                X.dispatched = true;
                //X.parentStream=null;

                //then it's ready to send out

                chosenVM.sendJob(X);
                System.out.println("send job " + X.getPath() + " to " + chosenVM.toString());
                System.out.println("estimated queuelength=" + chosenVM.estimatedQueueLength);
                System.out.println("estimated ExecutionTime=" + chosenVM.estimatedExecutionTime);
                workpending++;
                System.out.println("workpending=" + workpending + " maxpending=" + maxpending);
                if (workpending == maxpending) {
                    System.out.println("workpending==maxpending");
                }
            }
            working=0;
        }
    }

    //turn off VMS socket connection sockets
    public void close(){
        for(int i = 0; i< VMinterfaces.size(); i++){
            VMinterfaces.get(i).close();
        }
    }
}