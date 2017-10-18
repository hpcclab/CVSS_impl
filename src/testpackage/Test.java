package testpackage;

import Scheduler.AdmissionControl;
import Scheduler.GOPTaskScheduler;
import TranscodingVM.*;
import Repository.*;
import Stream.*;
import java.io.*;
import java.util.Scanner;

/**
 * Created by pi on 6/29/17.
 */
public class Test {

    public static String test() {
        try {
            Scanner scanner=new Scanner(System.in);
            //Set things up
            AdmissionControl AC = new AdmissionControl();
            GOPTaskScheduler GTS=new GOPTaskScheduler();
            TranscodingVM TC = new TranscodingVM(5690); //new thread waiting at that port
            TC.start();
            GTS.add_VM("localhost",5690); //connect to that machine (localhost) and that port
            //TSC.add_VM(TC);

            //load Videos into Repository
            Video V1=new Video("/home/pi/apache-tomcat-7.0.78/webapps/CVSS_Implementation_Interface_war/videos/ff_trailer_part1/");
            Video V2=new Video("/home/pi/apache-tomcat-7.0.78/webapps/CVSS_Implementation_Interface_war/videos/ff_trailer_part3/");

            // Check point, enter any key to continue
            System.out.println("two video loaded, enter any key to continue");
            scanner.next();


            // create Stream from Video
            Stream ST=new Stream(V1); //admission control can work in constructor, or later?
            ST.setting = "../bash/testbash.sh"; //setting creation or selection?

            //Admission Control assign Priority of each segments
            AC.AssignStreamPriority(ST);
            for(StreamGOP x:ST.streamGOPs){
                System.out.println(x.getPriority());
            }
            //Scheduler
            GTS.addStream(ST);

            // Check point, enter any key to continue
            System.out.println("enter any key to terminate the system");
            scanner.next();

            //wind down process
            //GTS.close();
            //TC.close();

            return "Successful " + System.getProperty("user.dir");
        } catch (Exception e) {
            return "Failed: " + e;
        }
    }
    //for test
    public static void main(String[] args){test();}

}
