package TranscodingVM;

import Repository.RepositoryGOP;

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
    public VMinterface(String addr,int port){
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