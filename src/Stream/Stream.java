package Stream;
import Repository.*;
import java.util.ArrayList;

public class Stream {
    public Video video; // for reference of which video we are talking about
    public String id;
    public Settings settings;
    private int status;
    public ArrayList<StreamGOP> streamGOPs;
    public long startTime;
    public void ScheduleVideoSegments(){

    }
    public Stream(){
        status=0;
        video =new Video();
        startTime=System.currentTimeMillis()+500; //thisTime+Constant for now, should really be scheduleTime
        streamGOPs= new ArrayList<StreamGOP>();
    }
    public Stream(Video v,Settings settings){
        this(v,settings,0,v.repositoryGOPs.size());
    }
    public Stream(Video v, Settings settings, int startSegment){
        this(v,settings,startSegment,v.repositoryGOPs.size());
    }

    public Stream(Video v,Settings settings,int startSegment,int endSegment){
        status=0;
        video =v;
        startTime=System.currentTimeMillis()+500; //thisTime+Constant for now, should really be scheduleTime
        streamGOPs= new ArrayList<StreamGOP>();
        //for(RepositoryGOP x: v.repositoryGOPs){
        for(int i=startSegment;i<endSegment;i++){
            StreamGOP xcopy=new StreamGOP(this,v.repositoryGOPs.get(i),settings);
            streamGOPs.add(xcopy);
        }
    }
    public int GetStatus(){
        return status;
    }
}