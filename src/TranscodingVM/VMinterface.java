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

import static java.lang.Thread.sleep;

public class VMinterface {
    private Socket s;
    //private ServerSocket ss;
    public ObjectOutputStream oos=null;
    public ObjectInputStream ois=null;
    Thread connector;
    private int status;
    public int estimatedQueueLength=0;
    public long estimatedExecutionTime=0;
    public int id;

    public VMinterface(String addr,int port,int inid){
       // try {
            //s = new Socket(addr, port);

            id=inid;
            //new thread to accept connection
            //connector = new Thread(new Runnable() {
                //@Override
                //public void run() {
                    while (status != 1) {
                        System.out.println("connecting");
                        try {
                            System.out.println("connect to :"+addr+" "+port);
                            s = new Socket(addr, port);
                            while(!s.isConnected()){
                                System.out.println("socket is not connected");
                                sleep(3000);
                            }
                            oos = new ObjectOutputStream(s.getOutputStream());
                            oos.flush();
                            oos.reset();
                            //sleep(2000);
                            ois = new ObjectInputStream(s.getInputStream());
                            status = 1;
                            System.out.println("succesfully set status=1");
                        } catch (Exception e) {
                            System.out.println("connector Failed: " + e);
                        }
                    }


             //   }
          //  });
           // connector.start();
       // }catch(Exception e) {
       //     System.out.println("Failed: " + e);
       // }
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