package testpackage;

import TranscodingVM.*;
import GOP.*;
import Stream.*;
import java.io.*;

/**
 * Created by pi on 6/29/17.
 */
public class Test {

    public static String test() {
        try {
            TranscodingVM t = new TranscodingVM();
            Stream ST=new Stream();
            //create Stream's Video list
            File[] files = new File("/home/pi/apache-tomcat-7.0.78/webapps/CVSS_Implementation_Interface_war/videos/").listFiles();
            for (File eachFile : files){
                if(!eachFile.isDirectory()){
                    //if(eachfile.getName() ....){ //check if it is not m3...
                        //GOP gop = new GOP("/home/pi/apache-tomcat-7.0.78/webapps/CVSS_Implementation_Interface_war/videos/bbb_trailer.mp4");
                        GOP gop = new GOP(eachFile.getCanonicalPath());
                        ST.video.addGOP(gop);
                    //}
                }
                ST.setting=""; //setting...
            }
            //read through Stream's video list and assign to TranscodingVM
            for (GOP X:ST.video.gops){
                t.AddJob(X);
            }


            return "Successful " + System.getProperty("user.dir");
        } catch (Exception e) {
            return "Failed: " + e;
        }
    }

    public static String hello(){
        return "hello";
    }

}
