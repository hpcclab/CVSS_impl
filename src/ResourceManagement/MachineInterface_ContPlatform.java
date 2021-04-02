package ResourceManagement;

import SessionPkg.TranscodingRequest;
import com.rabbitmq.client.Channel;
import mainPackage.CVSE;

import java.nio.charset.StandardCharsets;
/////////////////////// Cold container machine type: this machine type plug in directly to the RemoteDocker
// 2 alternate implementations
// synchronous with queue: should be a mix of thread and RMQ
// asynchronous (always just fire a container immediately) -> simpler ?
public class MachineInterface_ContPlatform extends MachineInterface{
    private RemoteDocker Dockerpool;
    public MachineInterface_ContPlatform(String vclass, String addr, int port, int inid, boolean iautoschedule, RemoteDocker _RD) {
        super(vclass, port, inid, iautoschedule);
        Dockerpool=_RD;
    }

    public boolean sendJob(TranscodingRequest segment) {
        String RepDir="/share_dir/SVSE/sampleRepo/";
        String ExpDir="/share_dir/SVSE/sampleOutput/";
        String[] ports=new String[port];
        /// Step 1, make sure folder exist

        /// Step 2, parse cmd first
        String[] sourcesplit=segment.DataSource.split("_");
        String vidChoice=sourcesplit[0];
        String segName="video"+sourcesplit[1]+".ts";


        String CMD= "-i "+RepDir+vidChoice+segName;
        String OPstring="";
        int cmdcount=0;
        for (String cmd:segment.listallCMD()){
            for(String param:segment.listparamsofCMD(cmd)) {
                switch(cmd){
                    case "RESOLUTION": OPstring="-vf";
                        OPstring+= (param.equalsIgnoreCase("0"))?"scale=1920:1080":"scale=640:360";
                        break;
                    case "FRAMERATE": OPstring="-r";
                        OPstring+= (param.equalsIgnoreCase("0"))?"60":"24";

                        break;
                    case "BITRATE": OPstring="-b:v";
                        OPstring+= (param.equalsIgnoreCase("0"))?"3.4M":"2.4M";

                        break;
                    case "CODEC": OPstring="-c:v";
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
        String ID=Dockerpool.CreateContainers(ports,id,"jrottenberg/ffmpeg",CMD); //CMD is not "/home/PythonWorker/FrontConnector.py"
        try{
            Dockerpool.waitContainersStop(ID);
        }catch(Exception E){
            System.out.println("bug in container waiting");
        }
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
