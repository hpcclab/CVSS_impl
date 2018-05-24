package Scheduler;

import Streampkg.StreamGOP;
import TimeEstimatorpkg.TimeEstimator;
import TimeEstimatorpkg.retStat;
import VMManagement.VMinterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Merger {

    private static HashMap<Streampkg.TaskRequest,StreamGOP> LV1map_pending=new HashMap<Streampkg.TaskRequest,StreamGOP>();;
    private static HashMap<Streampkg.TaskRequest,StreamGOP> LV2map_pending=new HashMap<Streampkg.TaskRequest,StreamGOP>();
    private static HashMap<Streampkg.TaskRequest,StreamGOP> LV3map_pending=new HashMap<Streampkg.TaskRequest,StreamGOP>();
    public static long typeAmerged=0;
    private miscTools.SortableList pendingqueue;
    private miscTools.SortableList Batchqueue;
    public static long probecounter=0;
    public static ArrayList<VMinterface> VMinterfaces;
    public Merger(miscTools.SortableList bq,miscTools.SortableList pq,ArrayList<VMinterface> vi) {
        Batchqueue=bq;
        pendingqueue=pq;
        VMinterfaces =vi;

    }

    //binary search version, only work if original miss ==0
    public int Bsearch_trybetweenPositions(Streampkg.TaskRequest key,HashMap<Streampkg.TaskRequest,StreamGOP> thislvlmap,StreamGOP X,StreamGOP original,StreamGOP merged,int originalmiss) {
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
                System.out.println("found a position");
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
        return 0;
    }
    public int tryOriginalUnmerged(StreamGOP X){
        ArrayList<StreamGOP> newVQ = new ArrayList<StreamGOP>(Batchqueue);
        newVQ.add(X);
        //System.out.println("tryOriginalUnmerged");
        //System.out.println(newVQ.toArray()[0]);
        return virtualQueueCheckReplace(newVQ, X,Integer.MAX_VALUE,false);

    }
    private int virtualQueueCheckReplace(StreamGOP Original,StreamGOP merged,int threshold){
        ArrayList<StreamGOP> newVQ = new ArrayList<StreamGOP>(Batchqueue);
        int indexofOriginal=newVQ.indexOf(Original);
        if(indexofOriginal<0||indexofOriginal>newVQ.size()){
            System.out.println("anomaly in virtualQueueCheckReplace index="+indexofOriginal);
            return -999;
        }else {
            newVQ.set(newVQ.indexOf(Original), merged); //can not find original???
            return virtualQueueCheckReplace(newVQ, Original, threshold, false);
        }
    }
    private int virtualShortestExeFirst(double[] VMQlength,StreamGOP x,double SDcoefficient){
        retStat chk= TimeEstimator.getHistoricProcessTime(ServerConfig.VM_class.get(0),x);
        double shortest=VMQlength[0]+chk.mean+chk.SD*SDcoefficient;
        int choice=0;
        for(int i=1;i<VMQlength.length;i++){
            retStat chkx=TimeEstimator.getHistoricProcessTime(ServerConfig.VM_class.get(0),x);
            double sample=VMQlength[i]+chkx.mean+chkx.SD*SDcoefficient;
            if(sample<shortest){
                choice=i;
                shortest=sample;
            }
        }
        return choice;
    }

    //return 0 for no miss, x for x missed deadline, -x for x missed deadline which include the original GOP miss their deadline too
    private int virtualQueueCheckReplace(ArrayList<StreamGOP> virtualQueue,StreamGOP focussed,int rthreshold,boolean find_Xpos){
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
        int threshold=Math.abs(rthreshold);
        for(int i=0;i<virtualQueue.size();i++){
            probecounter++;
            int choice=virtualShortestExeFirst(VM_Q,focussed,1);
            retStat chk=TimeEstimator.getHistoricProcessTime(ServerConfig.VM_class.get(choice),virtualQueue.get(i));
            double exeT=VM_Q[choice]+chk.mean+chk.SD*1;
            StreamGOP thisone=virtualQueue.get(i);
            if(exeT>thisone.deadLine){
                //deadline miss
                if(thisone==focussed){
                    originalmissed=-1;
                }
                missed+=thisone.requestcount ; //changed from ++
                if(missed>threshold){
                    return missed*originalmissed;
                }
            }else{
                //update VMQ
                VM_Q[choice]=exeT;
            }
            if(find_Xpos){
                //System.out.println("find Xpos");
                retStat chk2=TimeEstimator.getHistoricProcessTime(ServerConfig.VM_class.get(choice),focussed);
                double exeT2=VM_Q[choice]+chk2.mean+chk2.SD*1;
                if(VM_Q[choice]+exeT2 <focussed.deadLine) {
                    latestXpos=i;
                }else{
                    System.out.println("return Xpos");
                    return latestXpos;
                }
            }
        }
        //might return the latest position
        if(find_Xpos){ //finding latest position x can resides
            System.out.println("return Xpos");
            return latestXpos;
        }
        return missed*originalmissed;
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
    public void mergeifpossible(StreamGOP X){
        //HOLD UP! check for duplication first
        Streampkg.TaskRequest aRequestlvl1 = new Streampkg.TaskRequest(X,1); //= ... derive from X
        System.out.println(X.getPath() +" "+X.videoname);
        boolean uselinearprobe=true; //hard switch here for now

        if (LV1map_pending.containsKey(aRequestlvl1)) {
            typeAmerged++;
            System.out.println("match Type A (exactly the same request) -> dropping this request, no question!");
            //don't even need to check if it is not null or state is not dispatched
        }else {
            //System.out.println("not match A");
            LV1map_pending.put(aRequestlvl1, X); //add to the level0 map, no need to be a list as there aren't any object that is not merged
            int originalmiss = tryOriginalUnmerged(X);
            System.out.println("Original miss=" + originalmiss);
            //check level 2
            Streampkg.TaskRequest aRequestlvl2 = new Streampkg.TaskRequest(X, 2);
            if (LV2map_pending.containsKey(aRequestlvl2)) {
                System.out.println("match Type B (request on the same video and same command, dif resolution)");
                StreamGOP original = LV2map_pending.get(aRequestlvl2);
                if (original.dispatched) {
                    System.out.println("too late to merge, already dispatched");
                    Batchqueue.add(X);
                    LV2map_pending.replace(aRequestlvl2, X);
                } else {
                    System.out.println("not too late to merge");

                    // create merged StreamGOP
                    StreamGOP merged = new StreamGOP(original);
                    merged.getAllCMD(X);
                    // do merging
                    if (ServerConfig.sortedBatchQueue) {
                        System.out.println("merge on sorted batch queue");
                        //batch queue sorted by deadline, no need to change anything
                        long checked = 0;
                        if (ServerConfig.smartmerge){ //if not smart merge, always merge
                            checked = virtualQueueCheckReplace(original, merged, Math.abs(originalmiss));
                        }
                        System.out.println(checked+" vs "+originalmiss);
                        if ( Math.abs(checked)<= Math.abs(originalmiss)) {
                            LV2map_pending.replace(aRequestlvl2, merged);
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

                        if(ServerConfig.smartmerge) {
                            if(uselinearprobe) {
                                if (linearsearch_trybetweenPositions(aRequestlvl2, LV2map_pending, X, original, merged, originalmiss) == -1) {
                                    System.out.println("don't merge");
                                    Batchqueue.add(X);
                                    LV2map_pending.replace(aRequestlvl2, X);
                                }

                            }else { //logarithmic probe
                                if (try2Positions(aRequestlvl2, LV2map_pending, X, original, merged, originalmiss) == -1) {
                                    //try hard to merge
                                    if (Bsearch_trybetweenPositions(aRequestlvl2, LV2map_pending, X, original, merged, originalmiss) == -1) {
                                        System.out.println("don't merge");
                                        Batchqueue.add(X);
                                        LV2map_pending.replace(aRequestlvl2, X);
                                    }
                                    // IMPORTANT, if we don't merge, redo LV2map_pending.put(aRequestlvl2,X); to say we are the candidate for merge not the one we fail to merge with
                                }
                            }
                        }else{ //dumb merge
                            original.getAllCMD(X); //add all cmd to old request
                        }
                    }
                }
            } else {
                //System.out.println("not match B");
                //didn't see in lvl2, try lvl3 merge, DO NOT TRY HARD TO FIND BETWEEN POSITION, IT"S NOT WORTH IT FOR THIS LVL
                LV2map_pending.put(aRequestlvl2, X);
                Streampkg.TaskRequest aRequestlvl3 = new Streampkg.TaskRequest(X, 3);

                if (LV3map_pending.containsKey(aRequestlvl3)) {
                    System.out.println("match Type C (same video)");
                    StreamGOP original = LV3map_pending.get(aRequestlvl3);
                    if (original.dispatched) {
                        System.out.println("too late to merge, already dispatched");
                        Batchqueue.add(X);
                        LV3map_pending.replace(aRequestlvl3, X);
                    } else {
                        System.out.println("not too late to merge");
                        // create merged StreamGOP
                        StreamGOP merged = new StreamGOP(original);
                        merged.getAllCMD(X);
                        // do merging
                        if (ServerConfig.sortedBatchQueue) {
                            System.out.println("merge on sorted batch queue");
                            //batch queue sorted by deadline, no need to change anything
                            long checked = 0;
                            if (ServerConfig.smartmerge){ //if not smart merge, always merge
                                checked = virtualQueueCheckReplace(original, merged, Math.abs(originalmiss));
                            }
                            System.out.println(checked+" vs "+originalmiss);
                            retStat chkmerged=TimeEstimator.getHistoricProcessTime(ServerConfig.VM_class.get(0),merged);
                            System.out.println("runtime merged:"+chkmerged.mean+"("+ chkmerged.SD+")");
                            retStat chkX=TimeEstimator.getHistoricProcessTime(ServerConfig.VM_class.get(0),X);
                            System.out.println("runtime X:"+chkX.mean+"("+chkX.SD+")");
                            retStat chkoriginal=TimeEstimator.getHistoricProcessTime(ServerConfig.VM_class.get(0),original);
                            System.out.println("runtime original:"+chkoriginal.mean+"("+chkoriginal.SD+")");
                            if ( Math.abs(checked)<= Math.abs(originalmiss)) { // < or <= here is important, give different result
                                Batchqueue.remove(original);
                                Batchqueue.add(merged);
                                LV3map_pending.replace(aRequestlvl3, merged);
                                System.out.println("merged");
                            } else {
                                System.out.println("don't merge");
                                Batchqueue.add(X);
                                LV3map_pending.replace(aRequestlvl3, X);
                            }
                        } else {
                            System.out.println("merge on unsorted batch queue");
                            if(uselinearprobe) {
                                if (linearsearch_trybetweenPositions(aRequestlvl3, LV3map_pending, X, original, merged, originalmiss) == -1) {
                                    System.out.println("don't merge");
                                    Batchqueue.add(X);
                                    LV3map_pending.replace(aRequestlvl3, X);
                                }

                            }else {
                                // logarithmic probe
                                if (try2Positions(aRequestlvl3, LV3map_pending, X, original, merged, originalmiss) == -1) {
                                    //try hard to merge
                                    if(Bsearch_trybetweenPositions(aRequestlvl3,LV3map_pending,X,original,merged,originalmiss)==-1) {
                                        System.out.println("don't merge");
                                        Batchqueue.add(X);
                                        LV3map_pending.replace(aRequestlvl3, X);
                                    }
                                    // IMPORTANT, if we don't merge, redo LV2map_pending.put(aRequestlvl2,X); to say we are the candidate for merge not the one we fail to merge with
                                }
                            }
                        }
                    }

                } else {
                    System.out.println("add to queue directly, not matching anything");
                    LV3map_pending.put(aRequestlvl3, X);
                    Batchqueue.add(X); //only add directly if no match at all
                }
            }
        }
    }

    public int try2Positions(Streampkg.TaskRequest key, HashMap<Streampkg.TaskRequest,StreamGOP> thislvlmap, StreamGOP X, StreamGOP original, StreamGOP merged, int originalmiss){
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
    public int linearsearch_trybetweenPositions(Streampkg.TaskRequest key,HashMap<Streampkg.TaskRequest,StreamGOP> thislvlmap,StreamGOP X,StreamGOP original,StreamGOP merged,int originalmiss) {
        ArrayList<StreamGOP> newVQ = new ArrayList<StreamGOP>(Batchqueue);
        //put merged to latest position (at the moment)
        newVQ.remove(original);
        newVQ.add(merged);
        //get latest position for X not to miss their deadline
        int latestpos=virtualQueueCheckReplace(newVQ, merged,Math.abs(originalmiss),true);
        System.out.println("latestpos="+latestpos);
        //put merged to position that it'll not miss
        newVQ.remove(merged);
        newVQ.add(latestpos,merged);
        long check=virtualQueueCheckReplace( newVQ, merged,Math.abs(originalmiss),false);
        System.out.println(check +" vs "+originalmiss);
        if ( Math.abs(check)<= Math.abs(originalmiss)) {
            System.out.println("merged");
            Batchqueue.remove(original); //remove from old position
            original.getAllCMD(X);
            Batchqueue.add(latestpos,original); //re add at new specific position
            return latestpos;
        }
        return -1;
    }
}
