package TranscodingVM;

import java.util.PriorityQueue;

import Repository.RepositoryGOP;
//import com.amazonaws.services.ec2.model.Instance;

/**
 * Created by pi on 5/21/17.
 */
public class TranscodingVM {

    private PriorityQueue<RepositoryGOP> jobs = new PriorityQueue<RepositoryGOP>();
    //private Instance instance = new Instance();
    private boolean working=false;

    public void TranscodeSegment()
    {
        working=true;
        int i=0;
        while(!jobs.isEmpty()) {
            RepositoryGOP aRepositoryGOP = jobs.poll();
            //System.out.println(aRepositoryGOP.getPath());
            String filename= aRepositoryGOP.getPath().substring(aRepositoryGOP.getPath().lastIndexOf("/")+1, aRepositoryGOP.getPath().length());
            //String[] command = {"ffmpeg", "-i", aRepositoryGOP.getPath(), "-s", "320:240", "-c:a", "copy", "/home/pi/apache-tomcat-7.0.78/webapps/CVSS_Implementation_Interface_war/videos/output/"+(i++) +".mp4"};//jobs.poll().getPath()
            String[] command = {"bash", "./bash/resize.sh", aRepositoryGOP.getPath(),"320","240","./output/",filename};
            //ideally, we should be able to pull setting out from StreamGOP but now use fixed

            try {
                ProcessBuilder pb = new ProcessBuilder(command);
                pb.redirectOutput(ProcessBuilder.Redirect.INHERIT); //debug,make output from bash to screen
                pb.redirectError(ProcessBuilder.Redirect.INHERIT); //debug,make output from bash to screen

                Process p = pb.start();
                p.waitFor();
                //
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            //TODO: what to do after finish each RepositoryGOP ?
            //SendSegmentToVideoMerger();
        }
        working=false;
    }

    private void SendSegmentToVideoMerger()
    {

    }

    //TODO: make this get work from socket instead of direct call
    public void AddJob(RepositoryGOP segment)
    {

        jobs.add(segment);
        //TODO: sort by some criteria, shortest deadline first, highest priority first?
        if(!working){
            //System.out.println("test");
            TranscodeSegment(); //TODO: make this a thread/task, make it asynchronous
        }
    }

}
