package ResourceManagement;

import ProtoMessage.TaskRequest;
import SessionPkg.TranscodingRequest;
import mainPackage.CVSE;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import static java.lang.Thread.sleep;

public class MachineInterface_SocketIO extends MachineInterface {
    private Socket s;
    //private ServerSocket ss;
    public ObjectOutputStream oos=null;
    public ObjectInputStream ois=null;
    Thread connector;

    public MachineInterface_SocketIO(String vclass, String addr, int port, int inid, boolean iautoschedule){
        super(vclass,port,inid,iautoschedule);

        while (status != 1) {
            System.out.println("connecting");
            try {
                System.out.println("connect to :"+addr+" "+port);
                s = new Socket(addr, port);
                while(!s.isConnected()){
                    System.out.println("socket is not connected");
                    sleep(2000);
                }
                sleep(1000);
                System.out.println("now create pipe");
                oos = new ObjectOutputStream(s.getOutputStream());
                oos.flush();
                oos.reset();
                sleep(1000);
                System.out.println("now create input pipe");
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

    public boolean sendJob(TranscodingRequest segment){
        if(isWorking()) {

            //convert path if needed
            // if(!CVSE.config.CR_type.get(id).equalsIgnoreCase("thread")){
            // System.out.println("convert!");
            //segment.setPath(segment.getPath().replaceAll("\\\\","/"));
            // }

            estimatedQueueLength++;
            estimatedExecutionTime += segment.EstMean;
            try {
                oos.writeObject(segment.buildRequest());
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
                TaskRequest.Operation.Builder Obuilder=TaskRequest.Operation.newBuilder();
                TaskRequest.ServiceRequest.Builder ReBuilder=TaskRequest.ServiceRequest.newBuilder();
                TaskRequest.ServiceRequest query=ReBuilder.addOPlist(Obuilder.setCmd("query"))
                        .setPriority(0)
                        .setGlobalDeadline(CVSE.GTS.maxElapsedTime)
                        .build();
                oos.writeObject(query); //they expect an object, thus we need to send object
                TaskRequest.WorkerReport answer = (TaskRequest.WorkerReport) ois.readObject();
                //System.out.println("id= " + id + " update queue length data to " + answer.runtime_report);
                //System.out.println("id= " + id + " update queue Time data to " + answer.queue_executionTime);
                CVSE.GTS.workpending-=(estimatedQueueLength-answer.getQueueSize());
                CVSE.GTS.machineInterfaces.get(id).estimatedQueueLength = answer.getQueueSize();
                CVSE.GTS.machineInterfaces.get(id).estimatedExecutionTime = answer.getQueueExecutionTime();
                CVSE.GTS.machineInterfaces.get(id).elapsedTime=answer.getVMelapsedTime();
                CVSE.GTS.machineInterfaces.get(id).actualSpentTime=answer.getVMWorkTime();
                //TimeEstimator.updateTable(this.id, answer.runtime_report); //disable for now, broken
                CVSE.GTS.machineInterfaces.get(id).total_taskmiss =answer.getDlMissed();
                CVSE.GTS.machineInterfaces.get(id).total_taskdone =answer.getOntimeCompletion();
                //CVSE.VMP.ackCompletedVideo(answer.getCompletedTaskIDList());
                //completedTask.clear();
                //

                    System.out.println("got deadLineMissRate=" + CVSE.GTS.machineInterfaces.get(id).total_taskdone/CVSE.GTS.machineInterfaces.get(id).total_taskmiss);

            } catch (Exception e) {
                System.out.println("data update error:"+e);
            }
        }
    }
    public boolean sendShutdownmessage(){
        if(isWorking()) {
            try {
                TaskRequest.Operation.Builder Obuilder=TaskRequest.Operation.newBuilder();
                TaskRequest.ServiceRequest.Builder ReBuilder=TaskRequest.ServiceRequest.newBuilder();
                TaskRequest.ServiceRequest poison=ReBuilder.addOPlist(Obuilder.setCmd("shutdown"))
                        .setPriority(0)
                        .setTaskID(-1)
                        .build();
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