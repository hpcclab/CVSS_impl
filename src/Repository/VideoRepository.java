package Repository;
import Scheduler.ServerConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
        File[] directories = new File(ServerConfig.repository).listFiles(File::isDirectory);
        for(int i=0;i< directories.length;i++){
            videos.add(new Video(directories[i].getPath()));
        }
    }
}