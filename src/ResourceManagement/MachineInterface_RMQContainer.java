package ResourceManagement;

import com.rabbitmq.client.Channel;
import DockerManagement.DockerManager;

public class MachineInterface_RMQContainer extends MachineInterface_RabbitMQ {
    public MachineInterface_RMQContainer(String vclass, String addr, int port, int inid, boolean iautoschedule, Channel RMQchannel, String initqueue, String myQueueName, String myResponseQueuename) {
        super(vclass, addr, port, inid, iautoschedule, RMQchannel, initqueue, myQueueName, myResponseQueuename);
        System.out.println("Try to create Container");
        DockerManager.CreateContainers("5602");
        System.out.println("done creating container");
        //now, contact docker to create a container

    }

}
