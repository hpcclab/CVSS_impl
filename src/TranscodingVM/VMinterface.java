package TranscodingVM;

import Repository.RepositoryGOP;
import Scheduler.TimeEstimator;
import Stream.StreamGOP;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class VMinterface {
    private Socket s;
    public OutputStream os;
    public ObjectOutputStream oos;
    public InputStream is;
    public ObjectInputStream ois;
    private int status;
    public int estimatedqueuelength=0;
    public int id;

    public VMinterface(String addr,int port,int id){
        try {
            s = new Socket(addr, port);
            os=s.getOutputStream();
            oos = new ObjectOutputStream(os);
            is = s.getInputStream();
            ois = new ObjectInputStream(is);
            status=1;
        }catch(Exception e) {
            System.out.println("Failed: " + e);
        }
    }
    public boolean isWorking(){
        return status==1;
    }

    public boolean sendJob(RepositoryGOP segment){
        try {
            oos.writeObject(segment);
        }catch(Exception e){
            System.out.println("sendJob:"+e);
            return false;
        }
        return true;
    }
    public int dataUpdate(){
        try {
            StreamGOP query = new StreamGOP();
            query.setting = "query";
            oos.writeObject(query); //they expect an object, thus we need to send object
            report answer= (report)ois.readObject();
            //TODO: save that data for TimeEstimator
            TimeEstimator.updateTable(this.id,answer.runtime_report);
            //

            return answer.queue_size;

        }catch(Exception e){
            System.out.println(e);
            return -1;
        }
        //return 0;
    }
    public boolean sendShutdownmessage(){
        try {
            StreamGOP poison=new StreamGOP();
            poison.setting="shutdown";
            poison.setPriority(0);
            oos.writeObject(poison);
        }catch(Exception e){
            System.out.println(e);
            return false;
        }
        return true;
    }
    public void close(){
        try {
            ois.close();
            oos.close();
            s.close();
        }catch(Exception e) {
            System.out.println("Failed: " + e);
        }
    }
}