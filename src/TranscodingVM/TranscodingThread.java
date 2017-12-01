package TranscodingVM;

import Repository.RepositoryGOP;
import Scheduler.ServerConfig;
import Stream.StreamGOP;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import miscTools.Tuple;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TranscodingThread extends Thread{
    public PriorityBlockingQueue<StreamGOP> jobs = new PriorityBlockingQueue<StreamGOP>();
    public ConcurrentHashMap<Integer, Tuple<Long,Integer>> runtime_report=new ConcurrentHashMap<>(); //setting identifier number, < average, count>
    private HashMap<Integer,Integer> FakeDelay=new HashMap<>();
    public int workDone;
    public int deadLineMiss;
    long requiredTime; //TODO: make sure all these are thread safe, maybe block when add new item to the queue
    Boolean useS3=false;
    AmazonS3Client s3;
    String bucketName;
    private void TranscodeSegment()
    {
        int i=0;
        int exit=0;
        int delay=0;

        while(true) {
            long savedTime=System.nanoTime()/1000000;

            try {
                StreamGOP aStreamGOP = jobs.poll(1, TimeUnit.MINUTES);
                if(aStreamGOP!=null) {
                    if (aStreamGOP.command.equalsIgnoreCase("shutdown")) {
                        exit = 1;
                        System.out.println("VM's queue is empty and receiving shutting down command");
                        break;
                    }

                    //random delay
                    if (ServerConfig.addFakeDelay) {
                        if (FakeDelay.containsKey(aStreamGOP.userSetting.settingIdentifier)) //if we already have delay for this setting randomized,
                            delay = FakeDelay.get(aStreamGOP.userSetting.settingIdentifier); //use that value
                        else {
                            delay = (int) (Math.random() * 1000); //0-1000 ms
                            FakeDelay.put(aStreamGOP.userSetting.settingIdentifier, delay);
                        }
                    }

                    //System.out.println(aStreamGOP.getPath());
                    String filename = aStreamGOP.getPath().substring(aStreamGOP.getPath().lastIndexOf("/") + 1, aStreamGOP.getPath().length());
                    //String[] command = {"ffmpeg", "-i", aStreamGOP.getPath(), "-s", "320:240", "-c:a", "copy", "/home/pi/apache-tomcat-7.0.78/webapps/CVSS_Implementation_Interface_war/videos/output/"+(i++) +".mp4"};//jobs.poll().getPath()
                    String[] command = {"bash", ServerConfig.path + "bash/resize.sh", aStreamGOP.getPath(), aStreamGOP.userSetting.resWidth, aStreamGOP.userSetting.resHeight, aStreamGOP.userSetting.outputDir(), filename};
                    //ideally, we should be able to pull setting out from StreamGOP but now use fixed

                    ProcessBuilder pb = new ProcessBuilder(command);
                    pb.redirectOutput(ProcessBuilder.Redirect.INHERIT); //debug,make output from bash to screen
                    pb.redirectError(ProcessBuilder.Redirect.INHERIT); //debug,make output from bash to screen

                    Process p = pb.start();
                    p.waitFor();

                    //put to S3
                    if(useS3){
                        File file = new File(aStreamGOP.userSetting.outputDir());
                        testpackage.S3Control.PutFile(bucketName, filename, file, s3);
                    }
                    if (delay != 0) {
                        sleep(delay);
                    }
                    //it's done, reduce estimationTime
                    this.requiredTime-=aStreamGOP.estimatedExecutionTime;
                    //get RunTime, reduce from nano to millisecond
                    long elapsedTime = System.nanoTime()/1000000 - savedTime;
                    workDone++;
                    if(System.currentTimeMillis()>aStreamGOP.deadLine){
                        System.out.println("DEADLINE missed "+System.currentTimeMillis()+" "+aStreamGOP.deadLine );
                        deadLineMiss++;
                    }
                    //runtime_report.is
                    Tuple<Long, Integer> pulled = runtime_report.get(aStreamGOP.userSetting.settingIdentifier);
                    if (pulled == null) {
                        runtime_report.put(aStreamGOP.userSetting.settingIdentifier, new Tuple<Long, Integer>(elapsedTime, 1));
                        //System.out.println("new Tuple "+runtime_report.get(0).x+" "+runtime_report.get(0).y);
                    } else {
                        long newAvg = (pulled.x * pulled.y + elapsedTime) / (pulled.y + 1);
                        runtime_report.replace(aStreamGOP.userSetting.settingIdentifier, new Tuple<Long, Integer>(newAvg, pulled.y + 1));
                        //System.out.println("change Tuple "+runtime_report.get(0).x+" "+runtime_report.get(0).y);
                    }

                    //TODO: see if it miss deadline

                    //
                }else{
                    System.out.println("A thread wait 1 minute without getting any works!");
                }
            } catch (Exception e) {
                System.out.println("Thread Error:" +e.getMessage());
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
