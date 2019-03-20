package ResourceManagement;

import Scheduler.GOPTaskScheduler_mergable;
import Scheduler.GOPTaskScheduler;
import Scheduler.ServerConfig;
import TimeEstimatorpkg.TimeEstimator;
import Simulator.RequestGenerator;
import TranscodingVM.TranscodingVM;
/*
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.opsworkscm.model.Server;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
*/

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import static java.lang.Thread.sleep;

public class ResourceProvisioner {

    private int numberOfinstances=0;

    //private String imageId;
    private static int minimumMaintain;
    private static int VMcount=0;
    public static double deadLineMissRate=0.8;
    //private double highscalingThreshold; //get from ServerConfig
    //private double lowscalingThreshold;
    private static Semaphore x=new Semaphore(1);
    private static ArrayList<machineinfo> VMCollection =new ArrayList<>();

    private static GOPTaskScheduler GTS;
    //this need to set


    public ResourceProvisioner(GOPTaskScheduler GTS, int minimumVMtomaintain) {
        this.GTS=GTS;
        minimumMaintain=minimumVMtomaintain;
        if(ServerConfig.useEC2){
            System.out.println("Before EC2 client, disabled for now");
            /* //EC2, cred removed
            EC2instance= new AmazonEC2Client(credentials);
            Region region = Region.getRegion(Regions.US_EAST_2);
            EC2instance.setRegion(region);
            EC2instance.describeInstances();

            List<Instance> allInstances = new ArrayList<Instance>();
            List<Instance> instances1 = new ArrayList<Instance>();


            DescribeInstancesResult result = EC2instance.describeInstances();
            List<Reservation> reservations = result.getReservations();

            for (Reservation reservation : reservations)
            {
                instances1 = reservation.getInstances();

                for (Instance instance : instances1)
                {

                    if(instance.getInstanceId() != null)
                    {
                        allInstances.add(instance);
                    }

                }
            }




            System.out.println("use EC2");
            */
        }
        if(ServerConfig.file_mode.equalsIgnoreCase("S3")) {
            /* //cred removed
            s3 = new AmazonS3Client(credentials);
            s3.setS3ClientOptions(S3ClientOptions.builder().setPathStyleAccess(true).disableChunkedEncoding().build());
            s3BucketName = bucket_name;
            boolean exists = s3.doesBucketExist(bucket_name);
            System.out.println("S3 config finished");
            */
        }
        EvaluateClusterSize(-1);
        //set up task for evaluate cluster size every ms
        if(ServerConfig.dataUpdateInterval>0){
            ActionListener taskPerformer = new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    Tick();
                }
            };
            new Timer(ServerConfig.dataUpdateInterval, taskPerformer).start();

        }
        //
    }
    public void setGTS(GOPTaskScheduler_mergable X){
        GTS=X;
        System.out.println("VMP got GTS Set");
    }
    static int timeforced=0;
    static double previous_wovertime,previous_wundertime;

    public static void collectData(){
        //choice A: direct read (not feasible in real multiple VM run)
        //choice B: send packet to ask and wait for reply (need ID)
        int sum_DLmiss=0,sum_taskdone=0;
        long current_overtime=0,current_undertime=0;
        double current_weighted_undertime=0,current_weighted_overtime=0;
        long T_maxElapsedTime=GOPTaskScheduler.maxElapsedTime;
        int clustersize=GOPTaskScheduler.machineInterfaces.size();
        for (int i=0;i<clustersize;i++){
            GOPTaskScheduler.machineInterfaces.get(i).dataUpdate();
            System.out.println("tmp taskdone="+GOPTaskScheduler.machineInterfaces.get(i).tmp_taskdone);
            if(GOPTaskScheduler.machineInterfaces.get(i).tmp_taskdone!=0){
                sum_DLmiss+=GOPTaskScheduler.machineInterfaces.get(i).tmp_taskmiss;
                sum_taskdone+=GOPTaskScheduler.machineInterfaces.get(i).tmp_taskdone;
                current_overtime+=GOPTaskScheduler.machineInterfaces.get(i).tmp_overtime;
                current_undertime+=GOPTaskScheduler.machineInterfaces.get(i).tmp_undertime;
                current_weighted_overtime+=GOPTaskScheduler.machineInterfaces.get(i).tmp_weighted_overtime;
                current_weighted_undertime+=GOPTaskScheduler.machineInterfaces.get(i).tmp_weighted_undertime;
                System.out.println("in last 20 tasks overtime:"+GOPTaskScheduler.machineInterfaces.get(i).tmp_overtime+" undertime:"+GOPTaskScheduler.machineInterfaces.get(i).tmp_undertime
                +" weighted_overtime"+GOPTaskScheduler.machineInterfaces.get(i).tmp_weighted_overtime+" weighted_undertime:"+GOPTaskScheduler.machineInterfaces.get(i).tmp_weighted_undertime);
            }
            if(GOPTaskScheduler.machineInterfaces.get(i).elapsedTime>T_maxElapsedTime){
                T_maxElapsedTime=GOPTaskScheduler.machineInterfaces.get(i).elapsedTime;
                System.out.println("TelapsedTime update to "+T_maxElapsedTime);
            }
        }
        //now update deadline miss rate, and oversubscription level
        if(sum_taskdone!=0) {
            deadLineMissRate = sum_DLmiss / sum_taskdone;
            current_overtime/= clustersize;
            current_undertime/= clustersize;
            current_weighted_overtime/= clustersize;
            current_weighted_undertime/= clustersize;

            System.out.println("tmp DMR="+deadLineMissRate);
            System.out.println("current over/undertime="+current_overtime+" "+current_undertime);
            System.out.println("current weighted over/undertime="+current_weighted_overtime+" "+current_weighted_undertime);
            /////???? how to calculate oversubscription level?
            //System.out.println("deadline miss rate="+deadLineMissRate);

            //primitive usage of SDco
            if(GTS instanceof GOPTaskScheduler_mergable) {
                GOPTaskScheduler_mergable GTS_parse=(GOPTaskScheduler_mergable)GTS;
                GTS_parse.SDco = 2 - 4 * Math.min(1, current_weighted_overtime);
                System.out.println("change SDco to " + GTS_parse.SDco);
            }
            //finally, update current time to previous time
            previous_wovertime=current_weighted_overtime;
            previous_wundertime=current_weighted_undertime;
        }

        //if time doesn't move,F
        if(ServerConfig.run_mode.equalsIgnoreCase("dry")) {
            if (GOPTaskScheduler.maxElapsedTime != T_maxElapsedTime) {
                System.out.println("GOPTaskScheduler_mergable.maxElapsedTime="+GOPTaskScheduler.maxElapsedTime);
                System.out.println("reset timeforced count");
                GOPTaskScheduler.maxElapsedTime = T_maxElapsedTime;
                timeforced = 0;
            } else {
                long t=RequestGenerator.nextappearTime();
                if(t!=-1){ //force time move, to next arrival time
                    GOPTaskScheduler.maxElapsedTime=t;
                    System.out.println("force time move to" + GOPTaskScheduler.maxElapsedTime);
                } else{ //force time to move, by 200 at final burst
                    System.out.println("force time move+200 " + timeforced);
                    GOPTaskScheduler.maxElapsedTime += 200;
                }
                timeforced++;
                if (timeforced >= 3) {
                    DataUpdate.printstat();
                    //timeforced=0;
                }
            }
        }
        if(ServerConfig.profiledRequests){
            RequestGenerator.contProfileRequestsGen(GTS);
        }
    }

    private static int tcount=0;
    public static void Tick(){
        try {
            x.acquire();
        }catch(Exception e){
            System.out.println("Semaphore Bug");
        }
        //System.out.println("Tick, col data");
        tcount++;
        collectData();
        if(tcount%ServerConfig.VMscalingIntervalTick==0){

            EvaluateClusterSize(20);
        }
        //System.out.println("tick, submit work");
        GTS.taskScheduling();
        x.release();
    }
    //this need to be call periodically somehow
    public static void EvaluateClusterSize(int virtual_queuelength){

        int diff = 0;
        System.out.println(deadLineMissRate + " vs " + ServerConfig.lowscalingThreshold);
        System.out.println("virtual_queuelength= "+virtual_queuelength);
        //check QOS UpperBound, QOS LowerBound, update decision parameters
        if (virtual_queuelength == -1) { //-1 set special for just ignore this section
            diff = minimumMaintain; // tempolary
        } else if (virtual_queuelength == -2) { //unconditionally scale up
            diff = 1;
        } else {
            //collectData();
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
    }
    //pull VM data from setting file
    public static int AddInstances(int diff){

        for(int i=0;i<diff;i++){
            System.out.println("VMcount="+VMcount);
            if(VMcount <ServerConfig.maxVM) {


                if(ServerConfig.VM_type.get(VMcount).equalsIgnoreCase("thread")) { //local transcoding thread mode
                    System.out.println("local virtual server");
                    //System.out.println(ServerConfig.VM_class.get(VMcount));
                    //System.out.println(ServerConfig.VM_address.get(VMcount));
                    //System.out.println(ServerConfig.VM_ports.get(VMcount));

                    TranscodingVM TC = new TranscodingVM("Thread",ServerConfig.VM_class.get(VMcount),ServerConfig.VM_address.get(VMcount), ServerConfig.VM_ports.get(VMcount));
                    TimeEstimator.populate(ServerConfig.VM_class.get(VMcount));
                    TC.start();
                    try {
                        sleep(200);
                    }catch(Exception e){
                        System.out.println("sleep bug in AddInstance (localVMThread)");
                    }
                    VMCollection.add(new machineinfo("thread","",TC));
                    GTS.add_VM(ServerConfig.VM_type.get(VMcount),ServerConfig.VM_class.get(VMcount),ServerConfig.VM_address.get(VMcount), ServerConfig.VM_ports.get(VMcount),VMcount,ServerConfig.VM_autoschedule.get(VMcount));
                }else if(ServerConfig.VM_type.get(VMcount).equalsIgnoreCase("sim")){ //simulation mode, without socket
                    System.out.println("local simulated thread");
                    VMCollection.add(new machineinfo("sim",""));
                    GTS.add_VM(ServerConfig.VM_type.get(VMcount),ServerConfig.VM_class.get(VMcount),ServerConfig.VM_address.get(VMcount), ServerConfig.VM_ports.get(VMcount),VMcount,ServerConfig.VM_autoschedule.get(VMcount));
                    TimeEstimator.populate(ServerConfig.VM_class.get(VMcount));
                }else if(ServerConfig.VM_type.get(VMcount).equalsIgnoreCase("simNWcache")){ //simulation mode, without socket
                    System.out.println("simulated NWcached thread");
                    VMCollection.add(new machineinfo("simNWcache",""));
                    GTS.add_VM(ServerConfig.VM_type.get(VMcount),ServerConfig.VM_class.get(VMcount),ServerConfig.VM_address.get(VMcount), ServerConfig.VM_ports.get(VMcount),VMcount,ServerConfig.VM_autoschedule.get(VMcount));
                    TimeEstimator.populate(ServerConfig.VM_class.get(VMcount));
                }else if(ServerConfig.VM_type.get(VMcount).equalsIgnoreCase("EC2")){ //amazon ec2
                    System.out.println("Adding EC2, disabled");
                    /* //EC2
                    StartInstancesRequest start=new StartInstancesRequest().withInstanceIds(ServerConfig.VM_address.get(VMcount));
                    EC2instance.startInstances(start);
                    VMCollection.add(new machineinfo("EC2",ServerConfig.VM_address.get(VMcount)));
                    //get IP back and feed to GOPTaskScheduler_mergable.addVM

                    try {
                        sleep(6000);
                        while(true){
                            sleep(1000);
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
                    DescribeInstancesRequest request = new   DescribeInstancesRequest().withInstanceIds(ServerConfig.VM_address.get(VMcount));
                    DescribeInstancesResult result= EC2instance.describeInstances(request);

                    /* //list all from request
                    List <Reservation> list  = result.getReservations();
                    //System.out.println(list);
                    for (Reservation res:list) {
                        List <Instance> instanceList= res.getInstances();

                        for (Instance instance:instanceList){

                            System.out.println("Public IP :" + instance.getPublicIpAddress());
                            System.out.println("Public DNS :" + instance.getPublicDnsName());
                            System.out.println("Instance State :" + instance.getState());
                            System.out.println("Instance TAGS :" + instance.getTags());
                        }
                    }
                    ////

                    String IP=result.getReservations().get(0).getInstances().get(0).getPublicIpAddress();

                    while(IP==null){
                        try {


                            sleep(500);
                            result= EC2instance.describeInstances(request);
                            IP=result.getReservations().get(0).getInstances().get(0).getPublicIpAddress();
                        }catch(Exception e){
                            System.out.println("get IP fail :"+e);
                        }
                    }
                    System.out.println("get IP:"+IP);
                    //System.out.println("Halt!, before connect");
                    //scanner.nextInt();
                    GOPTaskScheduler_mergable.add_VM(ServerConfig.VM_class.get(VMcount),IP, ServerConfig.VM_ports.get(VMcount),VMcount);

                    // Line below, run in the VM machine, NOT here! we need to somehow make that server run this line of code
                    //TranscodingVMcloud TC=new TranscodingVMcloud("EC2",ServerConfig.VM_address.get(VMcount), ServerConfig.VM_ports.get(VMcount));
                    //make sure instance is up and running line above
                */
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
                machineinfo vmitoRemove = VMCollection.remove(VMCollection.size() - 1);

                if (vmitoRemove.type.equalsIgnoreCase("thread")) {
                    System.out.println("Removing Thread " + (VMCollection.size() - 1));
                    GTS.remove_VM(VMCollection.size() - 1);
                    VMcount--;
                    vmitoRemove.TVM.close();
                } else if (vmitoRemove.type.equalsIgnoreCase("EC2")) {
                    System.out.println("Removing EC2, disabled");
                    /* //EC2
                    StopInstancesRequest stop=new StopInstancesRequest().withInstanceIds(vmitoRemove.identification);
                    //
                    System.out.println("can't just stop a running VMCollection immediately, need a way to solve this!");
                    //vmitoRemove.EC2inst.stopInstances(stop);

                    */
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
        for(machineinfo vm : VMCollection){
            if (vm.type.equalsIgnoreCase("thread")) {
                vm.TVM.close();
            }else if(vm.type.equalsIgnoreCase("EC2, disabled")){
                /* //EC2
                //can force close in this mode
                StopInstancesRequest stop=new StopInstancesRequest().withInstanceIds(vm.identification);
                EC2instance.stopInstances(stop);
                */
            }else{
                System.out.println("Removing unknown");
            }
        }
    }
}
