package Scheduler;

import ResourceManagement.MachineInterface;
import SessionPkg.Session;
import SessionPkg.TranscodingRequest;
import mainPackage.CVSE;
import miscTools.TaskQueue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

// base class of all GOPTaskScheduler, have common functions, taskScheduling function itself schedule task in FCFS.
public abstract class GOPTaskScheduler {
    Semaphore readytoWork;
    protected TaskQueue Batchqueue;
    public  ArrayList<MachineInterface> machineInterfaces = new ArrayList<MachineInterface>();

    protected  int maxpending = 0;
    public  int workpending = 0;
    public int worksubmitted=0;
    public int workcompleted=0;
    public  long  maxElapsedTime; //use for setting Deadline
    public long referenceTime; // time that everything start
    public GOPTaskScheduler(){
        readytoWork=new Semaphore(1);
        Batchqueue= new TaskQueue();
        referenceTime=System.currentTimeMillis()+10000; // future 10 seconds, hope to finish set-up by then
    }
    public int getBatchqueueLength(){
        return Batchqueue.size();
    }
    public boolean add_VM(MachineInterface t,boolean autoSchedule){
        //overwrite this
        maxpending += CVSE.config.localqueuelengthperCR; //4?
        machineInterfaces.add(t);
        return true;
    }

    public boolean remove_VM(int which) {
        machineInterfaces.remove(which);
        maxpending -= CVSE.config.localqueuelengthperCR; //4?
        return true;
    }

    public void addStream(Session ST) {
        //CVSE.AC.AssignStreamPriority(ST);
        for (TranscodingRequest X : ST.AssociatedRequests) {
            if(!CVSE.CACHING.checkExistence(X)) {
                synchronized (Batchqueue) {
                    CVSE.AC.AssignSegmentPriority(X); //ask admission controll to assign priority of the task
                    Batchqueue.add(X);
                }
            }else{
                System.out.println("GOP cached, no reprocess");
            }
        }
        //taskScheduling();
    }

    public boolean emptyQueue() {
        if (Batchqueue != null ) {
            return (Batchqueue.isEmpty());
        }
        return false;
    }
    public int FCFS(){
        int dispatched=0;
        for(int i = 0; i< machineInterfaces.size(); i++){ //get a free machine
            int assignable= machineInterfaces.get(i).estimatedQueueLength - CVSE.config.localqueuelengthperCR; //get number of task can assign to this machine
            while ((!Batchqueue.isEmpty()) && assignable>0) {
                TranscodingRequest X;
                synchronized (Batchqueue) {
                    X = Batchqueue.removeDefault();
                }
                //X.DataTag="Dispatched";
                machineInterfaces.get(i).sendJob(X);
                dispatched++;
                System.out.println("FCFS scheduler send job " + X.DataSource + " to " + machineInterfaces.get(i).toString());
            }
        }
        return dispatched;
    }
    public void taskScheduling() {
        System.out.println("called non overwritten taskScheduling function, doing FCFS assignment");
        try {
            readytoWork.acquire();
        }catch(Exception e){
            System.out.println("Sem of task scheduling error");
        }

        FCFS();
        readytoWork.release();
        }
    //turn off VMS socket connection sockets
    public void close(){
        for(int i = 0; i< machineInterfaces.size(); i++){
            machineInterfaces.get(i).close();
        }
    }

}
