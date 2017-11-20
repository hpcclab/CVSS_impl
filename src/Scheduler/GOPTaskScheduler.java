package Scheduler;

import Stream.*;
import TranscodingVM.*;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class GOPTaskScheduler {
    private PriorityQueue<StreamGOP> Batchqueue=new PriorityQueue<StreamGOP>();
    private int working=0;
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

    public void addStream(Stream ST){
        Batchqueue.addAll(ST.streamGOPs);

        if(working!=1){
            //assignwork thread start
            submitworks();
        }

    }

    private VMinterface shortestQueueFirst(StreamGOP x,boolean useTimeEstimator){
        int addedConstForDeadLine=500;

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
                long estimatedT;
                //calculate new choice
                if(useTimeEstimator){
                    estimatedT=aMachine.estimatedExecutionTime+TimeEstimator.getHistoricProcessTime(i,x);
                }else{
                    estimatedT=aMachine.estimatedQueueLength;
                }
                //decide
                if(estimatedT<min){
                    answer=aMachine;
                    min=estimatedT;
                }
            }
            //TODO: set dead line based on real world time
            x.deadLine=System.currentTimeMillis()+min+addedConstForDeadLine;
            return answer;
        }
        System.out.println("BUG: try to schedule to 0 VM");
        return null;
    }

    //will have more ways to assign works later
    private VMinterface assignworks(StreamGOP x){
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
            if(chosenVM.estimatedQueueLength>ServerConfig.maxVMqueuelength){
                //do reprovisioner, we need more VM!
                System.out.println("queue too long");
                //VMProvisioner.EvaluateClusterSize(0.8,Batchqueue.size());
                if(ServerConfig.enableVMscalingoutofInterval) {
                    VMProvisioner.EvaluateClusterSize(0.8, 10);
                }
                //re-assign works
                chosenVM = assignworks(X);
            }

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