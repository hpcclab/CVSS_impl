package Streampkg;
import Repository.RepositoryGOP;
import Scheduler.ServerConfig;

import java.util.*;


public class StreamGOP extends RepositoryGOP implements Comparable<StreamGOP>,java.io.Serializable {
    //public Settings userSetting;
    public HashMap<String,LinkedList<String>> cmdSet=new HashMap<>();
    public HashMap<String,Long> deadlineSet=new HashMap<>();
    public transient Stream parentStream;
    public Settings videoSetting = null;

    public String videoname = "";
    public long deadLine=0;
    public long videoSize=900; //900 kb, fixed at the moment
    public boolean dispatched=false;
    public long arrivalTime=0;
    public long estimatedExecutionTime=0;
    public double estimatedExecutionSD=0;
    public int requestcount=0; //be 1 unless merged
    public long getdeadlineof(String key){
        if(!deadlineSet.containsKey(key)){
            System.out.println("does not contain data for this deadline!");
            return -1;
        }else{
                return deadlineSet.get(key);
        }

    }
    public boolean containCmdParam(String Command,String Setting){
        if(cmdSet.containsKey(Command)) {
            LinkedList<String> paramList=cmdSet.get(Command);
            if(paramList.contains(Setting)){
                return true;
            }
        }
        return false;
    }
    public void addCMD(String Command,String Setting,long in_deadline){
        //System.out.println("call addcmd"+Command+" "+Setting);
        if(!containCmdParam(Command,Setting)) {
            if (cmdSet.containsKey(Command)) {
                LinkedList<String> parameterList = cmdSet.get(Command);
                parameterList.add(Setting);
                // do i need to put back?, NO? it's pointer!
            } else {
                LinkedList<String> parameterList = new LinkedList<>();
                parameterList.add(Setting);
                cmdSet.put(Command, parameterList);
            }
            requestcount++;
            deadlineSet.put(Command+Setting,in_deadline);
            //System.out.println("cmd count="+requestcount+"\n\n");
        }else{
            System.out.println("already have this cmd");
        }
    }
    public void getAllCMD(StreamGOP aGOP){
        for (String command : aGOP.cmdSet.keySet()) { //not really needed, since X should have just one cmd at the moment
            LinkedList<String> param = new LinkedList<>(aGOP.cmdSet.get(command));
            for (String aparam : param) {
                addCMD(command, aparam,aGOP.getdeadlineof(command+aparam));
                //System.out.println("a call to add cmd");
            }
        }
    }
    public StreamGOP(){
        super();
        deadLine=presentationTime;
    }
    public StreamGOP(long arrivaltime){
        super();
        deadLine=presentationTime;
        arrivalTime=arrivaltime;
    }
    public StreamGOP(String name,Stream p,RepositoryGOP x,long slacktime,long arrivaltime){
        super(x);
        videoname=name;
        parentStream=p;
        deadLine=presentationTime+slacktime;
        //System.out.println("X presentationTime="+x.presentationTime);
        //System.out.println("this deadline="+deadLine);
    }
    public StreamGOP(String name,Stream p,RepositoryGOP x, String Command,String Setting,long slacktime,long arrivaltime){
        this(name,p,x,slacktime,arrivaltime);
        addCMD(Command, Setting,deadLine);
    }
    public StreamGOP(String name,Stream p,RepositoryGOP x, String Command,String Setting,long slacktime,long arrivaltime,Settings vidSetting){
        this(name,p,x,slacktime,arrivaltime);
        videoSetting = vidSetting;
        addCMD(Command, Setting,deadLine);
    }
    //deep clone
    public StreamGOP(StreamGOP X){
        this.setPath(X.getPath());
        this.segment=X.segment;
        this.isTranscoded=X.getIsTranscoded();
        this.setting=X.setting;
        this.presentationTime=X.presentationTime;
        this.arrivalTime=X.arrivalTime;
        //System.out.println("X presentationTime="+X.presentationTime);
        //
        this.videoname=X.videoname;
        this.parentStream=X.parentStream;
        this.deadLine=X.deadLine;
        this.estimatedExecutionSD=X.estimatedExecutionSD;
        this.estimatedExecutionTime=X.estimatedExecutionTime;
        this.requestcount=X.requestcount;
        //
        for(String command : X.cmdSet.keySet()){
            LinkedList<String> param= new LinkedList<>(X.cmdSet.get(command) );
            cmdSet.put(command,param);
        }
    }


    private double priority;

    @Override
    public int compareTo(StreamGOP t1) {
        double diff=this.priority-t1.getPriority();
        if(diff<0){
            return -1;
        }else if(diff>0){
            return 1;
        } else{
            return 0;
        }
    }
    public double getPriority()
    {
        return priority;
    }
    public void setPriority(double priority)
    {
        this.priority = priority;
    }
        //need to re
    public String outputDir() {
        //return System.getProperty("user.dir") + "./webapps/CVSS_Implementation_war_exploded/repositoryvideos/" + videoname + "/out.m3u8";
        return ServerConfig.path + "streams/" + videoname;
    }
    public String toString()
    {
        return "estimatedExeT: "+estimatedExecutionTime+"estimatedExeSD: "+estimatedExecutionSD+"deadline: "+deadLine+" "+cmdSet.toString();
    }

    public void print(){

    }

}