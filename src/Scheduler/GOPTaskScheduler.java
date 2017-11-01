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

    public static boolean add_VM(String addr,int port){
        VMinterface t=new VMinterface(addr,port);
        VMinterfaces.add(t);
        ShortestQueueFirstList.add(t);
        return true; //for success
    }

    public void addStream(Stream ST){
        Batchqueue.addAll(ST.streamGOPs);

        if(working!=1){
            //assignwork thread start
            submitworks();
        }

    }
    private static PriorityQueue<VMinterface> ShortestQueueFirstList=new PriorityQueue<VMinterface>(
            new Comparator<VMinterface>() {
                @Override
                public int compare(VMinterface vMinterface,VMinterface t1) {
                     return vMinterface.estimatedqueuelength-t1.estimatedqueuelength;
                }
            }   );
    private VMinterface shortestQueueFirst(){
        VMinterface X=ShortestQueueFirstList.poll();
        X.estimatedqueuelength++;
        ShortestQueueFirstList.add(X);
        return X;
    }
    //will have more ways to assign works later
    private VMinterface assignworks(StreamGOP x){
        return shortestQueueFirst();
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
            if(chosenVM.estimatedqueuelength>ServerConfig.maxVMqueuelength){
                //do reprovisioner, we need more VM!
                System.out.println("queue too long");
                //VMProvisioner.EvaluateClusterSize(0.8,Batchqueue.size());
                VMProvisioner.EvaluateClusterSize(0.8,10);
                //re-assign works
                chosenVM = assignworks(X);
            }
            chosenVM.sendJob(X);
            System.out.println("send job "+X.getPath()+" to "+chosenVM.toString());
            System.out.println("estimated queuelength="+chosenVM.estimatedqueuelength);
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