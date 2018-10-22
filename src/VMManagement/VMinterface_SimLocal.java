package VMManagement;

import Scheduler.GOPTaskScheduler;
import Scheduler.ServerConfig;
import Streampkg.StreamGOP;

import java.util.Random;

//does not actually have a socket, this code is the combination of transcodingVM, transcodingthread and interface
//Warning: this mode is immediate complete but the estimatedQueuelength and estimatedExecutionTime still needs to be there for compatibility
public class VMinterface_SimLocal extends VMinterface {
    //interface parameters are in VMinterface

    //pseudo thread's parameters
    private int l_workDone; //count each work as one
    private int l_NworkDone; //count each work as suggested in StreamGOP.requestcount
    private int l_deadlineMiss,l_NdeadlineMiss;
    private long l_synctime=0; //spentTime+requiredTime is imaginary total time to clear the queue
    private long l_realspentTime=0; //realspentTime is spentTime without Syncing
    private long l_overtime=0;
    private double l_overtime_weighted=0;
    private long l_undertime=0;
    private double l_undertime_weighted=0;
    private Random r=new Random();

    public VMinterface_SimLocal(String vclass,int iport, int inid,boolean iautoschedule) {
        super(vclass,iport,inid,iautoschedule);
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
        l_synctime+=delay;
        l_realspentTime+=delay;
        //System.out.println("synctime="+synctime);
        //System.out.println("realspentTime="+realspentTime);
        boolean missed=false;
        for (String cmd:segment.deadlineSet.keySet()){
            double diff=segment.getdeadlineof(cmd)-l_synctime;
            long slacktime=(segment.getdeadlineof(cmd)-segment.arrivalTime);
            if(diff<0){
                if(!missed) {
                    l_deadlineMiss++;
                    missed=true;
                }
                l_NdeadlineMiss++;
                l_overtime-=(long)diff;
                if(slacktime!=0) {
                    l_overtime_weighted -= diff /slacktime;
                }else{
                    System.out.println("ERROR: Time since dispatch=0");
                }
            }else{
                l_undertime+=diff;
                if(slacktime!=0) {
                    l_undertime_weighted -= diff /slacktime;
                }else{
                    System.out.println("ERROR: Time since dispatch=0");
                }
            }
        }
        l_workDone++;
        l_NworkDone +=segment.requestcount;
        //
        return false;
    }
    //get back the runtime stat
    public  double dataUpdate(boolean full){
        //Sync time
        if(GOPTaskScheduler.maxElapsedTime>l_synctime){
            //System.out.println("node sync time forward "+synctime +"-> "+GOPTaskScheduler.maxElapsedTime);
            l_synctime=GOPTaskScheduler.maxElapsedTime;
        }
        //
        double deadLineMiss=0;
        if(full){
            if(l_workDone !=0){
                deadLineMiss=(1.0*l_deadlineMiss)/ l_workDone;
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
        GOPTaskScheduler.VMinterfaces.get(id).elapsedTime=l_synctime;
        GOPTaskScheduler.VMinterfaces.get(id).actualSpentTime=l_realspentTime;
        //System.out.println("actualSpentTime="+GOPTaskScheduler.VMinterfaces.get(id).actualSpentTime+" realspentTime="+realspentTime);
        //TimeEstimator.updateTable(this.id, answer.runtime_report); //disable for now, broken

        GOPTaskScheduler.VMinterfaces.get(id).deadlinemiss=l_deadlineMiss;
        GOPTaskScheduler.VMinterfaces.get(id).workdone= l_workDone;
        GOPTaskScheduler.VMinterfaces.get(id).Nworkdone= l_NworkDone;
        GOPTaskScheduler.VMinterfaces.get(id).Ndeadlinemiss=l_NdeadlineMiss;

        GOPTaskScheduler.VMinterfaces.get(id).combined_overtime=l_overtime;
        GOPTaskScheduler.VMinterfaces.get(id).combined_undertime=l_undertime;

        GOPTaskScheduler.VMinterfaces.get(id).weighted_overtime=l_overtime_weighted;
        GOPTaskScheduler.VMinterfaces.get(id).weighted_undertime=l_undertime_weighted;
        /* //do we reset or not? and when do data expired?
        l_overtime=0;        l_undertime=0;
        l_overtime_weighted=0; l_undertime_weighted=0;
        */
        return deadLineMiss;
    }
    //shut it down, do nothing
    public  boolean sendShutdownmessage(){
        return true;
    }
    //shut it down, do nothing
    public void close(){}
    }
