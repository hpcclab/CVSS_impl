package Stream;

import Scheduler.ServerConfig;

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

    public String videoDir(){
        return "output/" + videoname + resWidth + resHeight + "/out.m3u8";
    }

    public String outputDir() {
        //return "repositoryvideos/output";
        //return System.getProperty("user.dir") + "./webapps/CVSS_Implementation_war_exploded/repositoryvideos/" + videoname + "/out.m3u8";
        return ServerConfig.path + "web/output/" + videoname + resWidth + resHeight;
        //return "output/" + videoname + resWidth + resHeight;
    }
}