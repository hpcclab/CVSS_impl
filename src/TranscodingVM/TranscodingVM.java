package TranscodingVM;

import Repository.RepositoryGOP;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
//import com.amazonaws.services.ec2.model.Instance;

/**
 * Created by pi on 5/21/17.
 */
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
                RepositoryGOP objectX =(RepositoryGOP) ois.readObject();
                //System.out.println("ObjectX's path"+objectX.getPath());
                AddJob(objectX);
            }
        }catch(Exception e){
            System.out.println("Failed: " + e);
        }
        System.out.println("closed");
    }

    public void AddJob(RepositoryGOP segment)
    {

        System.out.println("test");
        TT.jobs.add(segment);
        if(!TT.isAlive()){
            TT.start();
            //System.out.println("test");
        }
    }
    public void close(){

    }
}
