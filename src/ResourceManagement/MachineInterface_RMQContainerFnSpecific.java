package ResourceManagement;

import com.rabbitmq.client.Channel;

public class MachineInterface_RMQContainerFnSpecific extends MachineInterface_RMQContainer{
    public MachineInterface_RMQContainerFnSpecific(String vclass, String addr, int port, int inid, boolean iautoschedule, Channel RMQchannel, String initqueue, String myQueueName, String myResponseQueuename, RemoteDocker Dockerpool){
        super(vclass.split("-")[0], addr, port, inid, iautoschedule, RMQchannel, initqueue, myQueueName, myResponseQueuename,Dockerpool);
        if(vclass.contains("-")){
            properties.put("myFn",vclass.split("-")[1]);
        }else{
            System.out.println("Error: What is my specific function???");
        }
    }

}
