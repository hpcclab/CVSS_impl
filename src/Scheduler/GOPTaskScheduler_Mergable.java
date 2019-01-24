package Scheduler;

import Streampkg.*;
import TimeEstimatorpkg.TimeEstimator;
import TimeEstimatorpkg.retStat;
import VMManagement.*;



public class GOPTaskScheduler_Mergable extends GOPTaskScheduler_common {

    public static Merger mrg;
    //private HashMap<request,List<StreamGOP>> LV2map_pending=new HashMap<request,List<StreamGOP>>(); //level2's request record skip resolution so more matches



    public static double SDco=2;
    private static long oversubscriptionlevel;
    public GOPTaskScheduler_Mergable(){
        if(ServerConfig.mapping_mechanism.equalsIgnoreCase("ShortestQueueFirst")){
            //add server list to ShortestQueueFirst list too
        }
        if(ServerConfig.taskmerge){
            mrg= new Merger(Batchqueue,pendingqueue,VMinterfaces);
        }
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

    //function that do something after task X get sent
    protected void postschedulefn(StreamGOP X){
        //System.out.println("overwritten postschedulefn is CALLED\n\n\n");
        if(ServerConfig.taskmerge) {
            mrg.removeStreamGOPfromTable(X);
        }
    }
}