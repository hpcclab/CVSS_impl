package TranscodingVM;

import ProtoMessage.TaskRequest;
import SessionPkg.TranscodingRequest;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
//import com.amazonaws.services.ec2.model.Instance;



public class TranscodingVM extends Thread{

    protected int myport;
    protected String centerAddr;
    //private ServerSocket ss;
    public Socket s;
    public ServerSocket ss;
    protected TranscodingThread TT;
    public ObjectOutputStream oos=null;
    public ObjectInputStream ois=null;
    private int status;
    //private Instance instance = new Instance();

    public TranscodingVM(){}

    public TranscodingVM(String itype,String vclass,String addr,int port){
        myport=port;
        centerAddr=addr;
        TT=new TranscodingThread();
        TT.VM_class=vclass;
        TT.type=itype;
    }
    public void createRecvSocket(){
        status=0;
        try {
            //s = new Socket(centerAddr, myport);
            ss = new ServerSocket(myport);
            System.out.println("waiting at "+myport);
            s = ss.accept();
        }catch(Exception e){
            myport++;
            System.out.println("createRecvSocket Failed: " + e);
        }
        try{
            while(!s.isConnected()){
                System.out.println("socket is not connected");
                sleep(1000);
            }
            //ss.close();
            oos = new ObjectOutputStream(s.getOutputStream());

            oos.flush();
            oos.reset();
            sleep(500);
            ois = new ObjectInputStream(s.getInputStream());

            ss.close();
            status = 1;

            System.out.println("succesfully set status=running");


        }catch(Exception e){
            System.out.println("createRecvSocket Failed: " + e);
        }
    }
    private void SendSegmentToVideoMerger()
    {

    }

    public void run(){
        createRecvSocket();

        //TODO: have a way to gracefully terminate without causing error and force quit
        try {
            while(!s.isClosed()){

                ////
                //TaskRequest.ServiceRequest.Builder B= TaskRequest.ServiceRequest.newBuilder();

                /////
                TaskRequest.ServiceRequest alphaX= (TaskRequest.ServiceRequest) ois.readObject();
                TranscodingRequest objectX =new TranscodingRequest(alphaX);
                //System.out.println("ObjectX's path"+objectX.getPath());
                if(objectX.listallCMD().contains("shutdown")){
                    //receive shutting down message, close down receiving communication
                    //whatever in the queue will still be processed until queue is empty
                    AddJob(objectX); //still add to the queue
                    System.out.println("Shutting Down");
                    close();
                    break;
                }else if (objectX.listallCMD().contains("query") ||objectX.listallCMD().contains("fullstat") ){
                    double deadLineMiss=0;
                    if(objectX.GlobalDeadline>TT.synctime){ //syncTime
                        TT.synctime=objectX.GlobalDeadline;
                    }
                    TaskRequest.WorkerReport.Builder wrb=TaskRequest.WorkerReport.newBuilder();
                    TaskRequest.WorkerReport aReport=wrb.setQueueSize(TT.jobs.size())
                            .setQueueExecutionTime(TT.requiredTime)
                            .setVMelapsedTime(TT.synctime)
                            .setVMWorkTime(TT.realspentTime)
                            .setOntimeCompletion(TT.workDone-TT.deadlineMiss)
                            .setDlMissed(TT.deadlineMiss)
                            .addAllCompletedTaskID(TT.completedTask)
                            .build();
                    oos.writeObject(aReport);
                    TT.completedTask.clear(); //got asked so clear it out
                    /////////////
                    if (objectX.listallCMD().contains("fullstat")) { //if it is full sync, remove old stats
                        TT.completedTask.clear(); //got asked so clear it out
                    }
                    //////////

                }else{
                    //System.out.println("localthread: work adding");
                    AddJob(objectX);
                }
            }
        }catch(Exception e){
            System.out.println("run Failed: " + e);
        }
        System.out.println("closed");
    }

    protected void AddJob(TranscodingRequest segment)
    {
        TT.requiredTime += segment.EstMean;
        //debugging,
        //System.out.println("got segment "+segment.getSegmentNum());
        // System.out.println("it's at: "+segment.getPath());
        // System.out.println("it's at: "+segment.userSetting.outputDir());
        //File file=new File(segment.outputDir());
        //file.mkdirs();

        TT.jobs.add(segment);
        //System.out.println("Thread Status="+TT.isAlive() +" "+TT.isInterrupted()+" ");
        if (!TT.isAlive()) {
            TT.start();
        }
    }
    public void close(){
        try {
            s.close();
        } catch (Exception e) {
            System.out.println("Failed: " + e);
        }
    }
    //have a main?
    /*
    public static void main(String[] args){
        //TranscodingVMEC2.("ec2","test",2);

    }
    */
}