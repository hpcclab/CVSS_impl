package Scheduler;

import Streampkg.*;
import TimeEstimatorpkg.TimeEstimator;
import TimeEstimatorpkg.retStat;
import VMManagement.*;


import java.util.*;


public class GOPTaskScheduler {
    private miscTools.SortableList Batchqueue=new miscTools.SortableList();
    private miscTools.SortableList pendingqueue=new miscTools.SortableList();
    private int working=0;
    public static Merger mrg;
    //private HashMap<request,List<StreamGOP>> LV2map_pending=new HashMap<request,List<StreamGOP>>(); //level2's request record skip resolution so more matches


    public static long maxElapsedTime; //use for setting Deadline

    public static ArrayList<VMinterface> VMinterfaces =new ArrayList<VMinterface>();
    private static int maxpending=0;
    public static int workpending=0;
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
                mrg.mergeifpossible(X);
            }else{
                //dont merge check
                Batchqueue.add(X);
            }
        }
            //assignwork thread start
            submitworks();
    }

    //work properly on homogeneous only
    private VMinterface shortestQueueFirst(StreamGOP x,boolean useTimeEstimator,double SDcoefficient){
        //currently machine 0 must be autoscheduleable
        if(VMinterfaces.size()>0) {
            VMinterface answer=VMinterfaces.get(0);
            long min;
            if(useTimeEstimator){
                retStat chk= TimeEstimator.getHistoricProcessTime(ServerConfig.VM_class.get(0),ServerConfig.VM_ports.get(0),x);
                x.estimatedExecutionTime = (long)(chk.mean+chk.SD*SDcoefficient);
                min=answer.estimatedExecutionTime+x.estimatedExecutionTime;
                if(ServerConfig.run_mode.equalsIgnoreCase("dry")){
                    min+=answer.estimatedQueueLength;
                }

            }else{
                min = answer.estimatedQueueLength;
            }
            System.out.println("first est time="+min);
            //System.out.println("VMINTERFACE SIZE="+VMinterfaces.size());
            for (int i = 1; i < VMinterfaces.size(); i++) {
                VMinterface aMachine = VMinterfaces.get(i);
                if (aMachine.isWorking()) {
                    if(aMachine.autoschedule) {
                        long estimatedT;
                        long savedmean = 0;
                        //calculate new choice
                        if (useTimeEstimator) {
                            retStat chk = TimeEstimator.getHistoricProcessTime(ServerConfig.VM_class.get(i), ServerConfig.VM_ports.get(i), x);
                            savedmean = (long) (chk.mean + chk.SD * SDcoefficient);
                            estimatedT = aMachine.estimatedExecutionTime + savedmean;
                            if (ServerConfig.run_mode.equalsIgnoreCase("dry")) {
                                estimatedT += answer.estimatedQueueLength;
                            }
                        } else {
                            estimatedT = aMachine.estimatedQueueLength;
                        }
                        //decide
                        System.out.println("estimateT=" + estimatedT);
                        if (estimatedT < min) {
                            if (useTimeEstimator) { //update estimatedExecutionTime
                                x.estimatedExecutionTime = savedmean;
                            }
                            answer = aMachine;
                            min = estimatedT;
                        }
                    }else{
                        System.out.println("not considering non-auto assign machine");
                    }
                }else{
                    System.out.println("warning, a machine is not ready");
                }
            }
            System.out.println("decided a machine "+answer.VM_class+" id= "+answer.id);
            return answer;
        }
        System.out.println("BUG: try to schedule to 0 VM");
        return null;
    }

    //will have more ways to assign works later
    private VMinterface assignworks(StreamGOP x){
        //System.out.println("assigning works");
        if(ServerConfig.schedulerPolicy.equalsIgnoreCase("minmin")){
            //minimum expectedTime is basically ShortestQueueFirst but calculate using TimeEstimator, and QueueExpectedTime
            return shortestQueueFirst(x,true,1);
        }else { //default way, shortestQueueFirst
            return shortestQueueFirst(x,false,1); //false for not using TimeEstimator, not virtual assign
        }
    }
    //function to test if virtually assign and nothing miss their deadline
    private boolean virtualTest(){
    //update Data, need updated Estimated ExecutionTime
        //...

        return false;
    }
    public void submitworks(){ //will be a thread
        //read through list and assign to TranscodingVM
        //now we only assign task in round robin
        System.out.println("call submit work");
        if(working!=1) {
            System.out.println("working"+workpending+" "+maxpending);
            working = 1;
            while ((!Batchqueue.isEmpty()) && workpending < maxpending) {
                StreamGOP X;
                if(ServerConfig.batchqueuesortpolicy.equalsIgnoreCase("None")) { //not sorting batch queue
                    //X= Batchqueue.poll();
                    X=Batchqueue.remove();
                }else if(ServerConfig.batchqueuesortpolicy.equalsIgnoreCase("Priority")) {
                    X=Batchqueue.removeHighestPrio();
                }else if(ServerConfig.batchqueuesortpolicy.equalsIgnoreCase("Deadline")) {
                    X=Batchqueue.removeEDL();
                }else if(ServerConfig.batchqueuesortpolicy.equalsIgnoreCase("Urgency")) {
                    X=Batchqueue.removeMaxUrgency(); //Homogeneous Only
                }else{
                    System.out.println("unrecognize batchqueue policy");
                    X = Batchqueue.removeEDL();
                }
                pendingqueue.add(X);
                //
                //mapping_policy function
                //
                VMinterface chosenVM = assignworks(X);
                if (ServerConfig.enableVMscalingoutofInterval && (chosenVM.estimatedQueueLength > ServerConfig.maxVMqueuelength)) {
                    //do reprovisioner, we need more VM!
                    System.out.println("queue too long");
                    //VMProvisioner.EvaluateClusterSize(0.8,Batchqueue.size());
                    if (ServerConfig.enableVMscalingoutofInterval) {
                        VMProvisioner.EvaluateClusterSize(-2);
                    }
                    //re-assign works
                    chosenVM = assignworks(X);
                    System.out.println("ChosenVM="+chosenVM);
                }
                //it's dry mode, we need

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
                    VMProvisioner.collectData(false);
                }
            }
            working=0;
        }
    }

    //turn off VMS socket connection sockets
    public void close(){
        for(int i = 0; i< VMinterfaces.size(); i++){
            VMinterfaces.get(i).close();
        }
    }
}