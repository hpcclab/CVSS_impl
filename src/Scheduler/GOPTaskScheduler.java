package Scheduler;

import Stream.*;
import TranscodingVM.*;


import java.util.ArrayList;
import java.util.PriorityQueue;

public class GOPTaskScheduler {
    private ArrayList<VMinterface> transcodingVMs=new ArrayList<VMinterface>();
    private PriorityQueue<StreamGOP> Batchqueue=new PriorityQueue<StreamGOP>();
    private int working=0;
    private int roundrobinVM=0;
    public GOPTaskScheduler(){

    }

    public boolean add_VM(String addr,int port){
        VMinterface t=new VMinterface(addr,port);
        transcodingVMs.add(t);

        return true; //for success
    }

    public void addStream(Stream ST){
        Batchqueue.addAll(ST.streamGOPs);

        if(working!=1){
            //assignwork thread start
            submitworks();
        }

    }
    private int round_robin(){
        roundrobinVM=(roundrobinVM+1)%transcodingVMs.size();
        return roundrobinVM;
    }
    //will have more ways to assign works later
    private int assignworks(StreamGOP x){
        return round_robin();
    }
    private void submitworks(){ //will be a thread
        //read through list and assign to TranscodingVM
        //now we only assign task in round robin
        working=1;
        for (StreamGOP X:Batchqueue) {
            //
            //mapping_policy function
            //
            int nextVM=assignworks(X);
            VMinterface chosenVM =transcodingVMs.get(nextVM);
            chosenVM.sendJob(X);
            chosenVM.estimatedqueuelength++;
        }
        working=0;
    }

    //turn off VMS socket connection sockets
    public void close(){
        for(int i=0;i<transcodingVMs.size();i++){
            transcodingVMs.get(i).close();
        }
    }
}