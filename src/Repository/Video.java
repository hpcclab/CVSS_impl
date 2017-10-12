package Repository;

import Repository.RepositoryGOP;

import java.util.ArrayList;

/**
 * Created by pi on 5/21/17.
 */
public class Video {

    public ArrayList<RepositoryGOP> repositoryGOPs;

    private int totalSegments = 0;
    public String name;

    public Video(){
        repositoryGOPs = new ArrayList<RepositoryGOP>();
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

    public void addGOP(RepositoryGOP repositoryGop){
        repositoryGOPs.add(repositoryGop);
        totalSegments++;
    }


}
