package ResourceManagement;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.nio.charset.StandardCharsets;
import SessionPkg.TranscodingRequest;
import mainPackage.CVSE;

public class MachineInterface_RabbitMQ extends MachineInterface {

    private static String QUEUE_NAME = "mq0";
    private Channel channel; //RMQ communication channel
    //Channel channel;
    public MachineInterface_RabbitMQ(String vclass, String addr, int port, int inid, boolean iautoschedule,Channel RMQchannel){
        super(vclass,port,inid,iautoschedule);
        channel=RMQchannel;
        System.out.println("sending RMQ start");

        try  {
            //channel = CVSE.VMP.connection.createChannel();

            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            //String message = "Connecting message";
            //channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            //System.out.println(" [x] Sent '" + message + "'");
            status=1; //mark this machine as READY
        }catch (Exception E){
            System.out.println("Rmqbug in initializing "+E);
        }
    }
    public boolean sendJob(TranscodingRequest segment){

        System.out.println("SendJob called");
        estimatedQueueLength+=1;
        estimatedExecutionTime+=segment.EstMean;
        //

        ///////// normalize the parameters for real mode ?
        //Global Deadline
        //???
        //Parameter option?

        //
        if(isWorking()) {
            String message = "SendJob";

            try  {
                //channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
                channel.basicPublish("", QUEUE_NAME,null,segment.buildRequest().toByteString().toByteArray());
                System.out.println("Msg Sent");
                return true;
            }catch (Exception E){
                System.out.println("Rmqbug in sendJob");
            }
        }
        return false;
    }
    public void addOperation(Operations.simpleoperation newOP){

    } //introduce new operation to the system, interface for future feature, do nothing for now
    public void dataUpdate(){
        System.out.println("Data update called");
//        if(isWorking()) {
//            String message = "DataUpdate";
//            try  {
//                channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
//                System.out.println(" [x] Sent '" + message + "'");
//            }catch (Exception E){
//                System.out.println("Rmqbug in DataUpdate");
//            }
//        }
        //fake data update
        CVSE.GTS.workpending-=1;
        CVSE.GTS.machineInterfaces.get(id).estimatedQueueLength=0;
        CVSE.GTS.machineInterfaces.get(id).elapsedTime+=200;
    }
    public boolean sendShutdownmessage(){
        return true;
    }
}
