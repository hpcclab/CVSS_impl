package Repository;
import Scheduler.ServerConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class VideoRepository {
    public static ArrayList<Video> videos;
    private List<RepositoryGOP> pretranscodedSegments = new ArrayList<RepositoryGOP>();

    public VideoRepository(){
        videos = new ArrayList<Video>();
    }


    public void SendVideoToSplitter(String videoName)
    {

    }
    //add both real and profile video
    public void addAllKnownVideos(){
        for(int j=0;j<ServerConfig.repository.size();j++) {
            File[] directories = new File(ServerConfig.repository.get(j)).listFiles(File::isDirectory);
            Arrays.sort(directories);
            for (int i = 0; i < directories.length; i++) {
                System.out.println(directories[i].getPath() + File.separatorChar);
                videos.add(new Video(directories[i].getPath() + File.separatorChar));
            }
        }
    }
    //only add real video
    public void addAllRealVideos(){
        for(int j=0;j<ServerConfig.repository.size();j++) {
            if(ServerConfig.repository.get(j).equals("repositoryvideos/realVideo")){
                File[] directories = new File(ServerConfig.repository.get(j)).listFiles(File::isDirectory);
                Arrays.sort(directories);
                for (int i = 0; i < directories.length; i++) {
                    Video v = new Video(directories[i].getPath() + File.separatorChar);
                    videos.add(new Video(directories[i].getPath() + File.separatorChar));
                    System.out.println("Video number " + i + " GOP count: " + v.repositoryGOPs.size());
                }

            }
        }
    }
}