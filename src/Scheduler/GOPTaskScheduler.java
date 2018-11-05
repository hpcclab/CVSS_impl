package Scheduler;

import Streampkg.*;
import TimeEstimatorpkg.TimeEstimator;
import TimeEstimatorpkg.retStat;
import VMManagement.*;


import java.util.*;


public class GOPTaskScheduler {
    private miscTools.SortableList Batchqueue=new miscTools.SortableList();
    private miscTools.SortableList pendingqueue=new miscTools.SortableList();
    private int scheduler_working =0;
    public static Merger mrg;
    //private HashMap<request,List<StreamGOP>> LV2map_pending=new HashMap<request,List<StreamGOP>>(); //level2's request record skip resolution so more matches


    public static long maxElapsedTime; //use for setting Deadline

    public static ArrayList<VMinterface> VMinterfaces =new ArrayList<VMinterface>();
    private static int maxpending=0;
    public static int workpending=0;
    public static double SDco=2;
    private static long oversubscriptionlevel;
    public GOPTaskScheduler(){
        if(ServerConfig.mapping_mechanism.equalsIgnoreCase("ShortestQueueFirst")){
            //add server list to ShortestQueueFirst list too
        }
        if(ServerConfig.taskmerge){
            mrg= new Merger(Batchqueue,pendingqueue,VMinterfaces);
        }
    }
    //connect only
    public static boolean add_VM(String VM_type,String VM_class,String addr,int port,int id,boolean autoSchedule){
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
    public static boolean remove_VM(int which){
        VMinterfaces.remove(which);
        maxpending-= ServerConfig.localqueuelengthperVM; //4?
        return true;
    }


    //bloated version of addStream, check duplication and similarity first
    public void addStream(Stream ST){
        //Batchqueue.addAll(ST.streamGOPs); // can not just mass add without checking each piece if exist
        for(StreamGOP X:ST.streamGOPs) {
            if(ServerConfig.taskmerge){
                mrg.mergeifpossible(X,SDco);
            }else{
                //dont merge check
                Batchqueue.add(X);
            }
        }
            //assignwork thread start
            taskScheduling();
    }
    public boolean emptyQueue(){
        if(Batchqueue!=null&&pendingqueue!=null){
            return (Batchqueue.isEmpty()&&pendingqueue.isEmpty());
        }
        return false;
    }
    //work properly on homogeneous only
    //new update: use queue length from array
    public static VMinterface shortestQueueFirst(StreamGOP x,int[] pending_queuelength,long[] pending_executiontime,boolean useTimeEstimator,double SDcoefficient,boolean realSchedule){
        //currently machine 0 must be autoscheduleable
        long estimatedT;
        if(VMinterfaces.size()>0) {
            VMinterface answer=VMinterfaces.get(0);
            long minFT;
            long minET=0;
            double minSD=0;

            //set initial value to machine 1
            if((pending_queuelength[0] < ServerConfig.maxVMqueuelength) || !realSchedule){ //if not real assignment, we can violate queue length
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
                        if((pending_queuelength[i] < ServerConfig.maxVMqueuelength) || !realSchedule) {

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
            System.out.println("decided a machine "+answer.VM_class+" id= "+answer.id+" queuelength="+answer.estimatedQueueLength+"/"+ServerConfig.maxVMqueuelength);
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
    private VMinterface selectMachine(StreamGOP x){
        //System.out.println("assigning works");
        int[] queuelength=new int[VMinterfaces.size()];
        long[] executiontime=new long[VMinterfaces.size()];
        for(int i=0;i<VMinterfaces.size();i++){
            queuelength[i]=VMinterfaces.get(i).estimatedQueueLength;
            executiontime[i]=VMinterfaces.get(i).estimatedExecutionTime;
        }
        if(ServerConfig.schedulerPolicy.equalsIgnoreCase("minmin")){
            //minimum expectedTime is basically ShortestQueueFirst but calculate using TimeEstimator, and QueueExpectedTime
            return shortestQueueFirst(x,queuelength,executiontime,true,2,true);
        }else { //default way, shortestQueueFirst
            return shortestQueueFirst(x,queuelength,executiontime,false,2,true); //false for not using TimeEstimator
        }
    }

    public void taskScheduling(){ // first function call to submit some works to other machine

        System.out.println("call submit work");
        if(scheduler_working !=1) {
            scheduler_working = 1;
            while ((!Batchqueue.isEmpty()) && workpending < maxpending) {
                StreamGOP X;
                //select a task by a criteria
                X=Batchqueue.removeDefault();

                pendingqueue.add(X);

                VMinterface chosenVM = selectMachine(X);
                if (ServerConfig.enableVMscalingoutofInterval && (chosenVM.estimatedQueueLength > ServerConfig.maxVMqueuelength)) {
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
                if(ServerConfig.taskmerge) {
                    mrg.removeStreamGOPfromTable(X);
                }
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

    //turn off VMS socket connection sockets
    public void close(){
        for(int i = 0; i< VMinterfaces.size(); i++){
            VMinterfaces.get(i).close();
        }
    }
}