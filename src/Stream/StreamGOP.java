package Stream;
import Repository.RepositoryGOP;


public class StreamGOP extends RepositoryGOP implements Comparable<StreamGOP> {
    public String setting;

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

    private int priority;

    @Override
    public int compareTo(StreamGOP t1) {
        return this.priority-t1.getPriority();
    }
    public int getPriority()
    {
        return priority;
    }
    public void setPriority(int priority)
    {
        this.priority = priority;
    }
}
