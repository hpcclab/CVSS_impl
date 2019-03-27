package Scheduler;

import Streampkg.StreamGOP;
import TimeEstimatorpkg.TimeEstimator;
import TimeEstimatorpkg.retStat;
import ResourceManagement.*;
import mainPackage.CVSE;

//extends GOPTaskScheduler, with more VM type support, more scheduling options
public class GOPTaskScheduler_common extends GOPTaskScheduler {
    public GOPTaskScheduler_common(){
        super();
        //if(ServerConfig.mapping_mechanism.equalsIgnoreCase("ShortestQueueFirst")){
            //add server list to ShortestQueueFirst list too?
        //}
    }
    //overwrite with common VM types
    public boolean add_VM(MachineInterface t,boolean autoSchedule){
        if(autoSchedule) {
            maxpending += ServerConfig.localqueuelengthperVM; //4?
        }
        machineInterfaces.add(t);
        return true; //for success
    }

    //work properly on homogeneous only
    //new update: use queue length from array
    protected MachineInterface shortestQueueFirst(StreamGOP x, int[] pending_queuelength, long[] pending_executiontime, boolean useTimeEstimator, double SDcoefficient, boolean realSchedule){
        //currently machine 0 must be autoscheduleable
        long estimatedT;
        if(machineInterfaces.size()>0) {
            MachineInterface answer= machineInterfaces.get(0);
            long minFT;
            long minET=0;
            double minSD=0;

            //set initial value to machine 1
            if((pending_queuelength[0] < ServerConfig.localqueuelengthperVM) || !realSchedule){ //if not real assignment, we can violate queue length
                if (useTimeEstimator) {
                    retStat chk = CVSE.TE.getHistoricProcessTime(answer, x);
                    //System.out.println("chk.mean="+chk.mean+" chk.SD"+chk.SD+" SDco="+SDcoefficient);
                    estimatedT= (long) (chk.mean + chk.SD * SDcoefficient);
                    minFT = pending_executiontime[0] + estimatedT;
                    minET=chk.mean;
                    minSD=chk.SD;
                } else {
                    minFT = pending_queuelength[0];
                }
            }else{
                minFT=Integer.MAX_VALUE;   //don't select me, i'm full
            }


            //System.out.println("first est time="+minFT);
            //System.out.println("VMINTERFACE SIZE="+machineInterfaces.size());
            for (int i = 1; i < machineInterfaces.size(); i++) {
                MachineInterface aMachine = machineInterfaces.get(i);
                if (aMachine.isWorking()) {
                    if (aMachine.autoschedule) {
                        if((pending_queuelength[i] < ServerConfig.localqueuelengthperVM) || !realSchedule) {

                            //calculate new choice
                            if (useTimeEstimator) {
                                retStat chk = CVSE.TE.getHistoricProcessTime(aMachine, x);
                                //System.out.println("chk.mean="+chk.mean+" chk.SD"+chk.SD+" SDco="+SDcoefficient);
                                estimatedT = pending_executiontime[i] + (long)(chk.mean + chk.SD * SDcoefficient);
                                if (estimatedT < minFT) {
                                    answer = aMachine;
                                    minFT = estimatedT;
                                    minSD = chk.SD;
                                    minET = chk.mean;
                                }

                            } else {
                                estimatedT = pending_queuelength[i];
                                if (estimatedT < minFT) {
                                    answer = aMachine;
                                    minFT = estimatedT;
                                }
                            }

                        }else{
                            //System.out.println("queue is full");
                        }
                    }else{
                        System.out.println("not considering non-auto assign machine");
                    }
                }else{
                    System.out.println("warning, a machine is not ready");
                }
            }
            if(realSchedule) {
                System.out.println("decided to place on machine " + answer.VM_class + " id= " + answer.id + " new minFT=" + minFT + " queuelength=" + answer.estimatedQueueLength + "/" + ServerConfig.localqueuelengthperVM);
            }
            if (realSchedule && useTimeEstimator) { //update estimatedExecutionTime
                x.estimatedExecutionTime = minET;
                x.estimatedExecutionSD=minSD;
            }
            return answer;
        }
        System.out.println("BUG: try to schedule to 0 VM");
        return null;
    }



    //will have more ways to assign works later
    protected MachineInterface selectMachine(StreamGOP x){
        //System.out.println("assigning works");
        int[] queuelength=new int[machineInterfaces.size()];
        long[] executiontime=new long[machineInterfaces.size()];
        for(int i = 0; i< machineInterfaces.size(); i++){
            queuelength[i]= machineInterfaces.get(i).estimatedQueueLength;
            executiontime[i]= machineInterfaces.get(i).estimatedExecutionTime;
        }
        if(ServerConfig.schedulerPolicy.equalsIgnoreCase("minmin")){
            //minimum expectedTime is basically ShortestQueueFirst but calculate using TimeEstimator, and QueueExpectedTime
            return shortestQueueFirst(x,queuelength,executiontime,true,2,true); //use SDco or 2 ??
        }else { //default way, shortestQueueFirst
            return shortestQueueFirst(x,queuelength,executiontime,false,2,true); //false for not using TimeEstimator
        }
    }
    //function that do something before task X get sent
    protected void preschedulefn(StreamGOP X){

    }
    public void taskScheduling(){ // first function call to submit some works to other machine

        System.out.println("call submit work");
        if(scheduler_working !=1) {
            scheduler_working = 1;
            while ((!Batchqueue.isEmpty()) && workpending < maxpending) {
                StreamGOP X;
                //select a task by a criteria
                X=Batchqueue.removeDefault();

                preschedulefn(X);

                MachineInterface chosenVM = selectMachine(X);
                System.out.println("ChosenVM="+chosenVM);

                if (ServerConfig.enableVMscalingoutofInterval && (chosenVM.estimatedQueueLength > ServerConfig.localqueuelengthperVM)) {
                    //do reprovisioner, we need more VM!
                    //ResourceProvisioner.EvaluateClusterSize(0.8,Batchqueue.size());
                    System.out.println("queue too long, scale up!");
                    CVSE.VMP.EvaluateClusterSize(-2);
                    //re-assign works
                    chosenVM = selectMachine(X);
                    System.out.println("ChosenVM="+chosenVM);
                }

                if(ServerConfig.run_mode.equalsIgnoreCase("dry")){
                    retStat thestat=CVSE.TE.getHistoricProcessTime(chosenVM,X);
                    //System.out.println("dry run, mean="+thestat.mean+" sd="+thestat.SD);
                    X.estimatedExecutionTime=thestat.mean;
                    X.estimatedExecutionSD=thestat.SD;
                    //X.estimatedDelay
                }
                System.out.println("before dispatch");

                //change StreamGOP type to Dispatched
                X.dispatched = true;
                //X.parentStream=null;

                //then it's ready to send out
                chosenVM.sendJob(X);
                postschedulefn(X);
                System.out.println("send job " + X.getPath() + " to " + chosenVM.toString());
                //System.out.println("estimated queuelength=" + chosenVM.estimatedQueueLength);
                //System.out.println("estimated ExecutionTime=" + chosenVM.estimatedExecutionTime);
                workpending++;
                System.out.println("workpending=" + workpending + " maxpending=" + maxpending);
                if (workpending == maxpending) {
                    System.out.println("workpending==maxpending");
                    CVSE.VMP.collectData();
                }
            }
            scheduler_working =0;
        }
    }
    //function that do something after task X get sent
    protected void postschedulefn(StreamGOP X){

    }
}
