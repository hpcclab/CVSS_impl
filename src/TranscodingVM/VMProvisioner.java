package TranscodingVM;

import Scheduler.GOPTaskScheduler;
import Scheduler.ServerConfig;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;


import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

import static java.lang.Thread.sleep;
class vmi{
    String type;
    String identification;
    TranscodingVM TVM;
    AmazonEC2 EC2inst;
    public vmi(String type, String identification, TranscodingVM TVM) {
        this.type = type;
        this.identification = identification;
        this.TVM = TVM;
    }
    public vmi(String type, String identification,AmazonEC2 ec2i) {
        this.type = type;
        this.identification = identification;
        this.EC2inst=ec2i;
    }
}

public class VMProvisioner {

    private int numberOfinstances=0;

    //private String imageId;
    private static int minimumMaintain;
    private static int VMcount=0;
    public static double deadLineMissRate=0.8;
    //private double highscalingThreshold; //get from ServerConfig
    //private double lowscalingThreshold;
    private static Semaphore x=new Semaphore(1);
    private static ArrayList<vmi> VMCollection =new ArrayList<>();
    public VMProvisioner(){
        this(0);
    }
    public VMProvisioner(int minimumVMtomaintain){
        minimumMaintain=minimumVMtomaintain;
        EvaluateClusterSize(-1);
        try {
            sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //set up task for evaluate cluster size every ms
        if(ServerConfig.VMscalingInterval>0){
            ActionListener taskPerformer = new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    EvaluateClusterSize(10); //still need new values for parameters
                }
            };
            new Timer(ServerConfig.VMscalingInterval, taskPerformer).start();

        }
        //
    }
    private static void collectData(){
        //choice A: direct read (not feasible in real multiple VM run)
        //choice B: send packet to ask and wait for reply (need ID)
        double sum=0;
        int count=0;
        for (int i=0;i<GOPTaskScheduler.VMinterfaces.size();i++){
            double ret=GOPTaskScheduler.VMinterfaces.get(i).dataUpdate();
            if(ret!=-1){
                sum+=ret;
                count++;
            }
        }
        if(count!=0) {
            deadLineMissRate = sum / count;
            System.out.println("deadline miss rate="+deadLineMissRate);
        }
    }
    //this need to be call periodically somehow
    public static void EvaluateClusterSize(int virtual_queuelength){
        try {
            x.acquire();
        }catch(Exception e){
            System.out.println("Semaphore Bug");
        }
            int diff = 0;
            System.out.println(deadLineMissRate + " vs " + ServerConfig.lowscalingThreshold);
            //check QOS UpperBound, QOS LowerBound, update decision parameters
            if (virtual_queuelength == -1) { //-1 set special for just ignore this section
                diff = minimumMaintain; // tempolary
            } else if (virtual_queuelength == -2) { //unconditionally scale up
                diff = 1;
            } else {
                collectData();
                if (deadLineMissRate > ServerConfig.highscalingThreshold) {

                    //         if(deadLineMissRate>ServerConfig.highscalingThreshold){
                    //we need to scale up by n
                    diff = virtual_queuelength / (ServerConfig.remedialVM_constantfactor); // then divided by beta?
                    System.out.println("diff=" + diff);
                } else if (deadLineMissRate < ServerConfig.lowscalingThreshold) {
                    //we might consider scale down, for now, one at a time
                    System.out.println("scaling down");
                    diff = -1;
                    //select a VM to terminate, as they are not all the same
                    //
                }
            }
            // ...

            // then scaling to size
            if (diff > 0) {
                //add more VM
                AddInstances(diff);
            } else if (diff < 0) {
                //reduce VM numbers
                DeleteInstances(diff);
            } //do nothing if diff==0
        x.release();
    }
    //pull VM data from setting file
    public static int AddInstances(int diff){

        for(int i=0;i<diff;i++){

            if(VMcount <ServerConfig.maxVM) {


                if(ServerConfig.VM_type.get(VMcount).equalsIgnoreCase("thread")) {
                    System.out.println("local virtual server");
                    TranscodingVM TC = new TranscodingVM("Thread",ServerConfig.VM_address.get(VMcount), ServerConfig.VM_ports.get(VMcount));
                    TC.start();
                    try {
                        sleep(200);
                    }catch(Exception e){
                        System.out.println("sleep bug in AddInstance (localVMThread)");
                    }
                    VMCollection.add(new vmi("thread","",TC));
                    GOPTaskScheduler.add_VM(ServerConfig.VM_address.get(VMcount), ServerConfig.VM_ports.get(VMcount),VMcount);
                }else if(ServerConfig.VM_type.get(VMcount).equalsIgnoreCase("EC2")){
                    System.out.println("Adding EC2");
                    AmazonEC2 EC2instance= AmazonEC2ClientBuilder.defaultClient();

                    StartInstancesRequest start=new StartInstancesRequest().withInstanceIds(ServerConfig.VM_address.get(VMcount));
                    EC2instance.startInstances(start);
                    VMCollection.add(new vmi("EC2",ServerConfig.VM_address.get(VMcount),EC2instance));
                    //get IP back and feed to GOPTaskScheduler.addVM

                    System.out.println("Halt!, waiting for instance to run");
                    Scanner scanner=new Scanner(System.in);
                    scanner.nextInt();
                    try {
                        sleep(4000);
                    while(true){
                        sleep(2000);
                        List<InstanceStatus> poll=EC2instance.describeInstanceStatus().getInstanceStatuses();
                        //System.out.println("poll:"+poll);
                        if(poll.size()>0){
                            //System.out.println("poll:" +poll);
                            break;
                        }
                    }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    String IP=EC2instance.describeNetworkInterfaces().getNetworkInterfaces().get(0).getAssociation().getPublicIp();
                    System.out.println("get IP:"+IP);
                    GOPTaskScheduler.add_VM(IP, ServerConfig.VM_ports.get(VMcount),VMcount);

                    // Line below, run in the VM machine, NOT here! we need to somehow make that server run this line of code
                    //TranscodingVMcloud TC=new TranscodingVMcloud("EC2",ServerConfig.VM_address.get(VMcount), ServerConfig.VM_ports.get(VMcount));
                    //make sure instance is up and running line above

                }else{
                    System.out.println("Adding unknown");
                }

                System.out.println("VM " + VMcount + " started");
                VMcount++;
            }
        }
        System.out.println("VMCount="+VMcount);
        return VMcount;
    }
    public static int DeleteInstances(int diff){
        diff*=-1; //change to positive numbers
        for(int i=0;i<diff;i++) {
            if(VMcount >ServerConfig.minVM) {
                vmi vmitoRemove = VMCollection.remove(VMCollection.size() - 1);

                if (vmitoRemove.type.equalsIgnoreCase("thread")) {
                    System.out.println("Removing Thread " + (VMCollection.size() - 1));
                    GOPTaskScheduler.remove_VM(VMCollection.size() - 1);
                    VMcount--;
                    vmitoRemove.TVM.close();
                } else if (vmitoRemove.type.equalsIgnoreCase("EC2")) {
                    System.out.println("Removing EC2");
                    StopInstancesRequest stop=new StopInstancesRequest().withInstanceIds(vmitoRemove.identification);
                    //
                    System.out.println("can't just stop a running VMCollection immediately, need a way to solve this!");
                    //vmitoRemove.EC2inst.stopInstances(stop);
                } else {
                    System.out.println("Removing unknown");
                }
            }
        }
        ///
        ///
        return VMcount;
    }
    public static void closeAll(){
        for(vmi vm : VMCollection){
            if (vm.type.equalsIgnoreCase("thread")) {
                vm.TVM.close();
            }else if(vm.type.equalsIgnoreCase("EC2")){
                //can force close in this mode
                StopInstancesRequest stop=new StopInstancesRequest().withInstanceIds(vm.identification);
                vm.EC2inst.stopInstances(stop);
            }else{
                System.out.println("Removing unknown");
            }
        }
    }
}
