package TranscodingVM;

import Repository.RepositoryGOP;
import Stream.StreamGOP;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
//import com.amazonaws.services.ec2.model.Instance;

/**
 * Created by pi on 5/21/17.
 */
//TODO: best logical upgrade is to use FST instead of just serialization https://github.com/RuedigerMoeller/fast-serialization
//TODO: evaluate and implement jobqueue? activeMQ? rabbitMQ? Apache Qpid? threadpools?
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
                System.out.println(-1);
                StreamGOP objectX =(StreamGOP) ois.readObject();
                //System.out.println("ObjectX's path"+objectX.getPath());
                System.out.println(0);
                if(objectX.setting.equalsIgnoreCase("shutdown")){
                    //receive shutting down message, close down receiving communication
                    //whatever in the queue will still be processed until queue is empty
                    AddJob(objectX); //still add to the queue
                    System.out.println(1);
                    close();
                    break;
                }else if (objectX.setting.equalsIgnoreCase("query")){
                    System.out.println(2);
                    System.out.println("replying back "+TT.jobs.size());
                    oos.writeObject(TT.jobs.size());
                }else{
                    System.out.println(3);
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
        TT.jobs.add(segment);
        System.out.println("Thread Status="+TT.isAlive() +" "+TT.isInterrupted()+" ");
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