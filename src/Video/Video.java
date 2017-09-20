package Video;

import GOP.GOP;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pi on 5/21/17.
 */
public class Video {

    public List<GOP> gops = new ArrayList<GOP>();

    private int totalSegments = 0;

    public Video(){

    }

    public int getTotalSegments()
    {
        return totalSegments;
    }

    public void setTotalSegments(int totalSegments)
    {
        if(totalSegments >= 0)
        {
            this.totalSegments = totalSegments;
        }
        else
        {
            this.totalSegments = -1;
        }

    }

    public void addGOP(GOP gop){
        gops.add(gop);
        totalSegments++;
    }


}
