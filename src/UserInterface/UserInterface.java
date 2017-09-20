package UserInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pi on 5/21/17.
 */
public class UserInterface {

    private String videoFile;
    private List<String> videos = new ArrayList<String>();

    public String getVideoFile()
    {
        return videoFile;

    }

    public void setVideoFile(String videoFile)
    {
        this.videoFile = videoFile;
    }

    private void RequestVideoFromRepository()
    {


    }
}
