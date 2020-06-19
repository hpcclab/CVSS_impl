package ResourceManagement;

import DockerManagement.DockerManager;
import ProtoMessage.TaskRequest;
import SessionPkg.TranscodingRequest;
import TranscodingVM.TranscodingVM;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import mainPackage.CVSE;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.time.Instant;

import static java.lang.Thread.sleep;

public class ResourceProvisioner {

    private int numberOfinstances=0;

    //private String imageId;
    private int minimumMaintain;
    private int VMcount=0;
    public double deadLineMissRate=0.8;
    public Timer datacolEvent;
    //private double highscalingThreshold; //get from SystemConfig
    //private double lowscalingThreshold;
    private Semaphore x;
    private ArrayList<machineinfo> VMCollection =new ArrayList<>();
    public DataUpdate DU; //currently is a subcomponent of ResourceProvisioner
    //this need to set
    //rabbitMQ connection
    private static String RMQlocation="localhost";
    private static String FEEDBACKQUEUE_NAME = "feedback_queue";
    private static ConnectionFactory factory;
    public static Connection connection;
    private static Channel RMQchannel;

    public ResourceProvisioner( int minimumVMtomaintain) {
        //RMQ set up
        factory= new ConnectionFactory();

        factory.setHost(RMQlocation);

        try {

            connection = factory.newConnection();

            RMQchannel = connection.createChannel();
            RMQchannel.queueDeclare(FEEDBACKQUEUE_NAME, false, false, false, null);
        }catch (Exception E){
            System.out.println("Rmqbug in initializing "+E);
        }

        x=new Semaphore(1);
        DU=new DataUpdate();
        minimumMaintain=minimumVMtomaintain;
        EvaluateClusterSize(-1);
        //set up task for evaluate cluster size every ms
//        if(CVSE.config.dataUpdateInterval>0){
            //////// todo, spawn new thread to always listen to feedback_channel instead
//            ActionListener taskPerformer = new ActionListener() {
//                public void actionPerformed(ActionEvent evt) {
//                    Tick();
//                }
//            };
//            datacolEvent=new Timer(CVSE.config.dataUpdateInterval, taskPerformer);
//            datacolEvent.start();
//        }
        //////////////////////////////
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            //String message = new String(delivery.getBody(), "UTF-8");
            //System.out.println(" [x] Received '" + message + "'");

            TaskRequest.TaskReport T= TaskRequest.TaskReport.parseFrom(delivery.getBody());
            //System.out.println("Completed ExeT="+T.getExecutionTime());
            collectData(T);
            RMQchannel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };
        //////////////////////////
        boolean autoAck = false;
        try {
            RMQchannel.basicConsume(FEEDBACKQUEUE_NAME, autoAck, deliverCallback, consumerTag -> {
            });
        }catch (Exception E){
            System.out.println("feedback queue error: "+E);
        }
        System.out.println("out of loop");
            //
    }
    static int timeforced=0;
    static double previous_wovertime,previous_wundertime;
    private void collectData(TaskRequest.TaskReport T){ //ack ONE task completion
        CVSE.OW.ackCompletedVideo(T.getCompletedTaskID()); //do nothing at the moment

        CVSE.GTS.workpending-=1;
        MachineInterface MI=CVSE.GTS.machineInterfaces.get(T.getWorkerNodeID());

        MI.estimatedQueueLength=0;
        long completionTime= System.currentTimeMillis();

        //if(completionTime>T.)
        //MI.elapsedTime+=200;

    }
    public void collectData(){ //become daemon
        CVSE.GTS.machineInterfaces.get(0).dataUpdate();
        CVSE.RG.contProfileRequestsGen();

//
//        System.out.println("start collect data procedure");
//        int sum_DLmiss=0,sum_taskdone=0;
//        long current_overtime=0,current_undertime=0;
//        double current_weighted_undertime=0,current_weighted_overtime=0;
//        long T_maxElapsedTime=CVSE.GTS.maxElapsedTime;
//        int clustersize=CVSE.GTS.machineInterfaces.size();
//        for (int i=0;i<clustersize;i++){
//            CVSE.GTS.machineInterfaces.get(i).dataUpdate();
//            System.out.println("tmp taskdone="+CVSE.GTS.machineInterfaces.get(i).tmp_taskdone);
//            if(CVSE.GTS.machineInterfaces.get(i).tmp_taskdone!=0){
//                sum_DLmiss+=CVSE.GTS.machineInterfaces.get(i).tmp_taskmiss;
//                sum_taskdone+=CVSE.GTS.machineInterfaces.get(i).tmp_taskdone;
//                current_overtime+=CVSE.GTS.machineInterfaces.get(i).tmp_overtime;
//                current_undertime+=CVSE.GTS.machineInterfaces.get(i).tmp_undertime;
//                current_weighted_overtime+=CVSE.GTS.machineInterfaces.get(i).tmp_weighted_overtime;
//                current_weighted_undertime+=CVSE.GTS.machineInterfaces.get(i).tmp_weighted_undertime;
//                System.out.println("in last 20 tasks overtime:"+CVSE.GTS.machineInterfaces.get(i).tmp_overtime+" undertime:"+CVSE.GTS.machineInterfaces.get(i).tmp_undertime
//                +" weighted_overtime"+CVSE.GTS.machineInterfaces.get(i).tmp_weighted_overtime+" weighted_undertime:"+CVSE.GTS.machineInterfaces.get(i).tmp_weighted_undertime);
//            }
//            if(CVSE.GTS.machineInterfaces.get(i).elapsedTime>T_maxElapsedTime){
//                T_maxElapsedTime=CVSE.GTS.machineInterfaces.get(i).elapsedTime;
//                System.out.println("TelapsedTime update to "+T_maxElapsedTime);
//            }
//        }
//        //now update deadline miss rate, and oversubscription level
//        if(sum_taskdone!=0) {
//            deadLineMissRate = sum_DLmiss / sum_taskdone;
//            current_overtime/= clustersize;
//            current_undertime/= clustersize;
//            current_weighted_overtime/= clustersize;
//            current_weighted_undertime/= clustersize;
//
//            System.out.println("tmp DMR="+deadLineMissRate);
//            System.out.println("current over/undertime="+current_overtime+" "+current_undertime);
//            System.out.println("current weighted over/undertime="+current_weighted_overtime+" "+current_weighted_undertime);
//            previous_wovertime=current_weighted_overtime;
//            previous_wundertime=current_weighted_undertime;
//        }
//        if(DU !=null) { //maybe it's terminated at the end
//            DU.printfrequentstat();
//        }
//        //if time doesn't move,Force time move
//        if(CVSE.config.run_mode.equalsIgnoreCase("sim")) {
//            if (CVSE.GTS.maxElapsedTime != T_maxElapsedTime) {
//                System.out.println("CVSE.GTS_mergable.maxElapsedTime="+CVSE.GTS.maxElapsedTime);
//                System.out.println("reset timeforced count");
//                CVSE.GTS.maxElapsedTime = T_maxElapsedTime;
//                timeforced = 0;
//            } else {
//                long t=CVSE.RG.nextappearTime()+1;
//                if(t!=-1){ //force time move, to next arrival time
//                    CVSE.GTS.maxElapsedTime=t;
//                    System.out.println("force time move to" + CVSE.GTS.maxElapsedTime);
//                } else{ //force time to move, by 200 at final bursts
//                    System.out.println("force time move+200 " + timeforced);
//                    CVSE.GTS.maxElapsedTime += 200;
//                }
//                timeforced++;
//                if (timeforced >= 3) {
//                    if(DU !=null) { //maybe it's terminated at the end
//                        DU.printstat();
//                    }else{
//                        System.out.println("Data update module is null");
//                    }
//                    //timeforced=0;
//                }
//            }
//        }

//        if(CVSE.config.profiledRequests){
//            System.out.println("continue profile request gen");
//            if(!CVSE.RG.finished || !CVSE.GTS.emptyQueue()) { // all request arrives and queue is emtied
//               CVSE.RG.contProfileRequestsGen();
//            }  //move this somewhere else
//            else{
//                CVSE.VMP.datacolEvent.setRepeats(false);
//                CVSE.VMP.datacolEvent.stop();
//            }
//        }
    }

    private static int tcount=0;
    public void Tick(){
        if(x.tryAcquire()){
            //System.out.println("Tick, col data");
            tcount++;
            collectData();
            if(tcount% CVSE.config.CRscalingIntervalTick==0){
                EvaluateClusterSize(CVSE.GTS.getBatchqueueLength());
            }
            //System.out.println("tick, submit work");
            CVSE.GTS.taskScheduling();
            x.release();
        }else{
            System.out.println("System still busy, not doing interval job");
        }
    }
    //this need to be call periodically somehow
    public void EvaluateClusterSize(int virtual_queuelength){

        int diff = 0;
        //System.out.println(deadLineMissRate + " vs " + CVSE.config.lowscalingThreshold);
        System.out.println("virtual_queuelength= "+virtual_queuelength);
        //check QOS UpperBound, QOS LowerBound, update decision parameters
        if (virtual_queuelength == -1) { //-1 set special for just ignore this section
            diff = minimumMaintain; // tempolary
        } else if (virtual_queuelength == -2) { //unconditionally scale up
            diff = 1;
        } else {
            //collectData();
            if (deadLineMissRate > CVSE.config.highscalingThreshold) {

                //         if(deadLineMissRate>CVSE.config.highscalingThreshold){
                //we need to scale up by n
                diff = virtual_queuelength / (CVSE.config.remedialVM_constantfactor); // then divided by beta?
                System.out.println("diff=" + diff);
            } else if (deadLineMissRate < CVSE.config.lowscalingThreshold) {
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
    public int AddInstances(int diff){

        for(int i=0;i<diff;i++){
            System.out.println("VMcount="+VMcount);
            if(VMcount < CVSE.config.maxCR) {
                if(CVSE.config.CR_type.get(VMcount).equalsIgnoreCase("thread")) { //local transcoding thread mode
                    System.out.println("local virtual server");
                    //System.out.println(CVSE.config.CR_class.get(VMcount));
                    //System.out.println(CVSE.config.CR_address.get(VMcount));
                    //System.out.println(CVSE.config.CR_ports.get(VMcount));

                    TranscodingVM TC = new TranscodingVM("Thread", CVSE.config.CR_class.get(VMcount), CVSE.config.CR_address.get(VMcount), CVSE.config.CR_ports.get(VMcount));
                    CVSE.TE.populate(CVSE.config.CR_class.get(VMcount));
                    TC.start();
                    try {
                        sleep(200);
                    }catch(Exception e){
                        System.out.println("sleep bug in AddInstance (localVMThread)");
                    }
                    VMCollection.add(new machineinfo("thread","",TC));
                    MachineInterface t=new MachineInterface_JavaSocket(CVSE.config.CR_class.get(VMcount), CVSE.config.CR_address.get(VMcount), CVSE.config.CR_ports.get(VMcount),VMcount, CVSE.config.CR_autoschedule.get(VMcount));
                    CVSE.TE.populate(CVSE.config.CR_class.get(VMcount));
                    CVSE.GTS.add_VM(t, CVSE.config.CR_autoschedule.get(VMcount));
                }else if(CVSE.config.CR_type.get(VMcount).equalsIgnoreCase("sim")){ //simulation mode, without socket
                    System.out.println("simulated");
                    VMCollection.add(new machineinfo("sim",""));
                    MachineInterface t=new MachineInterface_SimLocal(CVSE.config.CR_class.get(VMcount), CVSE.config.CR_ports.get(VMcount),VMcount, CVSE.config.CR_autoschedule.get(VMcount)); //no ip needed
                    CVSE.TE.populate(CVSE.config.CR_class.get(VMcount));
                    CVSE.GTS.add_VM(t, CVSE.config.CR_autoschedule.get(VMcount));
                }else if(CVSE.config.CR_type.get(VMcount).equalsIgnoreCase("simNWcache")){ //simulation mode, without socket
                    System.out.println("simulated NWcached thread");
                    VMCollection.add(new machineinfo("simNWcache",""));
                    MachineInterface t=new MachineInterface_SimNWcache(CVSE.config.CR_class.get(VMcount), CVSE.config.CR_ports.get(VMcount),VMcount, CVSE.config.CR_autoschedule.get(VMcount)); //no ip needed
                    CVSE.GTS.add_VM(t, CVSE.config.CR_autoschedule.get(VMcount));
                    CVSE.TE.populate(CVSE.config.CR_class.get(VMcount));
                }else if(CVSE.config.CR_type.get(VMcount).equalsIgnoreCase("LocalPython")){ //create local rabbitMQ thread,
                //////////////// Experimenting here:
                    System.out.println("Create local python");
                    MachineInterface t=new MachineInterface_RabbitMQ(CVSE.config.CR_class.get(VMcount),CVSE.config.CR_address.get(VMcount), CVSE.config.CR_ports.get(VMcount),VMcount, CVSE.config.CR_autoschedule.get(VMcount),RMQchannel);
                    CVSE.TE.populate(CVSE.config.CR_class.get(VMcount));
                    CVSE.GTS.add_VM(t, CVSE.config.CR_autoschedule.get(VMcount));

                }else if(CVSE.config.CR_type.get(VMcount).equalsIgnoreCase("localContainer")){ //create local container
                    System.out.println("container thread");
                    String IP=DockerManager.CreateContainers(CVSE.config.CR_ports.get(VMcount)+"").split(",")[0]; //get IP from docker
                    System.out.println("returned IP="+IP);
                    VMCollection.add(new machineinfo("local container",IP));
                    try {
                        sleep(400);
                    }catch(Exception e){
                        System.out.println("sleep bug in AddInstance (localVMThread)");
                    }
                    System.out.println("VMcount="+VMcount);
                    MachineInterface t=new MachineInterface_JavaSocket(CVSE.config.CR_class.get(VMcount),IP, CVSE.config.CR_ports.get(VMcount),VMcount, CVSE.config.CR_autoschedule.get(VMcount)); //no ip needed
//                    MachineInterface t=new MachineInterface_JavaSocket(CVSE.config.CR_class.get(VMcount),IP, 5061,VMcount,CVSE.config.CR_autoschedule.get(VMcount)); //no ip needed
                    CVSE.TE.populate(CVSE.config.CR_class.get(VMcount));
                    CVSE.GTS.add_VM(t, CVSE.config.CR_autoschedule.get(VMcount));
                   // CVSE.TE.populate(CVSE.config.CR_class.get(VMcount)); no profile for container machine, yet
                }else if(CVSE.config.CR_type.get(VMcount).equalsIgnoreCase("EC2")){ //amazon ec2
                    System.out.println("Adding EC2, disabled");
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
    public int DeleteInstances(int diff){
        diff*=-1; //change to positive numbers
        for(int i=0;i<diff;i++) {
            if(VMcount > CVSE.config.minCR) {
                machineinfo vmitoRemove = VMCollection.remove(VMCollection.size() - 1);

                if (vmitoRemove.type.equalsIgnoreCase("thread")) {
                    System.out.println("Removing Thread " + (VMCollection.size() - 1));
                    CVSE.GTS.remove_VM(VMCollection.size() - 1);
                    VMcount--;
                    vmitoRemove.TVM.close();
                } else if (vmitoRemove.type.equalsIgnoreCase("EC2")) {
                    System.out.println("Removing EC2, disabled");
                } else {
                    System.out.println("Removing a resource");
                }
            }
        }
        ///
        ///
        return VMcount;
    }
    //relay function to outputwindoe
    public void ackCompletedVideo(List<TranscodingRequest> completedTasks){


        CVSE.OW.ackCompletedVideo(completedTasks);
    }


    public void closeAll(){
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
