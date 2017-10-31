package Stream;
import Repository.RepositoryGOP;


public class StreamGOP extends RepositoryGOP implements Comparable<StreamGOP>,java.io.Serializable {
    public String setting="";

    public StreamGOP(){
        super();
    }
    public StreamGOP(RepositoryGOP x){
        super(x);
    }
    public StreamGOP(RepositoryGOP x, String s){
        super(x);
        setting=s;
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