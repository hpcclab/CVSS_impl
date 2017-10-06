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


    public void TranscodeSegment()
    {
        //String[] command = {"ffmpeg", "-i", jobs.poll().getPath(), "-s", "320:240", "-c:a", "copy", "/home/pi/apache-tomcat-7.0.78/webapps/CVSS_Implementation_Interface_war/videos/output.mp4"};//jobs.poll().getPath()
        String[] command = {"bash", "../bash/testbash.sh"};
        try{
            ProcessBuilder pb = new ProcessBuilder(command);
            Process p = pb.start();

//            p.waitFor();
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    private void SendSegmentToVideoMerger()
    {

    }

    public void AddJob(GOP segment)
    {
        jobs.add(segment);
    }

}
