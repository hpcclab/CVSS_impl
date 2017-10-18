package Repository;

import Repository.RepositoryGOP;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by pi on 5/21/17.
 */
public class Video {

    public ArrayList<RepositoryGOP> repositoryGOPs= new ArrayList<RepositoryGOP>();;

    private int totalSegments = 0;
    public String name;

    public Video(){
    }
    public Video(String path){
        this(new File(path).listFiles());
    }

    public Video(File[] files){
        if(files!=null) {
            //System.out.println(files.length);
            for (int i=0;i<files.length;i++) {
                //System.out.println(i+" "+files[i].getName() +" "+files[i].getPath()); //DEBUG
                if (!files[i].isDirectory()) {
                    String fileName = files[i].getName();
                    //check if extension is not m3u8
                    if (!fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).equalsIgnoreCase("m3u8")) {
                        RepositoryGOP repositoryGop = new RepositoryGOP(files[i].getPath());
                        this.addGOP(repositoryGop);
                    }
                }
            }
        }
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
