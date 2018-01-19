package Scheduler;

import Stream.*;
import TranscodingVM.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


import java.util.*;

class request{ //fill every thing for lvl1 mapping constructor, skip resolution for lvl2 mapping, ...
    private final String command;
    private final String  param_resWidth;
    private final String  param_resHeight;
    private final String Path;

    //constructor with StreamGOP and intended level of matching
    request(StreamGOP original,int level){
        if(level==3) {
            Path=original.getPath(); //match video segment
            command="";
            param_resHeight="";
            param_resWidth="";
        }else if(level==2){
            Path=original.getPath();
            command=original.command; //match command
            param_resHeight="";
            param_resWidth="";
        }else{ //level 1, level 3 is not yet supported
            Path=original.getPath();
            command=original.command;
            param_resHeight=original.userSetting.resHeight; //match resolution too
            param_resWidth=original.userSetting.resWidth;

        }

    }
    public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
                // if deriving: appendSuper(super.hashCode()).
                        append(command).
                        append(param_resWidth).
                        append(param_resHeight).
                        append(Path).
                        toHashCode();
    }

    //return true if it's match, design to compare to request constructed with the SAME level
    public boolean equals(Object obj){
        if (!(obj instanceof request)) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        request X=(request)obj;
        return new EqualsBuilder().
                append(command,X.command).
                append(param_resWidth,X.param_resWidth).
                append(param_resHeight,X.param_resHeight).
                append(Path,X.Path).
                isEquals();
        }
}

public class GOPTaskScheduler {
    private PriorityQueue<StreamGOP> Batchqueue=new PriorityQueue<StreamGOP>();
    private int working=0;
    private HashMap<request,StreamGOP> LV1map_pending=new HashMap<request,StreamGOP>();;
    //private HashMap<request,List<StreamGOP>> LV2map_pending=new HashMap<request,List<StreamGOP>>(); //level2's request record skip resolution so more matches
    private HashMap<request,StreamGOP> LV2map_pending=new HashMap<request,StreamGOP>();
    public static ArrayList<VMinterface> VMinterfaces =new ArrayList<VMinterface>();
    public GOPTaskScheduler(){
        if(ServerConfig.mapping_mechanism.equalsIgnoreCase("ShortestQueueFirst")){
            //add server list to ShortestQueueFirst list too
        }
    }

    public static boolean add_VM(String addr,int port,int id){
        VMinterface t=new VMinterface(addr,port,id);
        VMinterfaces.add(t);
        return true; //for success
    }
    public static boolean remove_VM(int which){
        VMinterfaces.remove(which);
        return true;
    }

    public void addStream(Stream ST){
        //Batchqueue.addAll(ST.streamGOPs); // can not just mass add without checking each piece if exist

        for(StreamGOP X:ST.streamGOPs) {
            //HOLD UP! check for duplication first
            request aRequestlvl1 = new request(X,1); //= ... derive from X
            System.out.println(X.userSetting.absPath +" "+X.userSetting.videoname);
            if (LV1map_pending.containsKey(aRequestlvl1)) {
                System.out.println("match level 1 (exactly the same request) -> dropping this request, no question!");
                //don't even need to check if it is not null or state is not dispatched
            }else{
                LV1map_pending.put(aRequestlvl1,X); //add to the level0 map, no need to be a list as there aren't any object that is not merged
                //check level 2
                request aRequestlvl2 = new request(X,2);
                if (LV2map_pending.containsKey(aRequestlvl2)) {
                    System.out.println("match level 2 (request on the same video and same command, dif resolution");
                    // do merging
                    System.out.println("MERGE or not?");

                    // IMPORTANT, if we don't merge, redo LV2map_pending.put(aRequestlvl2,X); to say we are the candidate for merge not the one we fail to merge with

                    //
                }else{
                   // ArrayList<StreamGOP> listofGOP=new ArrayList<StreamGOP>();
                   // listofGOP.add(X);
                   // LV2map_pending.put(aRequestlvl2,listofGOP);
                    LV2map_pending.put(aRequestlvl2,X);
                    // check level 3


                    //...
                    Batchqueue.add(X);
                }
            }


        }
        if(working!=1){
            //assignwork thread start
            submitworks();
        }

    }

    private VMinterface shortestQueueFirst(StreamGOP x,boolean useTimeEstimator){
        int addedConstForDeadLine=50;

        if(VMinterfaces.size()>0) {
            VMinterface answer=VMinterfaces.get(0);
            long min;
            if(useTimeEstimator){
                x.estimatedExecutionTime=TimeEstimator.getHistoricProcessTime(0,x);
                min=answer.estimatedExecutionTime+x.estimatedExecutionTime;
            }else{
                min = answer.estimatedQueueLength;
            }
            for (int i = 1; i < VMinterfaces.size(); i++) {
                VMinterface aMachine = VMinterfaces.get(i);
                if (aMachine.isWorking()) {
                    long estimatedT;
                    //calculate new choice
                    if (useTimeEstimator) {
                        estimatedT = aMachine.estimatedExecutionTime + TimeEstimator.getHistoricProcessTime(i, x);
                    } else {
                        estimatedT = aMachine.estimatedQueueLength;
                    }
                    //decide
                    if (estimatedT < min) {
                        answer = aMachine;
                        min = estimatedT;
                    }
                }
            }
            // this is outdated
            //x.deadLine=System.currentTimeMillis()+min+addedConstForDeadLine;
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
            return shortestQueueFirst(x,true);
        }else { //default way, shortestQueueFirst
            return shortestQueueFirst(x,false); //false for not using TimeEstimator
        }
    }
    private void submitworks(){ //will be a thread
        //read through list and assign to TranscodingVM
        //now we only assign task in round robin
        working=1;
        while (!Batchqueue.isEmpty()) {
            StreamGOP X=Batchqueue.poll();
            //
            //mapping_policy function
            //
            VMinterface chosenVM = assignworks(X);
            if(ServerConfig.enableVMscalingoutofInterval&&(chosenVM.estimatedQueueLength>ServerConfig.maxVMqueuelength)){
                //do reprovisioner, we need more VM!
                System.out.println("queue too long");
                //VMProvisioner.EvaluateClusterSize(0.8,Batchqueue.size());
                if(ServerConfig.enableVMscalingoutofInterval) {
                    VMProvisioner.EvaluateClusterSize(-2);
                }
                //re-assign works
                chosenVM = assignworks(X);
            }

            //change deadLine to absolute value
            X.setDeadline(X.getDeadLine());
            //change StreamGOP type to Dispatched
            X.dispatched=true;
            //X.parentStream=null;

            //then it's ready to send out
            System.out.println("sending job");
            chosenVM.sendJob(X);
            System.out.println("send job "+X.getPath()+" to "+chosenVM.toString());
            System.out.println("estimated queuelength="+chosenVM.estimatedQueueLength);
            System.out.println("estimated ExecutionTime="+chosenVM.estimatedExecutionTime);
        }
        working=0;
    }

    //turn off VMS socket connection sockets
    public void close(){
        for(int i = 0; i< VMinterfaces.size(); i++){
            VMinterfaces.get(i).close();
        }
    }
}