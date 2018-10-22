package Streampkg;
import Repository.*;
import Scheduler.GOPTaskScheduler;
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
    public Stream(Video v,String command,String settings,long addedslackTime,long arrivalTime){
        this(v,command,settings,0,v.repositoryGOPs.size(),addedslackTime,arrivalTime);
    }
    public Stream(Video v,String command, String settings, int startSegment,long addedslackTime,long arrivalTime){
        this(v,command,settings,startSegment,v.repositoryGOPs.size(),addedslackTime,arrivalTime);
    }

    public Stream(Video v,String command,String settings,int startSegment,int endSegment,long addedslackTime,long arrivalTime){
        status=0;
        video =v;

        if(addedslackTime==0) { //ST==0, did not specified start time, make a new startTime
            //normally dont fall in this case anyway in sim mode
            if(ServerConfig.run_mode.equalsIgnoreCase("dry")) {

                startTime= GOPTaskScheduler.maxElapsedTime+2000; //add a prelinary value
            }else{
                startTime = System.currentTimeMillis() + 2000; //thisTime+Constant for now, should really be scheduleTime
            }
        }else{
            startTime=addedslackTime;
        }

        streamGOPs= new ArrayList<StreamGOP>();
        //for(RepositoryGOP x: v.repositoryGOPs){
        for(int i=startSegment;i<endSegment;i++){
            String designatedSettings;
            if(settings.equalsIgnoreCase("TBD")){ //change TBD to stream&gops specific
                designatedSettings=(i+1)+"_"+v.name;
                //System.out.println("setting="+designatedSettings);
            }else{
                designatedSettings=settings;
            }
            StreamGOP xcopy=new StreamGOP(video.name,this,v.repositoryGOPs.get(i),command,designatedSettings,startTime,arrivalTime);
            //System.out.println("deadline of "+video.name+" "+(i+1)+"="+xcopy.getDeadLine());
            streamGOPs.add(xcopy);
        }
    }
    public int GetStatus(){
        return status;
    }
}