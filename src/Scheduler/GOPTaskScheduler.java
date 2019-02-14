package Scheduler;

import Streampkg.Stream;
import Streampkg.StreamGOP;
import VMManagement.VMinterface;
import VMManagement.VMinterface_SimLocal;
import java.util.List;
import java.util.ArrayList;

// base class of all GOPTaskScheduler, have common functions, taskScheduling function itself schedule task in FCFS.
public abstract class GOPTaskScheduler {
    protected miscTools.SortableList Batchqueue = new miscTools.SortableList();
    public static ArrayList<VMinterface> VMinterfaces = new ArrayList<VMinterface>();
    protected int scheduler_working = 0;
    protected static int maxpending = 0;
    public static int workpending = 0;
    public static long maxElapsedTime; //use for setting Deadline
    public static List<Operations.simpleoperation> possible_Operations= new ArrayList<>();

    public boolean add_VM(String VM_type, String VM_class, String addr, int port, int id, boolean autoSchedule)
      {
        //overwrite this

        System.out.println("called empty non overwritten add_VM function, so i add only SimLocal thread");
        try {
            Thread.sleep(1000);
        }catch(Exception e){
            System.out.println("sleep bug");
        }
        VMinterface t = t = new VMinterface_SimLocal(VM_class, port, id, autoSchedule); //only support simlocal in this minimal version
        VMinterfaces.add(t);
        return false;
    }

    public boolean remove_VM(int which) {
        VMinterfaces.remove(which);
        maxpending -= ServerConfig.localqueuelengthperVM; //4?
        return true;
    }

    public void addStream(Stream ST) {

        for (StreamGOP X : ST.streamGOPs) {
            Batchqueue.add(X);
        }
        taskScheduling();
    }

    public boolean emptyQueue() {
        if (Batchqueue != null ) {
            return (Batchqueue.isEmpty());
        }
        return false;
    }

    public void taskScheduling() {
        System.out.println("called non overwritten taskScheduling function, doing FCFS assignment");
        if(scheduler_working !=1) {
            scheduler_working = 1;

            for(int i=0;i<VMinterfaces.size();i++){ //get a free machine
                int assignable=VMinterfaces.get(i).estimatedQueueLength - ServerConfig.localqueuelengthperVM; //get number of task can assign to this machine
                while ((!Batchqueue.isEmpty()) && assignable>0) {
                    StreamGOP X=Batchqueue.removeDefault();
                    X.dispatched=true;
                    VMinterfaces.get(i).sendJob(X);
                    System.out.println("send job " + X.getPath() + " to " + VMinterfaces.get(i).toString());
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
    public void readcommonOperations() {

        addOperations(new Operations.Framerate());
        addOperations(new Operations.Resolution());
        addOperations(new Operations.Bitrate());
        addOperations(new Operations.Codec());
    }
    public void addOperations(Operations.simpleoperation op) {
        possible_Operations.add(op);
    }
}
