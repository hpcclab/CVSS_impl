package TranscodingVM;

import Scheduler.GOPTaskScheduler;
import Scheduler.ServerConfig;
import TranscodingVM.VMinterface;

import java.util.ArrayList;

import static java.lang.Thread.sleep;

public class VMProvisioner {
    private int numberOfinstances=0;

    //private String imageId;
    private static int minimumMaintain;
    private static int VMcount=0;
    //private double highscalingThreshold; //get from ServerConfig
    //private double lowscalingThreshold;
    private int semaphore;
    private static ArrayList<TranscodingVM> instance=new ArrayList<>();
    public VMProvisioner(){
        this(0);
    }
    public VMProvisioner(int minimumVMtomaintain){
        minimumMaintain=minimumVMtomaintain;
        EvaluateClusterSize(-1,0);
    }
    //this need to be call periodically somehow
    public static void EvaluateClusterSize(double deadlineMissrate,int virtual_queuelength){
        int diff=0;
        //check QOS UpperBound, QOS LowerBound, update decision parameters
        if(deadlineMissrate!=-1){ //-1 set special for just ignore this section
            if(deadlineMissrate>ServerConfig.highscalingThreshold){
                //we need to scale up by n
                diff= virtual_queuelength/(ServerConfig.remedialVM_constantfactor); // then divided by beta?
                System.out.println("diff="+diff);
            }else if(deadlineMissrate<ServerConfig.lowscalingThreshold){
                //we might consider scale down

                //select a VM to terminate, as they are not all the same
                //
            }

                //no remidial yet
        }else{
            diff=minimumMaintain; // tempolary
        }
        // ...

        // then scaling to size
        if(diff>0){
            //add more VM
            AddInstances(diff);
        }else if(diff<0){
            //reduce VM numbers
            DeleteInstances(diff);
        } //do nothing if diff==0
    }
    //pull VM data from setting file
    public static int AddInstances(int diff){

        for(int i=0;i<diff;i++){
            TranscodingVM TC = new TranscodingVM(ServerConfig.VM_ports.get(VMcount));
            TC.start();
            instance.add(TC);
            System.out.println("VM " + VMcount + " started");
            // connect interface to GOPTaskScheduler, add small delay before connect
            try {
                sleep(1);
            }catch(Exception e){

            }
            GOPTaskScheduler.add_VM(ServerConfig.VM_address.get(VMcount), ServerConfig.VM_ports.get(VMcount));

            VMcount++;
        }
        System.out.println("VMCount="+VMcount);
        return VMcount;
    }
    public static int DeleteInstances(int diff){
        diff*=-1; //change to positive numbers
        for(int i=0;i<diff;i++) {
            TranscodingVM TCtoRemove=instance.remove(instance.size()-1);
            //need a way to tell GOPTaskScheduler to send shutdown message
            VMcount--;
            TCtoRemove.close();
        }
        ///
        ///
        return VMcount;
    }
    public static void closeAll(){
        for(TranscodingVM vm :instance){
            vm.close();
        }
    }
}
