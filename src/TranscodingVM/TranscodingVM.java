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
    HashMap<Integer, Tuple<Long,Integer>> runtime_report=new HashMap<>();


    public report(int queue_size,long time, ConcurrentHashMap<Integer, Tuple<Long, Integer>> runtime_report) {
        this.runtime_report.putAll(runtime_report);
        this.queue_executionTime=time;
        this.queue_size=queue_size;
    }
}

public class TranscodingVM extends Thread{
    private int myport;
    private ServerSocket ss;
    private Socket s;
    private TranscodingThread TT;
    private OutputStream os;
    private ObjectOutputStream oos;
    private InputStream is;
    private ObjectInputStream ois;
    //private Instance instance = new Instance();
    public TranscodingVM(int port){

        myport=port;
        TT=new TranscodingThread();

    }
    public void createRecvSocket(){
        try {
            ss = new ServerSocket(myport);
            s = ss.accept();
            ss.close();
            os = s.getOutputStream();
            oos = new ObjectOutputStream(os);
            is = s.getInputStream();
            ois = new ObjectInputStream(is);
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
                    oos.writeObject(new report(TT.jobs.size(),TT.requiredTime,TT.runtime_report));
                }else{
                    AddJob(objectX);
                }
            }
        }catch(Exception e){
            System.out.println("run Failed: " + e);
        }
        System.out.println("closed");
    }

    public void AddJob(StreamGOP segment)
    {
        TT.requiredTime+=segment.estimatedExecutionTime;
        TT.jobs.add(segment);
        //System.out.println("Thread Status="+TT.isAlive() +" "+TT.isInterrupted()+" ");
        if(!TT.isAlive()){
            TT.start();
            //System.out.println("test");
        }
    }
    public void close(){
        try {
            s.close();
        } catch (Exception e) {
            System.out.println("Failed: " + e);
        }
    }
}