package ResourceManagement;

import IOWindows.OutputWindow;
import Scheduler.GOPTaskScheduler;
import Scheduler.ServerConfig;
import Streampkg.StreamGOP;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

//does not actually have a socket, this code is the combination of transcodingVM, transcodingthread and interface
//Warning: this mode is immediate complete but the estimatedQueuelength and estimatedExecutionTime still needs to be there for compatibility
public class MachineInterface_SimLocal extends MachineInterface {
    //interface parameters are in MachineInterface

    //pseudo thread's parameters
    private Random r=new Random();
    private long node_synctime =0; //spentTime+requiredTime is imaginary total time to clear the queue
    private long node_realspentTime =0; //realspentTime is spentTime without Syncing
    private long node_aftersync_itemdone=0,node_aftersync_itemmiss=0; //these need total sum
    private long node_aftersync_taskdone=0,node_aftersync_taskmiss=0;


    public int node_focus_task=10,node_statindex=0;
    private long node_missArr[] =new long[50]; private long node_miss; //put 50 as current max
    //use node_focus_task as N work done
    private long node_undertimeArr[] =new long[50]; //negative num for overtime
    private long node_sum_undertime;
    private long node_sum_overtime;
    private double node_wundertimeArr[] =new double[50]; //negative num for overtime
    private double node_sum_wundertime;
    private double node_sum_wovertime;
    public List<StreamGOP> completedTask=new LinkedList<StreamGOP>();
    /*
    private int l_workDone; //count each work as one
    private int l_NworkDone; //count each work as suggested in StreamGOP.requestcount
    private int l_deadlineMiss,l_NdeadlineMiss;
    private long l_overtime=0;
    private double l_overtime_weighted=0;
    private long l_undertime=0;
    private double l_undertime_weighted=0;
    */

    public MachineInterface_SimLocal(String vclass, int iport, int inid, boolean iautoschedule) {
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
        double exetime=0;
        if(ServerConfig.addProfiledDelay) {
            //System.out.println("est="+segment.estimatedExecutionTime+" sd:"+segment.estimatedExecutionSD);
            exetime=(long) (segment.estimatedExecutionTime+segment.estimatedExecutionSD*r.nextGaussian());
        }
        //System.out.println("delay="+delay);
        node_synctime +=exetime;
        node_realspentTime +=exetime;
        //System.out.println("synctime="+synctime);
        //System.out.println("realspentTime="+realspentTime);
        boolean missed=false;
        for (String cmd:segment.deadlineSet.keySet()){
            double slackleft=segment.getdeadlineof(cmd)- node_synctime;
            long slacktime=(segment.getdeadlineof(cmd)-segment.arrivalTime);

            node_miss-=node_missArr[node_statindex]; //desum old miss record, if missed
            node_missArr[node_statindex]=0; //reset the miss record
            //System.out.println("node_statindex="+node_statindex+" diff="+slackleft+" slacktime="+slacktime);
            //desum undertime
            if(node_undertimeArr[node_statindex]<0){// miss
                node_sum_overtime-=node_undertimeArr[node_statindex];
                node_sum_wovertime-=node_wundertimeArr[node_statindex];
            }else{
                node_sum_undertime-=node_undertimeArr[node_statindex]; //desum
                node_sum_wundertime-=node_wundertimeArr[node_statindex];
            }

            if(slackleft<0){
                if(!missed) { //havent miss before
                    node_aftersync_taskmiss++;
                    missed=true;
                }
                node_missArr[node_statindex]++; //it miss, so count
                node_aftersync_itemmiss++;

                if(slacktime!=0) {
                    node_sum_overtime-=(long)slackleft;
                    node_sum_wovertime-=slackleft/slacktime;
                }else{
                    System.out.println("ERROR: Time since dispatch=0");
                }
                node_miss+=node_missArr[node_statindex];
            }else{
                if(slacktime!=0) {
                    double usefulslack=slacktime-exetime;
                    node_sum_undertime+=(long)slackleft;
                    node_sum_wundertime+=slackleft/usefulslack;
                }else{
                    System.out.println("ERROR: Time since dispatch=0");
                }
            }
            //System.out.println("node_sum_undertime="+node_sum_undertime+" node_sum_overtime="+node_sum_overtime);
            //System.out.println("node_sum_wundertime="+node_sum_wundertime+" node_sum_wovertime="+node_sum_wovertime);
            node_undertimeArr[node_statindex]=(long)slackleft; //set undertime
            node_wundertimeArr[node_statindex]=slackleft/slacktime;
            node_statindex=(node_statindex+1)%node_focus_task;
        }
        node_aftersync_itemdone++;
        //System.out.println("request count="+segment.requestcount);
        node_aftersync_taskdone +=segment.requestcount;
        completedTask.add(segment);
        //
        return false;
    }
    //get back the runtime stat
    public void dataUpdate(){
        //Sync time
        if(GOPTaskScheduler.maxElapsedTime> node_synctime){
            //System.out.println("node sync time forward "+synctime +"-> "+GOPTaskScheduler_mergable.maxElapsedTime);
            node_synctime =GOPTaskScheduler.maxElapsedTime;
        }
        // change to using the circular, tmp



        //System.out.println("dataUpdate");
        GOPTaskScheduler.workpending-=(estimatedQueueLength);

         //we completed the scheduling and execution
        GOPTaskScheduler.machineInterfaces.get(id).estimatedQueueLength = 0;
        GOPTaskScheduler.machineInterfaces.get(id).estimatedExecutionTime = 0;
        GOPTaskScheduler.machineInterfaces.get(id).elapsedTime= node_synctime;
        GOPTaskScheduler.machineInterfaces.get(id).actualSpentTime= node_realspentTime;
        //System.out.println("actualSpentTime="+GOPTaskScheduler_mergable.machineInterfaces.get(id).actualSpentTime+" realspentTime="+realspentTime);
        //TimeEstimator.updateTable(this.id, answer.runtime_report); //disable for now, broken

        GOPTaskScheduler.machineInterfaces.get(id).total_itemmiss +=node_aftersync_itemmiss;
        GOPTaskScheduler.machineInterfaces.get(id).total_itemdone += node_aftersync_itemdone;
        GOPTaskScheduler.machineInterfaces.get(id).total_taskdone += node_aftersync_taskdone;
        GOPTaskScheduler.machineInterfaces.get(id).total_taskmiss += node_aftersync_taskmiss;
        GOPTaskScheduler.machineInterfaces.get(id).tmp_taskdone = node_aftersync_taskdone;
        GOPTaskScheduler.machineInterfaces.get(id).tmp_taskmiss = node_aftersync_taskmiss;
        node_aftersync_itemmiss=node_aftersync_itemdone=node_aftersync_taskdone=node_aftersync_taskmiss=0;


        GOPTaskScheduler.machineInterfaces.get(id).tmp_overtime =node_sum_overtime/node_focus_task;
        GOPTaskScheduler.machineInterfaces.get(id).tmp_undertime =node_sum_undertime/node_focus_task;

        GOPTaskScheduler.machineInterfaces.get(id).tmp_weighted_overtime =node_sum_wovertime/node_focus_task;
        GOPTaskScheduler.machineInterfaces.get(id).tmp_weighted_undertime =node_sum_wundertime/node_focus_task;

        OutputWindow.ackCompletedVideo(completedTask);
        completedTask.clear();
        //data are self expired, no need to reset or resum
    }
    //shut it down, do nothing
    public  boolean sendShutdownmessage(){
        return true;
    }
    //shut it down, do nothing
    public void close(){}
    }
