package Scheduler;

import Streampkg.StreamGOP;
import TimeEstimatorpkg.TimeEstimator;
import TimeEstimatorpkg.retStat;
import VMManagement.*;
import Cache.Caching;
//extends GOPTaskScheduler, with more VM type support, more scheduling options
public class GOPTaskScheduler_common extends GOPTaskScheduler {
    public GOPTaskScheduler_common(Caching c){
        super(c);
        //if(ServerConfig.mapping_mechanism.equalsIgnoreCase("ShortestQueueFirst")){
            //add server list to ShortestQueueFirst list too?
        //}
    }
    //overwrite with common VM types
    public boolean  add_VM(String VM_type,String VM_class,String addr,int port,int id,boolean autoSchedule){
        VMinterface t;
        if(VM_type.equalsIgnoreCase("sim")) {
            t = new VMinterface_SimLocal(VM_class,port,id,autoSchedule);
        }else if(VM_type.equalsIgnoreCase("simNWcache")){
            t = new VMinterface_SimNWcache(VM_class,port,id,autoSchedule);
        }else{ //not a simulation, create socket
            t = new VMinterface_SocketIO(VM_class, addr, port, id,autoSchedule);
        }
        if(autoSchedule) {
            maxpending += ServerConfig.localqueuelengthperVM; //4?
        }
        VMinterfaces.add(t);

        return true; //for success
    }

    //work properly on homogeneous only
    //new update: use queue length from array
    protected static VMinterface shortestQueueFirst(StreamGOP x, int[] pending_queuelength, long[] pending_executiontime, boolean useTimeEstimator, double SDcoefficient, boolean realSchedule){
        //currently machine 0 must be autoscheduleable
        long estimatedT;
        if(VMinterfaces.size()>0) {
            VMinterface answer=VMinterfaces.get(0);
            long minFT;
            long minET=0;
            double minSD=0;

            //set initial value to machine 1
            if((pending_queuelength[0] < ServerConfig.localqueuelengthperVM) || !realSchedule){ //if not real assignment, we can violate queue length
                if (useTimeEstimator) {
                    retStat chk = TimeEstimator.getHistoricProcessTime(ServerConfig.VM_class.get(0), ServerConfig.VM_ports.get(0), x);
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


            System.out.println("first est time="+minFT);
            //System.out.println("VMINTERFACE SIZE="+VMinterfaces.size());
            for (int i = 1; i < VMinterfaces.size(); i++) {
                VMinterface aMachine = VMinterfaces.get(i);
                if (aMachine.isWorking()) {
                    if (aMachine.autoschedule) {
                        if((pending_queuelength[i] < ServerConfig.localqueuelengthperVM) || !realSchedule) {

                            //calculate new choice
                            if (useTimeEstimator) {
                                retStat chk = TimeEstimator.getHistoricProcessTime(ServerConfig.VM_class.get(i), ServerConfig.VM_ports.get(i), x);
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
            System.out.println("decided a machine "+answer.VM_class+" id= "+answer.id+" queuelength="+answer.estimatedQueueLength+"/"+ServerConfig.localqueuelengthperVM);
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
    protected VMinterface selectMachine(StreamGOP x){
        //System.out.println("assigning works");
        int[] queuelength=new int[VMinterfaces.size()];
        long[] executiontime=new long[VMinterfaces.size()];
        for(int i=0;i<VMinterfaces.size();i++){
            queuelength[i]=VMinterfaces.get(i).estimatedQueueLength;
            executiontime[i]=VMinterfaces.get(i).estimatedExecutionTime;
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

                VMinterface chosenVM = selectMachine(X);
                if (ServerConfig.enableVMscalingoutofInterval && (chosenVM.estimatedQueueLength > ServerConfig.localqueuelengthperVM)) {
                    //do reprovisioner, we need more VM!
                    //VMProvisioner.EvaluateClusterSize(0.8,Batchqueue.size());
                    System.out.println("queue too long, scale up!");
                    VMProvisioner.EvaluateClusterSize(-2);
                    //re-assign works
                    chosenVM = selectMachine(X);
                    System.out.println("ChosenVM="+chosenVM);
                }

                if(ServerConfig.run_mode.equalsIgnoreCase("dry")){
                    retStat thestat=TimeEstimator.getHistoricProcessTime(chosenVM.VM_class,chosenVM.port,X);
                    //System.out.println("dry run, mean="+thestat.mean+" sd="+thestat.SD);
                    X.estimatedExecutionTime=thestat.mean;
                    X.estimatedExecutionSD=thestat.SD;
                    //X.estimatedDelay
                }

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
                //System.out.println("workpending=" + workpending + " maxpending=" + maxpending);
                if (workpending == maxpending) {
                    System.out.println("workpending==maxpending");
                    VMProvisioner.collectData();
                }
            }
            scheduler_working =0;
        }
    }
    //function that do something after task X get sent
    protected void postschedulefn(StreamGOP X){

    }
}
