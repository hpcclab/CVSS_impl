package ResourceManagement;

import SessionPkg.TranscodingRequest;
import com.rabbitmq.client.Channel;
import mainPackage.CVSE;

import java.nio.charset.StandardCharsets;
/////////////////////// Cold container machine type: this machine type plug in directly to the RemoteDocker
// 2 alternate implementations
// synchronous with queue: should be a mix of thread and RMQ
// asynchronous (always just fire a container immediately) -> simpler ?
public class MachineInterface_ColdContainer extends MachineInterface{
    private RemoteDocker RD;
    public MachineInterface_ColdContainer(String vclass, String addr, int port, int inid, boolean iautoschedule, RemoteDocker _RD) {
        super(vclass, port, inid, iautoschedule);
        RD=_RD;
    }

    public boolean sendJob(TranscodingRequest segment) {
        //CreateContainers(String[] ports,int nodeID,String imageName)
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
