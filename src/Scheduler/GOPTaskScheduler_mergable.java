package Scheduler;

import Streampkg.Stream;
import Streampkg.StreamGOP;
import mainPackage.CVSE;
import miscTools.TaskQueue;


// mergable GOPTaskScheduler, this is just mostly pairing merger class to GOPTaskScheduler.
// currently pending queue is not really used
public class GOPTaskScheduler_mergable extends GOPTaskScheduler_common {

    public Merger MRG;
    protected TaskQueue pendingqueue; //keep track of pending task (submitted, but didn't completed)


    public double SDco=2;
    private long oversubscriptionlevel;
    public GOPTaskScheduler_mergable(){
        super();
        pendingqueue = new TaskQueue();
        MRG= new Merger(Batchqueue,pendingqueue, machineInterfaces);
    }

    public boolean emptyQueue() {
        if (Batchqueue != null && pendingqueue != null) {
            return (Batchqueue.isEmpty() || pendingqueue.isEmpty());
        }
        return true;
    }

    //bloated version of addStream, check duplication and similarity first
    public void addStream(Stream ST){
        CVSE.AC.AssignStreamPriority(ST);
        //Batchqueue.addAll(ST.streamGOPs); // can not just mass add without checking each piece if exist
        for(StreamGOP X:ST.streamGOPs) {
            if (!CVSE.CACHING.checkExistence(X)) {
                if (ServerConfig.taskmerge) {
                    MRG.mergeifpossible(X, SDco);
                } else {
                    //dont merge check
                    Batchqueue.add(X);
                }
            }else{
                System.out.println("GOP cached, no reprocess");
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
        /*
        if(ServerConfig.taskmerge) {
            mrg.removeStreamGOPfromTable(X);
        }
        */
    }
}