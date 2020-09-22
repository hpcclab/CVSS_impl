package ResourceManagement;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.nio.charset.StandardCharsets;
import SessionPkg.TranscodingRequest;
import mainPackage.CVSE;

public class MachineInterface_RabbitMQ extends MachineInterface {

    private String QUEUE_NAME;
    private String FEEDBACKQUEUE_NAME;
    private Channel channel; //RMQ communication channel
    //Channel channel;
    public MachineInterface_RabbitMQ(String vclass, String addr, int port, int inid, boolean iautoschedule,Channel RMQchannel,String initqueue,String myQueueName,String myResponseQueuename){
        super(vclass,port,inid,iautoschedule);
        channel=RMQchannel;
        System.out.println("sending RMQ start");
        QUEUE_NAME=myQueueName;
        FEEDBACKQUEUE_NAME=myResponseQueuename;
        String initmsg=QUEUE_NAME+" "+FEEDBACKQUEUE_NAME;
        try  {
            //send queuename to init channel
            channel.basicPublish("", initqueue, null, initmsg.getBytes(StandardCharsets.UTF_8));
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
        System.out.println("Data update called, do nothing in this mode");
    }
    public boolean sendShutdownmessage(){
        return true;
    }
}
