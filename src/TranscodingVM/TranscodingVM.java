package TranscodingVM;

import Stream.StreamGOP;
import miscTools.Tuple;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
//import com.amazonaws.services.ec2.model.Instance;

/**
 * Created by pi on 5/21/17.
 */
//TODO: best logical upgrade is to use FST instead of just serialization https://github.com/RuedigerMoeller/fast-serialization
//TODO: evaluate and implement jobqueue? activeMQ? rabbitMQ? Apache Qpid? threadpools?

class report implements Serializable{
    int queue_size;
    long queue_executionTime;
    double deadLineMissRate;
    HashMap<Integer, Tuple<Long,Integer>> runtime_report=new HashMap<>();


    public report(int queue_size,long time,double deadLineMissRate, ConcurrentHashMap<Integer, Tuple<Long, Integer>> runtime_report) {
        this.runtime_report.putAll(runtime_report);
        this.queue_executionTime=time;
        this.deadLineMissRate=deadLineMissRate;
        this.queue_size=queue_size;
    }
}

public class TranscodingVM extends Thread{
    public String type;
    protected int myport;
    protected String centerAddr;
    //private ServerSocket ss;
    protected Socket s;
    private ServerSocket ss;
    protected TranscodingThread TT;
    protected OutputStream os;
    protected ObjectOutputStream oos;
    protected InputStream is;
    protected ObjectInputStream ois;
    int status=0;
    //private Instance instance = new Instance();

    public TranscodingVM(){}

    public TranscodingVM(String itype,String addr,int port){
        type=itype;
        myport=port;
        centerAddr=addr;
        TT=new TranscodingThread();
    }
    public void createRecvSocket(){
        try {
            //s = new Socket(centerAddr, myport);
            ss = new ServerSocket(myport);
            while (status != 1) {
                try {
                    s = ss.accept();
                    ss.close();
                    os = s.getOutputStream();
                    oos = new ObjectOutputStream(os);
                    is = s.getInputStream();
                    ois = new ObjectInputStream(is);
                    status = 1;
                } catch (Exception e) {
                    System.out.println("connector thread Failed: " + e+", retry");
                }
            }
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
                StreamGOP objectX =(StreamGOP) ois.readObject();
                //System.out.println("ObjectX's path"+objectX.getPath());
                if(objectX.command.equalsIgnoreCase("shutdown")){
                    //receive shutting down message, close down receiving communication
                    //whatever in the queue will still be processed until queue is empty
                    AddJob(objectX); //still add to the queue
                    System.out.println("Shutting Down");
                    close();
                    break;
                }else if (objectX.command.equalsIgnoreCase("query")){
                    if(!TT.runtime_report.isEmpty()) {
                        //System.out.println("reporting: " + TT.runtime_report.get(0).x + " " + TT.runtime_report.get(0).y);
                    }
                    double deadLineMiss;
                    if(TT.workDone==0){
                        deadLineMiss=0;
                    }else{
                        deadLineMiss=(1.0*TT.deadLineMiss)/TT.workDone;
                    }
                    oos.writeObject(new report(TT.jobs.size(),TT.requiredTime,deadLineMiss,TT.runtime_report));
                    TT.deadLineMiss=0;
                    TT.workDone=0;
                }else{
                    AddJob(objectX);
                }
            }
        }catch(Exception e){
            System.out.println("run Failed: " + e);
        }
        System.out.println("closed");
    }

    protected void AddJob(StreamGOP segment)
    {
        TT.requiredTime+=segment.estimatedExecutionTime;
        TT.jobs.add(segment);
        //System.out.println("Thread Status="+TT.isAlive() +" "+TT.isInterrupted()+" ");
        if(!TT.isAlive()){
            TT.start();
            //System.out.println("test");
        }
    }
    protected void close(){
        try {
            s.close();
        } catch (Exception e) {
            System.out.println("Failed: " + e);
        }
    }
    //have a main?
    /*
    public static void main(String[] args){
        //TranscodingVMcloud.("ec2","test",2);

    }
    */
}