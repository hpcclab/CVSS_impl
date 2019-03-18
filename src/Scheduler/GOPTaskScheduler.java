package Scheduler;

import Cache.Caching;
import Streampkg.Stream;
import Streampkg.StreamGOP;
import VMManagement.VMinterface;
import VMManagement.VMinterface_SimLocal;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

// base class of all GOPTaskScheduler, have common functions, taskScheduling function itself schedule task in FCFS.
public abstract class GOPTaskScheduler {
    protected miscTools.SortableList Batchqueue = new miscTools.SortableList();
    public static ArrayList<VMinterface> VMinterfaces = new ArrayList<VMinterface>();
    protected int scheduler_working = 0;
    protected static int maxpending = 0;
    public static int workpending = 0;
    public static long maxElapsedTime; //use for setting Deadline
    public static List<Operations.simpleoperation> possible_Operations= new ArrayList<>();
    public static Caching cache;

    public GOPTaskScheduler(Caching c){
        cache=c;
    }

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
            if(!cache.checkExistence(X)) {
                Batchqueue.add(X);
            }else{
                System.out.println("GOP cached, no reprocess");
            }
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
    public void readlistedOperations() {
        File listfile = new File("profile/operations.txt");
        if(!listfile.exists()){
            System.out.println("\n\nWarning profile/operations.txt does not exist \n\n");
        }else{
            try {
                Scanner scanner=new Scanner(listfile);
                while(scanner.hasNext()){
                    String line[]=scanner.nextLine().split(",");
                    if(line.length==2){
                        addOperation(line[0],line[1]);
                    }else{
                        System.out.println("ill formed line?");
                    }
                }
                scanner.close();
            } catch (FileNotFoundException e) {
            System.out.println("videorepository can not find list.txt or read fail");
            //e.printStackTrace();
        }
        }
        //operations as file
    }
    //provide operation name and batchscript, and it'll be added
    public void addOperation(String name,String batchscript) {
        addOperation(new Operations.simpleoperation(name,batchscript));
    }
    public void addOperation(Operations.simpleoperation op) {
        System.out.println("operation: "+op.operationname+" is added to the system");
        possible_Operations.add(op);
    }
}
