package Repository;
import Scheduler.ServerConfig;

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
        for(int i=0;i< ServerConfig.videoList.size();i++){
            videos.add(new Video(ServerConfig.videoList.get(i)));
        }
    }
}