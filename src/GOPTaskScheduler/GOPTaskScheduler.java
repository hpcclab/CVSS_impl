package GOPTaskScheduler;

import Repository.RepositoryGOP;
import TranscodingVM.TranscodingVM;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Created by pi on 5/21/17.
 */
public class GOPTaskScheduler {

    private Queue<RepositoryGOP> batchQueue;
    private List<TranscodingVM> transcodingVMs = new ArrayList<TranscodingVM>();

    public void ScheduleJob(RepositoryGOP segment)
    {

    }

    private Boolean isJobQueueAvailable()
    {
        Boolean isAvailable = false;

        return isAvailable;
    }



}
