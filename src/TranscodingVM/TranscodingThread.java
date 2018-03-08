package TranscodingVM;

import Repository.RepositoryGOP;
import Scheduler.ServerConfig;
import Stream.StreamGOP;
//import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import miscTools.Tuple;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TranscodingThread extends Thread{
    public String type;
    public PriorityBlockingQueue<StreamGOP> jobs = new PriorityBlockingQueue<StreamGOP>();
    public ConcurrentHashMap<String, Tuple<Long,Integer>> runtime_report=new ConcurrentHashMap<>(); //setting identifier number, < average, count>
    private HashMap<Integer,Long> FakeDelay=new HashMap<>();
    public int workDone;
    public int deadLineMiss;
    long requiredTime; //TODO: make sure all these are thread safe, maybe block when add new item to the queue
    long synctime=0; //spentTime+requiredTime is imaginary total time to clear the queue
    long realspentTime=0; //realspentTime is spentTime without Syncing
    private Boolean useS3=false;
   //EC2 public AmazonS3 s3;
    public String bucketName;
    public String VM_class;
    private Random r=new Random();
    /* //EC2
    public void addS3(AmazonS3 ns3,String nbucketName){
        this.useS3=true;
        this.s3=ns3;
        this.bucketName=nbucketName;
    }
    */
    private void TranscodeSegment()
    {
        int i=0;
        int exit=0;
        long delay=0;
        long elapsedTime;
        while(true) {
            long savedTime=System.nanoTime()/1000000;
            try {
                StreamGOP aStreamGOP = jobs.poll(1, TimeUnit.MINUTES);
                if(aStreamGOP!=null) {
                    if (aStreamGOP.cmdSet.containsKey("shutdown")) {
                        exit = 1;
                        System.out.println("VM's queue is empty and receiving shutting down command");
                        break;
                    }

                    //random delay, BROKEN for now, userSetting does not exist.

                    if (ServerConfig.addFakeDelay) {
                        int identifier =0; //was aStreamGOP.userSetting.settingIdentifier;
                        if (FakeDelay.containsKey(identifier)) //if we already have delay for this setting randomized,
                            delay = FakeDelay.get(identifier); //use that value
                        else {
                            delay = (int) (Math.random() * 1000); //0-1000 ms
                            FakeDelay.put(identifier, delay);
                        }

                    }else if(ServerConfig.addProfiledDelay) {
                        System.out.println("est="+aStreamGOP.estimatedExecutionTime+" sd:"+aStreamGOP.estimatedExecutionSD);
                        delay=(long) (aStreamGOP.estimatedExecutionTime+aStreamGOP.estimatedExecutionSD*r.nextGaussian());
                    }



                    //System.out.println(aStreamGOP.getPath());
                    String filename = aStreamGOP.getPath().substring(aStreamGOP.getPath().lastIndexOf("/") + 1, aStreamGOP.getPath().length());
                    //extra line for windows below, need test if work with linux
                    filename = filename.substring(filename.lastIndexOf("\\") + 1, filename.length());
                    String outputdir=aStreamGOP.outputDir();;
                    /*
                    if(type.equalsIgnoreCase("EC2")){
                        aStreamGOP.setPath("/home/ec2-user/"+aStreamGOP.getPath());
                        outputdir= ("/home/ec2-user/"+aStreamGOP.userSetting.outputDir());
                    }else{
                        outputdir=aStreamGOP.userSetting.outputDir();
                    }
                    */
                    //System.out.println(filename);

                     //don't process, always do dry mode
                    // String[] command = {"bash", ServerConfig.path + "bash/resize.sh", aStreamGOP.getPath(), aStreamGOP.userSetting.resWidth, aStreamGOP.userSetting.resHeight, outputdir, filename};

                    //ideally, we should be able to pull setting out from StreamGOP but now use fixed

                    //if not dryMode
                    if(!ServerConfig.run_mode.equalsIgnoreCase("dry")) {
                        /* //dry mode, did not fix the processing

                        ProcessBuilder pb = new ProcessBuilder(command);
                        //pb.redirectOutput(ProcessBuilder.Redirect.INHERIT); //debug,make output from bash to screen
                        //pb.redirectError(ProcessBuilder.Redirect.INHERIT); //debug,make output from bash to screen
                        Process p = pb.start();
                        p.waitFor();

                        */

                        System.out.println("finished a segment");
                        //put to S3
                        /* //EC2
                        if (useS3) {
                            File file = new File(aStreamGOP.userSetting.outputDir() + "/" + filename);
                            //System.out.println("from "+"output "+aStreamGOP.userSetting.outputDir()+"/"+filename);
                            //System.out.println("from "+"output "+aStreamGOP.userSetting.outputDir().substring(aStreamGOP.userSetting.outputDir().lastIndexOf("/"),aStreamGOP.userSetting.outputDir().length())+"/"+filename);
                            if (file.exists()) {
                                testpackage.S3Control.PutFile(bucketName, "output" + aStreamGOP.userSetting.outputDir().substring(aStreamGOP.userSetting.outputDir().lastIndexOf("/"), aStreamGOP.userSetting.outputDir().length()) + "/" + filename, file, s3);
                            } else {
                                System.out.println("tried to upload nonexist file");
                            }
                            file.delete();
                        }
                        */
                        if (delay != 0) {
                            sleep(delay);
                        }
                        if(System.currentTimeMillis()>aStreamGOP.getDeadLine()){
                            System.out.println("DEADLINE missed (realmode)"+System.currentTimeMillis()+" "+aStreamGOP.getDeadLine() );
                            deadLineMiss++;
                        }
                        elapsedTime = System.nanoTime()/1000000 - savedTime;
                        synctime+=elapsedTime;
                        realspentTime+=elapsedTime;
                    }else{
                        elapsedTime=delay;
                        System.out.println("delay="+delay);
                        synctime+=elapsedTime;
                        realspentTime+=elapsedTime;
                        if(synctime>aStreamGOP.getDeadLine()){
                            System.out.println("DEADLINE missed (drymode) "+synctime+" "+aStreamGOP.getDeadLine() );
                            deadLineMiss++;
                        }
                    }

                    //it's done, reduce estimationTime
                    this.requiredTime-=aStreamGOP.estimatedExecutionTime;
                    //get RunTime, reduce from nano to millisecond
                    workDone++;

                    //runtime_report.is
                    /*
                    Tuple<Long, Integer> pulled = runtime_report.get(aStreamGOP.userSetting.settingIdentifier);
                    if (pulled == null) {
                        runtime_report.put(aStreamGOP.userSetting.settingIdentifier, new Tuple<Long, Integer>(elapsedTime, 1));
                        //System.out.println("new Tuple "+runtime_report.get(0).x+" "+runtime_report.get(0).y);
                    } else {
                        long newAvg = (pulled.x * pulled.y + elapsedTime) / (pulled.y + 1);
                        runtime_report.replace(aStreamGOP.userSetting.settingIdentifier, new Tuple<Long, Integer>(newAvg, pulled.y + 1));
                        //System.out.println("change Tuple "+runtime_report.get(0).x+" "+runtime_report.get(0).y);
                    }
                    */
                    //
                }else{
                    System.out.println("A thread wait 1 minute without getting any works!");
                    System.out.println("total spentTime= "+realspentTime);
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
