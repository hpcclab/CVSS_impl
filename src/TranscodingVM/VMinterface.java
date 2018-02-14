package TranscodingVM;

import Repository.RepositoryGOP;
import Scheduler.GOPTaskScheduler;
import Scheduler.ServerConfig;
import Scheduler.TimeEstimator;
import Stream.StreamGOP;

import java.io.*;
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
    public long elapsedTime=0;

    public int id;
    public String VM_class;
    public VMinterface(String vclass,String addr,int port,int inid){
       // try {
            //s = new Socket(addr, port);
            this.VM_class=vclass;
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
                                sleep(1000);
                            }
                            oos = new ObjectOutputStream(s.getOutputStream());
                            oos.flush();
                            oos.reset();
                            sleep(2000);
                            System.out.println("2");
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

            //convert path if needed
               // if(!ServerConfig.VM_type.get(id).equalsIgnoreCase("thread")){
                   // System.out.println("convert!");
                    segment.setPath(segment.getPath().replaceAll("\\\\","/"));
               // }

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
    public double dataUpdate(boolean full){
        if(isWorking()) {
            try {
                StreamGOP query = new StreamGOP();
                if(full){
                    query.cmdSet.put("fullstat", null);
                }else {
                    query.cmdSet.put("query", null);
                }
                oos.writeObject(query); //they expect an object, thus we need to send object
                report answer = (report) ois.readObject();
                //System.out.println("id= " + id + " update queue length data to " + answer.runtime_report);
                //System.out.println("id= " + id + " update queue Time data to " + answer.queue_executionTime);
                GOPTaskScheduler.workpending-=(estimatedQueueLength-answer.queue_size);
                GOPTaskScheduler.VMinterfaces.get(id).estimatedQueueLength = answer.queue_size;
                GOPTaskScheduler.VMinterfaces.get(id).estimatedExecutionTime = answer.queue_executionTime;
                GOPTaskScheduler.VMinterfaces.get(id).elapsedTime=answer.VMelapsedTime;
                //TimeEstimator.updateTable(this.id, answer.runtime_report); //disable for now, broken
                VMProvisioner.deadLineMissRate=answer.deadLineMissRate;
                //
                if(full) {
                    System.out.println("got deadLineMissRate=" + answer.deadLineMissRate);
                }
                return answer.deadLineMissRate;
            } catch (Exception e) {
                System.out.println("data update error:"+e);
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
                poison.cmdSet.put("shutdown",null);
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