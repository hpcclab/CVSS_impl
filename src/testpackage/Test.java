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
            File dir=new  File("/home/pi/apache-tomcat-7.0.78/webapps/CVSS_Implementation_Interface_war/videos/ff_trailer_part1/");
            File[] files = dir.listFiles();

            if(files!=null) {
                //System.out.println(files.length);
                for (int i=0;i<files.length;i++) {
                    System.out.println(i+" "+files[i].getName() +" "+files[i].getPath()); //DEBUG

                    if (!files[i].isDirectory()) {
                        String fileName = files[i].getName();
                        //check if extension is not m3u8
                        if (!fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).equalsIgnoreCase("m3u8")) {
                            GOP gop = new GOP(files[i].getPath());
                            //gop.setPriority(1);
                            gop.setPriority((int)(Math.random()*10));
                            ST.video.addGOP(gop);
                        }
                    }
                    //setting creation or selection
                    ST.setting = "../bash/testbash.sh";
                }
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
    //for test
    public static void main(String[] args){test();}

    public static String hello(){
        return "hello";
    }

}
