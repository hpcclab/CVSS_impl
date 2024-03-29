package ResourceManagement;

import ProtoMessage.TaskRequest;
import SessionPkg.TranscodingRequest;
import mainPackage.CVSE;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import static java.lang.Thread.sleep;

public class MachineInterface_JavaSocket extends MachineInterface {
    private Socket s;
    //private ServerSocket ss;
    public ObjectOutputStream oos=null;
    public ObjectInputStream ois=null;
    Thread connector;

    public MachineInterface_JavaSocket(String vclass, String addr, int port, int inid, boolean iautoschedule){
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
            System.out.println("sending a request");
            try {
                //oos.writeObject(segment.buildRequest()+"\n");
                segment.buildRequest().writeDelimitedTo(oos);
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
        //need update to RabbitMQ version
//        if(isWorking()) {
//            try {
//                TaskRequest.Operation.Builder Obuilder=TaskRequest.Operation.newBuilder();
//                TaskRequest.ServiceRequest.Builder ReBuilder=TaskRequest.ServiceRequest.newBuilder();
//                TaskRequest.ServiceRequest query=ReBuilder.addOPlist(Obuilder.setCmd("query"))
//                        .setPriority(0)
//                        .setGlobalDeadline(CVSE.GTS.maxElapsedTime)
//                        .build();
//                //System.out.println("sending query msg");
//                query.writeDelimitedTo(oos);
//                oos.flush();
//                //oos.writeObject(query.toByteArray());
//                //System.out.println("wait reply");
//                TaskRequest.WorkerReport answer = TaskRequest.WorkerReport.parseDelimitedFrom(ois);
//
//                //System.out.println("get reply");
//                //System.out.println("id= " + id + " update queue length data to " + answer.runtime_report);
//                //System.out.println("id= " + id + " update queue Time data to " + answer.queue_executionTime);
//                CVSE.GTS.workpending-=(estimatedQueueLength-answer.getQueueSize());
//                CVSE.GTS.machineInterfaces.get(id).estimatedQueueLength = answer.getQueueSize();
//                CVSE.GTS.machineInterfaces.get(id).estimatedExecutionTime = answer.getQueueExecutionTime();
//                CVSE.GTS.machineInterfaces.get(id).elapsedTime=answer.getVMelapsedTime();
//                CVSE.GTS.machineInterfaces.get(id).actualSpentTime=answer.getVMWorkTime();
//                //TimeEstimator.updateTable(this.id, answer.runtime_report); //disable for now, broken
//                CVSE.GTS.machineInterfaces.get(id).total_taskmiss =answer.getDlMissed();
//                CVSE.GTS.machineInterfaces.get(id).total_taskdone =answer.getOntimeCompletion();
//                //CVSE.RP.ackCompletedVideo(answer.getCompletedTaskIDList());
//                //completedTask.clear();
//                //
//                double missrate=0;
//                if(CVSE.GTS.machineInterfaces.get(id).total_taskdone>0){
//                    missrate=CVSE.GTS.machineInterfaces.get(id).total_taskmiss/CVSE.GTS.machineInterfaces.get(id).total_taskdone;
//                }
//                System.out.println("got deadLineMissRate=" + missrate);
//
//            } catch (Exception e) {
//                System.out.println("data update error:"+e);
//            }
//        }
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
                System.out.println("sending shutdown msg");
                poison.writeTo(oos);
                oos.flush();
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