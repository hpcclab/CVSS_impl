package ResourceManagement;

import com.rabbitmq.client.Channel;

public class MachineInterface_RMQContainer extends MachineInterface_RabbitMQ {
    public MachineInterface_RMQContainer(String vclass, String addr, int port, int inid, boolean iautoschedule, Channel RMQchannel, String initqueue, String myQueueName, String myResponseQueuename,RemoteDocker Dockerpool) {
        super(vclass, addr, port, inid, iautoschedule, RMQchannel, initqueue, myQueueName, myResponseQueuename);
        System.out.println("Try to create Container");
        String[] ports=new String[port];
        Dockerpool.CreateContainers(ports,inid,"testworkerthread");
        System.out.println("done creating container");
        //now, contact docker to create a container
    }

}
