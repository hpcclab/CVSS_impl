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
    public void addAllKnownVideos(){
        for(int j=0;j<ServerConfig.repository.size();j++) {
            File[] directories = new File(ServerConfig.repository.get(j)).listFiles(File::isDirectory);
            Arrays.sort(directories);
            for (int i = 0; i < directories.length; i++) {
                System.out.println(directories[i].getPath() + "/");
                videos.add(new Video(directories[i].getPath() + "/"));
            }
        }
    }
}