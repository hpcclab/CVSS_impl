package Streampkg;

import Scheduler.ServerConfig;

import javax.servlet.http.HttpServletRequest;

public class Settings implements java.io.Serializable{

    public String absPath;
    public String videoname = "";
    public boolean resolution = false;
    public boolean bitrate = false;
    public boolean subtitles = false;
    public boolean summarization = false;
    public String resWidth = "";
    public String resHeight = "";
    public int settingIdentifier=0;
    public Settings(){
    }
    public Settings(String vName,String Hres,String Wres){
        this.videoname=vName;
        this.resHeight=Hres;
        this.resWidth=Wres;

        /* //don't make dir here
        //mk output dir if not exist
        File F=new File(this.outputDir() );
        if(ServerConfig.file_mode.equalsIgnoreCase("EC2")){
            //create cloud directory here
        }else {
            if (!F.exists()) {
                System.out.println("mkdir :" + F.getPath());
                F.mkdir();
            }
        }
        */
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
        //mk output dir if not exist
       /*
        File F=new File(this.outputDir() );
        if(!F.exists()){
            System.out.println("mkdir :"+F.getPath());
            F.mkdir();
        }
        */
    }

    public String videoDir(){
        return "output/" + videoname + resWidth + resHeight + "/out.m3u8";
    }

    public String outputDir() {
        //return "repositoryvideos/output";
        //return System.getProperty("user.dir") + "./webapps/CVSS_Implementation_war_exploded/repositoryvideos/" + videoname + "/out.m3u8";
        //return ServerConfig.path + "streams/" + videoname + resWidth + resHeight;
        return "/var/www/html/2019WebDemo/streams/" + videoname + resWidth + resHeight;
        //return "output/" + videoname + resWidth + resHeight;
    }
}