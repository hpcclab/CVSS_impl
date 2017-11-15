package TranscodingVM;

import Repository.RepositoryGOP;
import Scheduler.ServerConfig;
import Stream.StreamGOP;
import miscTools.Tuple;

import java.util.HashMap;
import java.util.PriorityQueue;

public class TranscodingThread extends Thread{
    public PriorityQueue<StreamGOP> jobs = new PriorityQueue<StreamGOP>();
    public HashMap<Integer, Tuple<Long,Integer>> runtime_report=new HashMap<>(); //setting identifier number, < average, count>
    private HashMap<Integer,Integer> FakeDelay=new HashMap<>();
    private void TranscodeSegment()
    {
        int i=0;
        int exit=0;
        int delay=0;

        while(!jobs.isEmpty()) {
            long savedTime=System.nanoTime();
            StreamGOP aRepositoryGOP = jobs.poll();
            if(aRepositoryGOP.setting.equalsIgnoreCase("shutdown")){
                exit=1;
                System.out.println("VM's queue is empty and receiving shutting down command");
                break;
            }

            //random delay
            if(ServerConfig.addFakeDelay){
                if(FakeDelay.containsKey(aRepositoryGOP.userSetting.settingIdentifier)) //if we already have delay for this setting randomized,
                    delay=FakeDelay.get(aRepositoryGOP.userSetting.settingIdentifier); //use that value
                else{
                    delay=(int)(Math.random()*100); //0-100 ms
                    FakeDelay.put(aRepositoryGOP.userSetting.settingIdentifier,delay);
                }
            }

            //System.out.println(aRepositoryGOP.getPath());
            String filename= aRepositoryGOP.getPath().substring(aRepositoryGOP.getPath().lastIndexOf("/")+1, aRepositoryGOP.getPath().length());
            //String[] command = {"ffmpeg", "-i", aRepositoryGOP.getPath(), "-s", "320:240", "-c:a", "copy", "/home/pi/apache-tomcat-7.0.78/webapps/CVSS_Implementation_Interface_war/videos/output/"+(i++) +".mp4"};//jobs.poll().getPath()
            String[] command = {"bash",ServerConfig.path + "bash/resize.sh", aRepositoryGOP.getPath(),aRepositoryGOP.userSetting.resWidth,aRepositoryGOP.userSetting.resHeight,aRepositoryGOP.userSetting.outputDir(),filename};
            //ideally, we should be able to pull setting out from StreamGOP but now use fixed

            try {
                ProcessBuilder pb = new ProcessBuilder(command);
                pb.redirectOutput(ProcessBuilder.Redirect.INHERIT); //debug,make output from bash to screen
                pb.redirectError(ProcessBuilder.Redirect.INHERIT); //debug,make output from bash to screen

                Process p = pb.start();
                p.waitFor();
                if(delay!=0){
                    sleep(delay);
                }
                //get RunTime
                long elapsedTime=System.nanoTime()-savedTime;
                Tuple<Long,Integer> pulled=runtime_report.get(aRepositoryGOP.userSetting.settingIdentifier);
                if(pulled==null){
                    runtime_report.put(aRepositoryGOP.userSetting.settingIdentifier,new Tuple<Long,Integer>(elapsedTime,1));
                }else{
                    long newAvg=(pulled.x*pulled.y+elapsedTime)/(pulled.y+1);
                    runtime_report.put(aRepositoryGOP.userSetting.settingIdentifier,new Tuple<Long,Integer>(newAvg,pulled.y+1));
                }

                //TODO: see if it miss deadline

                //

                //wait, so the thread never go down
                jobs.wait();
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
