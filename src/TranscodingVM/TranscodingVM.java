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
            System.out.println("Failed: " + e);
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
                AddJob(objectX);
                if(objectX.setting.equalsIgnoreCase("shutdown")){
                    //receive shutting down message, close down receiving communication
                    //whatever in the queue will still be processed until queue is empty
                    close();
                    break;
                }
            }
        }catch(Exception e){
            System.out.println("Failed: " + e);
        }
        System.out.println("closed");
    }

    public void AddJob(StreamGOP segment)
    {
        TT.jobs.add(segment);
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