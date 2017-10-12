package GOP;

import java.util.Comparator;
/**
 * Created by pi on 5/21/17.
 */
public class GOP implements Comparable<GOP>{

    private String segment;
    private int priority;
    private Boolean isTranscoded;
    private String path;

    public GOP(String path){
        setPath(path);
        priority = 0;
        isTranscoded = false;
        segment = path.toString().substring(path.length()-4);//gets last 4 characters of path before extension, a number between 0000 and 9999
    }

    public String getPath(){ return path;}

    public void setPath(String path){this.path = path;}

    public String getSegment()
    {
        return segment;
    }

    public void setSegment(String segment)
    {
        this.segment = segment;
    }

    public int getPriority()
    {
        return priority;
    }

    public void setPriority(int priority)
    {
        this.priority = priority;
    }

    public Boolean getIsTranscoded()
    {
        return isTranscoded;
    }

    public void setIsTranscoded(Boolean isTranscoded)
    {
        this.isTranscoded = isTranscoded;
    }

    @Override
    public int compareTo(GOP t1) {
        return this.priority-t1.getPriority();
    }
}
