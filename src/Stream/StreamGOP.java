package Stream;
import Repository.RepositoryGOP;
import Scheduler.ServerConfig;
import com.amazonaws.services.dynamodbv2.xspec.S;
import sun.awt.image.ImageWatched;

import java.util.*;


public class StreamGOP extends RepositoryGOP implements Comparable<StreamGOP>,java.io.Serializable {
    //public Settings userSetting;
    public HashMap<String,LinkedList<String>> cmdSet=new HashMap<>();
    public transient Stream parentStream;

    public String videoname = "";
    private long deadLine;
    public long estimatedExecutionTime=0;
    public double estimatedExecutionSD=0;
    public boolean dispatched=false;


    public void addCMD(String Command,String Setting){
        if(cmdSet.containsKey(Command)){
            LinkedList<String> parameterList=cmdSet.get(Command);
            parameterList.add(Setting);
            // do i need to put back?, NO? it's pointer!
        }else{
            LinkedList<String> parameterList=new LinkedList<>();
            parameterList.add(Setting);
            cmdSet.put(Command,parameterList);
        }
    }
    public StreamGOP(){
        super();

    }
    public StreamGOP(String name,Stream p,RepositoryGOP x){
        super(x);
        videoname=name;
        parentStream=p;
    }
    public StreamGOP(String name,Stream p,RepositoryGOP x, String Command,String Setting){
        this(name,p,x);
        addCMD(Command, Setting);
    }
    //deep clone
    public StreamGOP(StreamGOP X){
        this.setPath(X.getPath());
        this.segment=X.segment;
        this.isTranscoded=X.getIsTranscoded();
        this.setting=X.setting;
        //
        this.videoname=X.videoname;
        this.parentStream=X.parentStream;
        //
        for(String command : X.cmdSet.keySet()){
            LinkedList<String> param= new LinkedList<>(X.cmdSet.get(command) );
            cmdSet.put(command,param);
        }
    }
    public long getDeadLine(){
        if(dispatched){
            return deadLine;
        }else{
            return deadLine+parentStream.startTime;
        }
    }
    public void setDeadline(long newD){
        this.deadLine=newD;
    }


    private double priority;

    @Override
    public int compareTo(StreamGOP t1) {
        double diff=t1.getPriority()-this.priority;
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
        return ServerConfig.path + "web/output/" + videoname;
    }
    public String toString()
    {
        return "estimatedExeT: "+estimatedExecutionTime+"estimatedExeSD: "+estimatedExecutionSD+"deadline: "+deadLine+" "+cmdSet.toString();
    }

}