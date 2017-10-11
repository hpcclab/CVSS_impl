package TranscodingVM;

import java.util.LinkedList;

import GOP.GOP;
//import com.amazonaws.services.ec2.model.Instance;

/**
 * Created by pi on 5/21/17.
 */
public class TranscodingVM {

//    private Queue<GOP> jobs;//queue abstract, LinkedList can be used as a queue though

    private LinkedList<GOP> jobs = new LinkedList<GOP>();
    //private Instance instance = new Instance();
    private boolean working=false;
    private int worktodo=0; // this might later be visible by other scheduler

    public void TranscodeSegment()
    {
        working=true;
        while(!jobs.isEmpty()) {
            GOP aGOP = jobs.poll();
            // where is the command for this specific GOP? , each GOP in the list may have different command, right?
            //String[] command = {"ffmpeg", "-i", jobs.poll().getPath(), "-s", "320:240", "-c:a", "copy", "/home/pi/apache-tomcat-7.0.78/webapps/CVSS_Implementation_Interface_war/videos/output.mp4"};//jobs.poll().getPath()
            String[] command = {"bash", "../bash/testbash.sh"};
            try {
                ProcessBuilder pb = new ProcessBuilder(command);
                Process p = pb.start();
                //p.waitFor();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            worktodo--;

            //TODO: what to do after finish each GOP ?
            //SendSegmentToVideoMerger();
        }
        working=false;
    }

    private void SendSegmentToVideoMerger()
    {

    }

    //TODO: make this get work from socket instead of direct call
    public void AddJob(GOP segment)
    {

        jobs.add(segment);
        worktodo++;
        //TODO: sort by some criteria, shortest deadline first, highest priority first?
        if(!working){
            TranscodeSegment(); //TODO: make this a thread/task ?
        }
    }

}
