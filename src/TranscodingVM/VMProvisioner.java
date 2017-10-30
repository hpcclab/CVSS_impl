package TranscodingVM;

import Scheduler.ServerConfig;
import TranscodingVM.VMinterface;

import java.util.ArrayList;

public class VMProvisioner {
    private int numberOfinstances=0;

    //private String imageId;
    private int minimumMaintain;
    private static int VMcount=0;
    private int QOSUpperBound;
    private int QOSLowerBound;
    private int semaphore;
    private ArrayList<TranscodingVM> instance=new ArrayList<>();

    public VMProvisioner(){
        this(0);
    }
    public VMProvisioner(int minimumVMtomaintain){
        minimumMaintain=minimumVMtomaintain;
        EvaluateClusterSize();
    }

    public void EvaluateClusterSize(){
        //check QOS UpperBound, QOS LowerBound, update decision parameters
        // ...
        int size=minimumMaintain; // tempolary

        // then scaling to size
        if(size>VMcount){
            //add more VM
            AddInstances(size); //now work like, scaleTo(size)
        }else{
            //reduce VM numbers
            DeleteInstances(size);
        }
    }
    //pull VM data from setting file
    public int AddInstances(int x){
        while(x>VMcount) {
            //
            TranscodingVM TC = new TranscodingVM(ServerConfig.VM_ports.get(VMcount));
            TC.start();
            instance.add(TC);
            System.out.println("VM " + VMcount + " started");
            VMcount++;
        }
        return VMcount;
    }
    public int DeleteInstances(int x){

        return VMcount;
    }
    public void closeAll(){
        for(TranscodingVM vm :instance){
            vm.close();
        }
    }
}
