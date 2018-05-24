package VMManagement;

import Scheduler.GOPTaskScheduler;
import Scheduler.ServerConfig;
import Streampkg.StreamGOP;

import java.util.Random;

//does not actually have a socket, this code is the combination of transcodingVM, transcodingthread and interface
//Warning: this mode is immediate complete but the estimatedQueuelength and estimatedExecutionTime still needs to be there for compatibility
public class VMinterface_SimLocal extends VMinterface {
    //interface's parameters
    //pseudo thread's parameters
    private int workDone; //count each work as one
    private int NworkDone; //count each work as suggested in StreamGOP.requestcount
    private int deadlineMiss,NdeadlineMiss;
    private long synctime=0; //spentTime+requiredTime is imaginary total time to clear the queue
    private long realspentTime=0; //realspentTime is spentTime without Syncing
    private Random r=new Random();

    public VMinterface_SimLocal(String vclass, int inid) {
        super(vclass,inid);
        status=1;
    }

    public boolean isWorking(){
        return status==1;
    }
    //push in the data
    public  boolean sendJob(StreamGOP segment){
        segment.setPath(segment.getPath().replaceAll("\\\\","/"));
        estimatedQueueLength++;
        estimatedExecutionTime += segment.estimatedExecutionTime;
        //simulate time
        double delay=0;
        if(ServerConfig.addProfiledDelay) {
            //System.out.println("est="+segment.estimatedExecutionTime+" sd:"+segment.estimatedExecutionSD);
            delay=(long) (segment.estimatedExecutionTime+segment.estimatedExecutionSD*r.nextGaussian());
        }
        System.out.println("delay="+delay);
        synctime+=delay;
        realspentTime+=delay;
        //System.out.println("synctime="+synctime);
        //System.out.println("realspentTime="+realspentTime);
        boolean missed=false;
        for (String cmd:segment.deadlineSet.keySet()){
            if(segment.getdeadlineof(cmd)<=synctime){
                if(!missed) {
                    deadlineMiss++;
                    missed=true;
                }
                NdeadlineMiss++;
            }
        }
        workDone++;
        NworkDone+=segment.requestcount;
        //
        return false;
    }
    //get back the runtime stat
    public  double dataUpdate(boolean full){
        //Sync time
        if(GOPTaskScheduler.maxElapsedTime>synctime){
            //System.out.println("node sync time forward "+synctime +"-> "+GOPTaskScheduler.maxElapsedTime);
            synctime=GOPTaskScheduler.maxElapsedTime;
        }
        //
        double deadLineMiss=0;
        if(full){
            if(workDone!=0){
                deadLineMiss=(1.0*deadlineMiss)/workDone;
            }
                GOPTaskScheduler.VMinterfaces.get(id).deadLineMissRate=deadLineMiss;
                System.out.println("got deadLineMissRate=" + deadLineMiss);

        }else{
            //not full runtime_report, don't update deadlineMiss, then what?
        }
        //System.out.println("dataUpdate");
        GOPTaskScheduler.workpending-=(estimatedQueueLength);

         //we completed the scheduling and execution
        GOPTaskScheduler.VMinterfaces.get(id).estimatedQueueLength = 0;
        GOPTaskScheduler.VMinterfaces.get(id).estimatedExecutionTime = 0;
        GOPTaskScheduler.VMinterfaces.get(id).elapsedTime=synctime;
        GOPTaskScheduler.VMinterfaces.get(id).actualSpentTime=realspentTime;
        //System.out.println("actualSpentTime="+GOPTaskScheduler.VMinterfaces.get(id).actualSpentTime+" realspentTime="+realspentTime);
        //TimeEstimator.updateTable(this.id, answer.runtime_report); //disable for now, broken

        GOPTaskScheduler.VMinterfaces.get(id).deadlinemiss=deadlineMiss;
        GOPTaskScheduler.VMinterfaces.get(id).workdone=workDone;
        GOPTaskScheduler.VMinterfaces.get(id).Nworkdone=NworkDone;
        GOPTaskScheduler.VMinterfaces.get(id).Ndeadlinemiss=NdeadlineMiss;


        return deadLineMiss;
    }
    //shut it down, do nothing
    public  boolean sendShutdownmessage(){
        return true;
    }
    //shut it down, do nothing
    public void close(){}
    }
