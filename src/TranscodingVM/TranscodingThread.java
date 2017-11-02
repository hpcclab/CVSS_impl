package TranscodingVM;

import Repository.RepositoryGOP;
import Stream.StreamGOP;

import java.util.PriorityQueue;

public class TranscodingThread extends Thread{
    public PriorityQueue<StreamGOP> jobs = new PriorityQueue<StreamGOP>();

    private void TranscodeSegment()
    {
        int i=0;
        int exit=0;
        while(!jobs.isEmpty()) {
            StreamGOP aRepositoryGOP = jobs.poll();
            if(aRepositoryGOP.setting.equalsIgnoreCase("shutdown")){
                exit=1;
                System.out.println("VM's queue is empty and receiving shutting down command");
                break;
            }
            //System.out.println(aRepositoryGOP.getPath());
            String filename= aRepositoryGOP.getPath().substring(aRepositoryGOP.getPath().lastIndexOf("/")+1, aRepositoryGOP.getPath().length());
            //String[] command = {"ffmpeg", "-i", aRepositoryGOP.getPath(), "-s", "320:240", "-c:a", "copy", "/home/pi/apache-tomcat-7.0.78/webapps/CVSS_Implementation_Interface_war/videos/output/"+(i++) +".mp4"};//jobs.poll().getPath()
            String[] command = {"bash", "./bash/resize.sh", aRepositoryGOP.getPath(),aRepositoryGOP.userSetting.resWidth,aRepositoryGOP.userSetting.resHeight,aRepositoryGOP.userSetting.outputDir(),filename};
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
        }
        if(exit!=0){
            //receive exit command, now what?
        }
    }

    public void run(){
        TranscodeSegment();
    }
}
