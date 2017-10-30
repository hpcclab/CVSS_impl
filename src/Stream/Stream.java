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
        this(v,0,v.repositoryGOPs.size());
    }
    public Stream(Video v,int startSegment){
        this(v,startSegment,v.repositoryGOPs.size());
    }

    public Stream(Video v,int startSegment,int endSegment){
        status=0;
        video =v;
        streamGOPs= new ArrayList<StreamGOP>();
        //for(RepositoryGOP x: v.repositoryGOPs){
        for(int i=startSegment;i<endSegment;i++){
            StreamGOP xcopy=new StreamGOP(v.repositoryGOPs.get(i) );
            streamGOPs.add(xcopy);
        }
    }
    public int GetStatus(){
        return status;
    }
}