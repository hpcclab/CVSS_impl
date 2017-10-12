package Repository;
import java.util.ArrayList;
import java.util.List;

public class VideoRepository {
    public ArrayList<Video> videos;
    private List<RepositoryGOP> pretranscodedSegments = new ArrayList<RepositoryGOP>();

    public VideoRepository(){
        videos = new ArrayList<Video>();
    }

    public void SendVideoToSplitter(String videoName)
    {

    }

}
