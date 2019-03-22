package IOWindows;

import Repository.Video;
import Scheduler.GOPTaskScheduler;
import Scheduler.GOPTaskScheduler_mergable;
import Scheduler.ServerConfig;
import Streampkg.StreamGOP;
import mainPackage.CVSE;

import java.util.List;


public class OutputWindow {
    CVSE _CVSE;
    public OutputWindow(CVSE cvse){
        _CVSE=cvse;
    }
    private Video video;
    public void ackCompletedVideo(List<StreamGOP> completedTasks){

        if(_CVSE.GTS instanceof GOPTaskScheduler_mergable) {
            GOPTaskScheduler_mergable GTS= (GOPTaskScheduler_mergable) _CVSE.GTS;
            if (ServerConfig.taskmerge) {
                for (StreamGOP g: completedTasks
                     ) {
                    GTS.MRG.removeStreamGOPfromTable(g);
                }
            }
        }
    }
    public void PlayVideo()
    {

    }
}
