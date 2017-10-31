package TranscodingVM;

import Scheduler.ServerConfig;
import TranscodingVM.VMinterface;

import java.util.ArrayList;

public class VMProvisioner {
    private int numberOfinstances=0;

    //private String imageId;
    private int minimumMaintain;
    private static int VMcount=0;
    //private double highscalingThreshold; //get from ServerConfig
    //private double lowscalingThreshold;
    private int semaphore;
    private ArrayList<TranscodingVM> instance=new ArrayList<>();
    public VMProvisioner(){
        this(0);
    }
    public VMProvisioner(int minimumVMtomaintain){
        minimumMaintain=minimumVMtomaintain;
        EvaluateClusterSize(-1,0);
    }
    //this need to be call periodically somehow
    public void EvaluateClusterSize(double deadlineMissrate,int virtual_queuelength){
        int diff;
        //check QOS UpperBound, QOS LowerBound, update decision parameters
        if(deadlineMissrate!=-1){ //-1 set special for just ignore this section
            if(deadlineMissrate<ServerConfig.lowscalingThreshold){
                //we need to scale up by n
                diff= virtual_queuelength/(ServerConfig.remedialVM_constantfactor); // then divided by beta?
            }else if(deadlineMissrate>ServerConfig.highscalingThreshold){
                //we might consider scale down

                //select a VM to terminate, as they are not all the same
                //
            }

                //no remidial yet
        }
        // ...
        diff=minimumMaintain; // tempolary

        // then scaling to size
        if(diff>0){
            //add more VM
            AddInstances(diff); //now work like, scaleTo(size)
        }else{
            //reduce VM numbers
            DeleteInstances(diff);
        }
    }
    //pull VM data from setting file
    public int AddInstances(int diff){

        for(int i=0;i<diff;i++){
            TranscodingVM TC = new TranscodingVM(ServerConfig.VM_ports.get(VMcount));
            TC.start();
            instance.add(TC);
            System.out.println("VM " + VMcount + " started");
            VMcount++;
        }
        return VMcount;
    }
    public int DeleteInstances(int diff){
        diff*=-1; //change to positive numbers
        for(int i=0;i<diff;i++) {
            TranscodingVM TCtoRemove=instance.remove(instance.size()-1);
            //need a way to tell GOPTaskScheduler to send shutdown message

            TCtoRemove.close();
        }
        ///
        ///
        return VMcount;
    }
    public void closeAll(){
        for(TranscodingVM vm :instance){
            vm.close();
        }
    }
}
