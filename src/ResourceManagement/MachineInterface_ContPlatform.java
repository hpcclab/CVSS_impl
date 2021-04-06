package ResourceManagement;

import ProtoMessage.TaskRequest;
import SessionPkg.TranscodingRequest;
import com.rabbitmq.client.Channel;
import mainPackage.CVSE;
import org.apache.commons.lang.ObjectUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/////////////////////// Cold container machine type: this machine type plug in directly to the RemoteDocker
//
//

public class MachineInterface_ContPlatform extends MachineInterface{
    private RemoteDocker Dockerpool;
    private Thread workerThread;
    private String FEEDBACKQUEUE_NAME;
    private Channel channel;
    public List<TranscodingRequest> MQ= Collections.synchronizedList(new ArrayList<>());
    private Runnable runnable= () -> {
        System.out.println("MQ length=" + MQ.size());
        while (MQ.size() > 0) {
            System.out.println("MQ length=" + MQ.size());
            TranscodingRequest awork = MQ.remove(0);
            sendJobAndWait(awork);
        }
        System.out.println("Terminating the thread");
    };

    public MachineInterface_ContPlatform(String vclass, String addr, int port, int inid, boolean iautoschedule,Channel RMQchannel,String myResponseQueuename, RemoteDocker _RD) {
        super(vclass, port, inid, iautoschedule);
        Dockerpool=_RD;
        channel=RMQchannel;
        FEEDBACKQUEUE_NAME=myResponseQueuename;
        workerThread= new Thread(runnable);
    }

    public boolean sendJob(TranscodingRequest segment) {
        estimatedQueueLength++;
        estimatedExecutionTime += segment.EstMean;
        //if no workerthread initiate it
        if(!workerThread.isAlive()) {
            workerThread = new Thread(runnable);
        }
        //add segment to MQ of workerthread
        MQ.add(segment);
        //start the workerThread
        if(!workerThread.isAlive()) {
            System.out.println("Start runningThread");
            workerThread.start();
        }
        return true;
    }

    public boolean sendJobAndWait(TranscodingRequest segment) {

        System.out.println("try sending a segment to cold cont");
        String RepDir="/share_dir/SVSE/sampleRepo/";
        String ExpDir="/share_dir/SVSE/sampleOutput/";
        System.out.println("port="+port);
        String[] ports=new String[1];
        ports[0]=port+"";
        /// Step 1, make sure folder exist

        /// Step 2, parse cmd first
        String[] sourcesplit=segment.DataSource.split("_");
        String vidChoice=sourcesplit[0];
        String segName="video"+sourcesplit[1]+".ts";


        String CMD= "-i "+RepDir+vidChoice+segName+" ";
        String OPstring="";
        int cmdcount=0;
        for (String cmd:segment.listallCMD()){
            for(String param:segment.listparamsofCMD(cmd)) {
                switch(cmd){
                    case "RESOLUTION": OPstring="-vf ";
                        OPstring+= (param.equalsIgnoreCase("0"))?"scale=1920:1080":"scale=640:360";
                        break;
                    case "FRAMERATE": OPstring="-r ";
                        OPstring+= (param.equalsIgnoreCase("0"))?"60":"24";

                        break;
                    case "BITRATE": OPstring="-b:v ";
                        OPstring+= (param.equalsIgnoreCase("0"))?"3.4M":"2.4M";

                        break;
                    case "CODEC": OPstring="-c:v ";
                        OPstring+= (param.equalsIgnoreCase("0"))?"libx264":"libvpx-vp9";
                        break;
                    default:
                        //no specific operation string in default
                        OPstring=param;
                }
                cmdcount++;
            }
        }
        CMD+=OPstring +" "+ExpDir+vidChoice+segName; //no -o needed
        if(cmdcount!=1){
            System.out.println("currently, multiple cmd is not supported");
            return false;
        }
        // Step 3, call ffmpeg container
        System.out.println("cold container will run:"+CMD);
        String ID=Dockerpool.CreateContainers(ports,id,"jrottenberg/ffmpeg:3.4-ubuntu",CMD);
        try{
            Dockerpool.waitContainersStop(ID);
        }catch(Exception E){
            System.out.println("bug in container waiting");
        }
        //Step 4, Report stats to FeedbackQueue
        double timeStamp=100;
        double ExeTime=100;
        try  {
            TaskRequest.TaskReport.Builder Reportbuilder=TaskRequest.TaskReport.newBuilder();
            TaskRequest.TaskReport report=Reportbuilder.setCompletedTaskID(segment.TaskId)
                    .setWorkerNodeID(id)
                    .setExecutionTime(ExeTime)
                    .setTimeStamp(timeStamp)
                    .setTheRequest(segment.buildRequest())
            .build();
        //channel.basicPublish("", FEEDBACKQUEUE_NAME,null,segment.buildRequest().toByteString().toByteArray());
            channel.basicPublish("", FEEDBACKQUEUE_NAME,null, report.toByteString().toByteArray());
            System.out.println("Feedback published");
        }catch (Exception E){
            System.out.println("Rmqbug in sending Feedback "+E);
        }
        //TaskCompletionRecord
        return true;
    }


    public void dataUpdate(){
        elapsedTime=System.currentTimeMillis()- CVSE.GTS.referenceTime;
        System.out.println("Data update called, do nothing in this mode");
    }
    public boolean sendShutdownmessage(){
        return true;
    }

}
