package Stream;

import javax.servlet.http.HttpServletRequest;

public class Settings implements java.io.Serializable{

    public String absPath;
    public boolean resolution = false;
    public boolean bitrate = false;
    public boolean subtitles = false;
    public boolean summarization = false;
    public String videoname = "";
    public String resWidth = "";
    public String resHeight = "";
    public Settings(){

    }
    public Settings(HttpServletRequest request) {
        this.videoname = request.getParameter("videoname");
        String resolution = request.getParameter("resolution");

        if (!resolution.equals("")) {
            this.resolution = true;
            String[] resData = resolution.split("x");
            this.resWidth = resData[0];
            this.resHeight = resData[1];
        }
    }

    public String outputDir() {
        return "repositoryvideos/" + videoname + "/out.m3u8";
        //return "output/" + videoname + resWidth + resHeight;
    }
}