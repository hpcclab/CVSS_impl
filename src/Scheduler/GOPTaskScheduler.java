package Scheduler;

import Streampkg.Stream;
import Streampkg.StreamGOP;
import VMManagement.VMinterface;
import VMManagement.VMinterface_SimLocal;

import java.util.ArrayList;

public abstract class GOPTaskScheduler {
    protected miscTools.SortableList Batchqueue = new miscTools.SortableList();
    protected miscTools.SortableList pendingqueue = new miscTools.SortableList();
    public static ArrayList<VMinterface> VMinterfaces = new ArrayList<VMinterface>();
    protected int scheduler_working = 0;
    protected static int maxpending = 0;
    public static int workpending = 0;
    public static long maxElapsedTime; //use for setting Deadline

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
        if (Batchqueue != null && pendingqueue != null) {
            return (Batchqueue.isEmpty() && pendingqueue.isEmpty());
        }
        return false;
    }

    public void taskScheduling() {
        System.out.println("called empty non overwritten taskScheduling function");
    }
    //turn off VMS socket connection sockets
    public void close(){
        for(int i = 0; i< VMinterfaces.size(); i++){
            VMinterfaces.get(i).close();
        }
    }
}
