package testpackage;

import Scheduler.GOPTaskScheduler;
import TranscodingVM.*;
import Repository.*;
import Stream.*;
import java.io.*;

/**
 * Created by pi on 6/29/17.
 */
public class Test {

    public static String test() {
        try {
            TranscodingVM TC = new TranscodingVM();


            //load Video into Repository
            Video V=new Video();
            File[] files = new File("/home/pi/apache-tomcat-7.0.78/webapps/CVSS_Implementation_Interface_war/videos/ff_trailer_part1/").listFiles();
            if(files!=null) {
                //System.out.println(files.length);
                for (int i=0;i<files.length;i++) {
                    System.out.println(i+" "+files[i].getName() +" "+files[i].getPath()); //DEBUG

                    if (!files[i].isDirectory()) {
                        String fileName = files[i].getName();
                        //check if extension is not m3u8
                        if (!fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).equalsIgnoreCase("m3u8")) {
                            RepositoryGOP repositoryGop = new RepositoryGOP(files[i].getPath());
                            //repositoryGop.setPriority((int)(Math.random()*10));
                            V.addGOP(repositoryGop);
                        }
                    }
                }
            }

            // create Stream from Video
            Stream ST=new Stream(V); //admission control can work in constructor, or later?
            ST.setting = "../bash/testbash.sh"; //setting creation or selection?

            //Scheduler
            GOPTaskScheduler TSC=new GOPTaskScheduler();
            TSC.add_VM(TC);
            TSC.addStream(ST);

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
