package Scheduler;

import Stream.*;
import TranscodingVM.*;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class GOPTaskScheduler {
    private ArrayList<TranscodingVM> transcodingVMs=new ArrayList<TranscodingVM>();
    private PriorityQueue<StreamGOP> GOPtoAssign=new PriorityQueue<StreamGOP>();

    public GOPTaskScheduler(){

    }

    public boolean add_VM(TranscodingVM t){
        transcodingVMs.add(t);

        return true; //for success
    }

    public void addStream(Stream ST){
        //read through list and assign to TranscodingVM
        //now we only assign task to VM 1
        for (StreamGOP X:ST.streamGOPs){
            transcodingVMs.get(0).AddJob(X);
        }
    }
}
