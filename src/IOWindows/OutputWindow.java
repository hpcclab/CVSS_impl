package IOWindows;

import Repository.Video;
//import Scheduler.GOPTaskScheduler_mergable;
import SessionPkg.TranscodingRequest;
import mainPackage.CVSE;

import java.util.List;


public class OutputWindow {
    public OutputWindow(){

    }
    private Video video;
    public void ackCompletedVideo(long Tid){

    }
    public void ackCompletedVideo(List<TranscodingRequest> completedTasks){

//        if(CVSE.GTS instanceof GOPTaskScheduler_mergable) {
//            GOPTaskScheduler_mergable GTS= (GOPTaskScheduler_mergable) CVSE.GTS;
//            if (CVSE.config.taskmerge) {
//                for (StreamGOP g: completedTasks
//                     ) {
//                    //a copy of pending queue
//                    GTS.MRG.removefromPendingQueue(g); //only one record here
//                    // already deleted when dispatch
//                    //GTS.MRG.removeStreamGOPfromTables(g);
//
//                }
//            }
//        }
    }
    public void PlayVideo()
    {

    }
}
