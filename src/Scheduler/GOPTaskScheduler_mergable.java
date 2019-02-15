package Scheduler;

import Streampkg.*;
import TimeEstimatorpkg.TimeEstimator;
import TimeEstimatorpkg.retStat;
import VMManagement.*;


// mergable GOPTaskScheduler, this is just mostly pairing merger class to GOPTaskScheduler.
// currently pending queue is not really used
public class GOPTaskScheduler_mergable extends GOPTaskScheduler_common {

    public static Merger mrg;
    protected miscTools.SortableList pendingqueue = new miscTools.SortableList(); //keep track of pending task (submitted, but didn't completed)


    public static double SDco=2;
    private static long oversubscriptionlevel;
    public GOPTaskScheduler_mergable(){
        super();
        if(ServerConfig.taskmerge){
            mrg= new Merger(Batchqueue,pendingqueue,VMinterfaces);
        }
    }

    public boolean emptyQueue() {
        if (Batchqueue != null && pendingqueue != null) {
            return (Batchqueue.isEmpty() || pendingqueue.isEmpty());
        }
        return true;
    }

    //bloated version of addStream, check duplication and similarity first
    public void addStream(Stream ST){
        //Batchqueue.addAll(ST.streamGOPs); // can not just mass add without checking each piece if exist
        for(StreamGOP X:ST.streamGOPs) {
            if(ServerConfig.taskmerge){
                mrg.mergeifpossible(X,SDco);
            }else{
                //dont merge check
                Batchqueue.add(X);
            }
        }
            //assignwork thread start
            taskScheduling();
    }
    //function that do something before task X get sent
    protected void preschedulefn(StreamGOP X){
        pendingqueue.add(X);
    }
    //function that do something after task X get sent
    protected void postschedulefn(StreamGOP X){
        //System.out.println("overwritten postschedulefn is CALLED\n\n\n");
        if(ServerConfig.taskmerge) {
            mrg.removeStreamGOPfromTable(X);
        }
    }
}