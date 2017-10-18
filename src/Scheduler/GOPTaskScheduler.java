package Scheduler;

import Stream.*;
import TranscodingVM.*;


import java.util.ArrayList;
import java.util.PriorityQueue;

public class GOPTaskScheduler {
    private ArrayList<VMinterface> transcodingVMs=new ArrayList<VMinterface>();
    private PriorityQueue<StreamGOP> GOPtoAssign=new PriorityQueue<StreamGOP>();
    private int working=0;
    public GOPTaskScheduler(){

    }

    public boolean add_VM(String addr,int port){
        VMinterface t=new VMinterface(addr,port);
        transcodingVMs.add(t);

        return true; //for success
    }

    public void addStream(Stream ST){
        GOPtoAssign.addAll(ST.streamGOPs);

        if(working!=1){
            assignworks();
        }
    }

    private void assignworks(){ //will be a thread
        //read through list and assign to TranscodingVM
        //now we only assign task to VM 1
        working=1;
        for (StreamGOP X:GOPtoAssign) {
            transcodingVMs.get(0).sendJob(X);
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
