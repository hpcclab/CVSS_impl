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
import java.net.ServerSocket;
import java.net.Socket;

public class VMinterface {
    private Socket s;
    private ServerSocket ss;
    public OutputStream os;
    public ObjectOutputStream oos;
    public InputStream is;
    public ObjectInputStream ois;
    Thread connector;
    private int status;
    public int estimatedQueueLength=0;
    public long estimatedExecutionTime=0;
    public int id;

    public VMinterface(String addr,int port,int inid){
        try {
            //s = new Socket(addr, port);
            ss = new ServerSocket(port);
            id=inid;
            //new thread to accept connection
            connector = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        s = ss.accept();
                        ss.close();
                        os=s.getOutputStream();
                        oos = new ObjectOutputStream(os);
                        is = s.getInputStream();
                        ois = new ObjectInputStream(is);
                        status=1;
                    }catch(Exception e) {
                        System.out.println("connector thread Failed: " + e);
                    }
                }
            });
            connector.start();
        }catch(Exception e) {
            System.out.println("Failed: " + e);
        }
    }
    public boolean isWorking(){
        return status==1;
    }

    public boolean sendJob(StreamGOP segment){
        if(isWorking()) {
            estimatedQueueLength++;
            estimatedExecutionTime += segment.estimatedExecutionTime;
            try {
                oos.writeObject(segment);
            } catch (Exception e) {
                System.out.println("sendJob fail:" + e);
                return false;
            }
            return true;
        }
        System.out.println("not working!");
        return false;
    }
    public double dataUpdate(){
        if(isWorking()) {
            try {
                StreamGOP query = new StreamGOP();
                query.command = "query";
                oos.writeObject(query); //they expect an object, thus we need to send object
                report answer = (report) ois.readObject();
                System.out.println("id= " + id + " update queue length data to " + answer.runtime_report);

                System.out.println("id= " + id + " update queue Time data to " + answer.queue_executionTime);
                GOPTaskScheduler.VMinterfaces.get(id).estimatedQueueLength = answer.queue_size;
                GOPTaskScheduler.VMinterfaces.get(id).estimatedExecutionTime = answer.queue_executionTime;
                TimeEstimator.updateTable(this.id, answer.runtime_report);
                VMProvisioner.deadLineMissRate=answer.deadLineMissRate;
                //
                System.out.println("got deadLineMissRate=" + answer.deadLineMissRate);
                return answer.deadLineMissRate;

            } catch (Exception e) {
                System.out.println(e);
                return -1;
            }
        }
            return -1;
        //return 0;
    }
    public boolean sendShutdownmessage(){
        if(isWorking()) {
            try {
                StreamGOP poison = new StreamGOP();
                poison.command = "shutdown";
                poison.setPriority(0);
                oos.writeObject(poison);
            } catch (Exception e) {
                System.out.println(e);
                return false;
            }
            return true;
        }
        return false;
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