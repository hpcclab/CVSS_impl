package ResourceManagement;

import com.rabbitmq.client.Channel;

public class MachineInterface_RMQContainer extends MachineInterface_RabbitMQ {

    public MachineInterface_RMQContainer(String vclass, String addr, int port, int inid, boolean iautoschedule, Channel RMQchannel, String initqueue, String myQueueName, String myResponseQueuename,RemoteDocker Dockerpool) {
        super(vclass.split("-")[0], addr, port, inid, iautoschedule, RMQchannel, initqueue, myQueueName, myResponseQueuename);
        System.out.println("Try to create Container");
        System.out.println("port="+port);

        String[] ports={Integer.toString(port)};
        //System.out.println("ports="+ports);
            Dockerpool.CreateContainers(ports, inid, GetImageName(vclass), "python3", "/home/PythonWorker/FrontConnector.py");

        System.out.println("done creating container");
        //now, contact docker to create a container
    }
    //may overwrite/modify later
    public String GetImageName(String vclass){
        System.out.println("use vclass="+"testworkerthread");
        return "testworkerthread";
    }
}
