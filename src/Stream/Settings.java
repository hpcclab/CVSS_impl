package Stream;

public class Settings {
    public boolean resolution = false;
    public boolean bitrate;
    public boolean subtitles;
    public boolean summarization;
    public String videoname = "";
    public String resWidth = "";
    public String resHeight = "";

    public String outputDir(){
        return "output/" + videoname + resWidth + resHeight + "/out.m3u8";
    }
}
