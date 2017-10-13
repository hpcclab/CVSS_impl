package Stream;
import Repository.*;
import java.util.ArrayList;

public class Stream {
    public Video video; // for reference of which video we are talking about
    public String id;
    public String setting;
    private int status;
    public ArrayList<StreamGOP> streamGOPs;
    public void ScheduleVideoSegments(){

    }
    public Stream(){
        status=0;
        video =new Video();
        streamGOPs= new ArrayList<StreamGOP>();
    }
    public Stream(Video v){
        status=0;
        video =v;
        streamGOPs= new ArrayList<StreamGOP>();
        for(RepositoryGOP x: v.repositoryGOPs){
            StreamGOP xcopy=new StreamGOP(x);
            //xcopy.setPriority((int)(Math.random()*10)); //admission Control may kick in here
            streamGOPs.add(xcopy);
        }
    }
    public int GetStatus(){
        return status;
    }
}
