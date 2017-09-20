package testpackage;

import TranscodingVM.*;
import GOP.*;


/**
 * Created by pi on 6/29/17.
 */
public class Test {

    public static String test() {
        try {
            TranscodingVM t = new TranscodingVM();
            GOP gop = new GOP("/home/pi/apache-tomcat-7.0.78/webapps/CVSS_Implementation_Interface_war/videos/bbb_trailer.mp4");
            t.AddJob(gop);
            t.TranscodeSegment();
            return "Successful " + System.getProperty("user.dir");
        } catch (Exception e) {
            return "Failed: " + e;
        }
    }

    public static String hello(){
        return "hello";
    }

}
