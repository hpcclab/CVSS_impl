package ResourceManagement;

import Streampkg.StreamGOP;
import TranscodingVM.runtime_report;
import mainPackage.CVSE;

import java.io.*;
import java.net.Socket;

import static java.lang.Thread.sleep;

public class MachineInterface_SocketIO extends MachineInterface {
    private Socket s;
    //private ServerSocket ss;
    public ObjectOutputStream oos=null;
    public ObjectInputStream ois=null;
    Thread connector;

    public MachineInterface_SocketIO(CVSE cvse,String vclass, String addr, int port, int inid, boolean iautoschedule){
        super(cvse,vclass,port,inid,iautoschedule);

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
    public void dataUpdate(){
        if(isWorking()) {
            try {
                StreamGOP query = new StreamGOP();

                    query.cmdSet.put("query", null);
                    query.dispatched=true;
                    query.deadLine=_CVSE.GTS.maxElapsedTime;
                oos.writeObject(query); //they expect an object, thus we need to send object
                runtime_report answer = (runtime_report) ois.readObject();
                //System.out.println("id= " + id + " update queue length data to " + answer.runtime_report);
                //System.out.println("id= " + id + " update queue Time data to " + answer.queue_executionTime);
                _CVSE.GTS.workpending-=(estimatedQueueLength-answer.queue_size);
                _CVSE.GTS.machineInterfaces.get(id).estimatedQueueLength = answer.queue_size;
                _CVSE.GTS.machineInterfaces.get(id).estimatedExecutionTime = answer.queue_executionTime;
                _CVSE.GTS.machineInterfaces.get(id).elapsedTime=answer.VMelapsedTime;
                _CVSE.GTS.machineInterfaces.get(id).actualSpentTime=answer.VMspentTime;
                //TimeEstimator.updateTable(this.id, answer.runtime_report); //disable for now, broken
                _CVSE.GTS.machineInterfaces.get(id).total_itemmiss =answer.missed;
                _CVSE.GTS.machineInterfaces.get(id).total_itemdone =answer.workdone;
                _CVSE.GTS.machineInterfaces.get(id).total_taskdone =answer.Nworkdone;
                _CVSE.GTS.machineInterfaces.get(id).total_taskmiss =answer.Nmissed;
                _CVSE.VMP.ackCompletedVideo(answer.completedTask);
                //completedTask.clear();
                //

                    System.out.println("got deadLineMissRate=" + answer.deadLineMissRate);

            } catch (Exception e) {
                System.out.println("data update error:"+e);
            }
        }
    }
    public boolean sendShutdownmessage(){
        if(isWorking()) {
            try {
                StreamGOP poison = new StreamGOP();
                poison.cmdSet.put("shutdown",null);
                poison.setPriority(0);
                oos.writeObject(poison);
            } catch (Exception e) {
                System.out.println("SocketIO error"+ e);
                return false;
            }
            return true;
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