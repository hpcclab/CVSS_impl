package Stream;
import Repository.*;
import Scheduler.ServerConfig;

import java.util.ArrayList;

public class Stream {
    public Video video; // for reference of which video we are talking about
    public String id;
    private int status;
    public ArrayList<StreamGOP> streamGOPs;
    public long startTime;
    public void ScheduleVideoSegments(){

    }
    public Stream(){
        status=0;
        video =new Video();
        if(ServerConfig.run_mode.equalsIgnoreCase("dry")){

        }else {
            startTime = System.currentTimeMillis() + 1000; //thisTime+Constant for now, should really be scheduleTime
        }
        streamGOPs= new ArrayList<StreamGOP>();
    }
    public Stream(Video v,String command,String settings){
        this(v,command,settings,0,v.repositoryGOPs.size());
    }
    public Stream(Video v,String command, String settings, int startSegment){
        this(v,command,settings,startSegment,v.repositoryGOPs.size());
    }

    public Stream(Video v,String command,String settings,int startSegment,int endSegment){
        status=0;
        video =v;
        if(ServerConfig.run_mode.equalsIgnoreCase("dry")){

        }else {
            startTime=System.currentTimeMillis()+1000; //thisTime+Constant for now, should really be scheduleTime
        }
        streamGOPs= new ArrayList<StreamGOP>();
        //for(RepositoryGOP x: v.repositoryGOPs){
        for(int i=startSegment;i<endSegment;i++){
            StreamGOP xcopy=new StreamGOP(video.name,this,v.repositoryGOPs.get(i),command,settings);
            streamGOPs.add(xcopy);
        }
    }
    public int GetStatus(){
        return status;
    }
}