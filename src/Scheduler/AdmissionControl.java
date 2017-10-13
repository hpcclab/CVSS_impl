package Scheduler;

import Repository.RepositoryGOP;
import Stream.*;

public class AdmissionControl {
    public void AssignSegmentPriority(StreamGOP segment)
    {
        segment.setPriority(0);
    }
    public void AssignSegmentPriority(StreamGOP segment,int priority)
    {
        segment.setPriority(priority);
    }
    public void AssignStreamPriority(Stream stream){
        for (StreamGOP x :stream.streamGOPs){
            int newPriority;
            //how we actually assign priority?
            newPriority=Integer.parseInt(x.segment);

            //
            x.setPriority(newPriority);
        }

    }
    private void GetVideoSplitterSegmentInfo(StreamGOP segment)
    {

    }

    private void GetVideoMergerSegmentInfo(StreamGOP segment)
    {

    }

}
