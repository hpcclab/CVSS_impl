package Stream;
import Repository.RepositoryGOP;


public class StreamGOP extends RepositoryGOP implements Comparable<StreamGOP>,java.io.Serializable {
    public Settings userSetting;
    public String command="";
    public transient Stream parentStream;
    private long deadLine;
    public long estimatedExecutionTime=0;
    public boolean dispatched=false;

    public StreamGOP(){
        super();
    }
    public StreamGOP(Stream p,RepositoryGOP x){
        super(x);
        parentStream=p;
    }
    public StreamGOP(Stream p,RepositoryGOP x, Settings settings){
        super(x);
        parentStream=p;
        this.userSetting=settings;
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
}