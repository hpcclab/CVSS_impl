package TranscodingVM;

import Repository.RepositoryGOP;
import Scheduler.GOPTaskScheduler;
import Scheduler.ServerConfig;
import Scheduler.TimeEstimator;
import Stream.StreamGOP;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class VMinterface {
    private Socket s;
    public OutputStream os;
    public ObjectOutputStream oos;
    public InputStream is;
    public ObjectInputStream ois;
    private int status;
    public int estimatedQueueLength=0;
    public long estimatedExecutionTime=0;
    public int id;

    public VMinterface(String addr,int port,int id){
        try {
            s = new Socket(addr, port);
            os=s.getOutputStream();
            oos = new ObjectOutputStream(os);
            is = s.getInputStream();
            ois = new ObjectInputStream(is);
            status=1;
            this.id=id;
        }catch(Exception e) {
            System.out.println("Failed: " + e);
        }
    }
    public boolean isWorking(){
        return status==1;
    }

    public boolean sendJob(StreamGOP segment){
        estimatedQueueLength++;
        estimatedExecutionTime+=segment.estimatedExecutionTime;
        try {
            oos.writeObject(segment);
        }catch(Exception e){
            System.out.println("sendJob fail:"+e);
            return false;
        }
        return true;
    }
    public boolean dataUpdate(){
        try {
            StreamGOP query = new StreamGOP();
            query.command = "query";
            oos.writeObject(query); //they expect an object, thus we need to send object
            report answer= (report)ois.readObject();
            System.out.println("id= " +id+" update queue length data to "+answer.runtime_report);

            System.out.println("id= " +id+" update queue Time data to "+answer.queue_executionTime);
            GOPTaskScheduler.VMinterfaces.get(id).estimatedQueueLength=answer.queue_size;
            GOPTaskScheduler.VMinterfaces.get(id).estimatedExecutionTime=answer.queue_executionTime;
            TimeEstimator.updateTable(this.id,answer.runtime_report);
            //

            return true;

        }catch(Exception e){
            System.out.println(e);
            return false;
        }
        //return 0;
    }
    public boolean sendShutdownmessage(){
        try {
            StreamGOP poison=new StreamGOP();
            poison.command="shutdown";
            poison.setPriority(0);
            oos.writeObject(poison);
        }catch(Exception e){
            System.out.println(e);
            return false;
        }
        return true;
    }
    public void close(){
        try {
            ois.close();
            oos.close();
            s.close();
        }catch(Exception e) {
            System.out.println("Failed: " + e);
        }
    }
}