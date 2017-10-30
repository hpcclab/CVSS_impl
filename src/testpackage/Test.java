package testpackage;

import Scheduler.AdmissionControl;
import Scheduler.GOPTaskScheduler;
import Scheduler.ServerConfig;
import TranscodingVM.*;
import Repository.*;
import Stream.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.Scanner;

/**
 * Created by pi on 6/29/17.
 */
public class Test {

    public static String test() {
        try {
            Scanner scanner=new Scanner(System.in);
            //read config file
            File configfile=new File("config.xml");
            JAXBContext ctx = JAXBContext.newInstance(ServerConfig.class);
            Unmarshaller um = ctx.createUnmarshaller();
            ServerConfig rootElement = (ServerConfig) um.unmarshal(configfile);


            //Set things up
            VideoRepository VR=new VideoRepository();
            GOPTaskScheduler GTS=new GOPTaskScheduler();
            VMProvisioner VMP=new VMProvisioner(2); //says we need at least two machines

            GTS.add_VM(ServerConfig.VM_address.get(0), ServerConfig.VM_ports.get(0)); //connect to that machine (localhost) and that port
            GTS.add_VM(ServerConfig.VM_address.get(1), ServerConfig.VM_ports.get(1));
            //TSC.add_VM(TC);

            //load Videos into Repository
            VR.addAllKnownVideos();

            // Check point, enter any key to continue
            System.out.println("two video loaded, enter any key to continue");
            scanner.next();


            // create Stream from Video, there are 3 constructor for Stream, two for making from only certain segment (not all)
            Stream ST=new Stream(VR.videos.get(0)); //admission control can work in constructor, or later?
            ST.setting = ServerConfig.defaultBatchScript; //setting creation or selection?

            //Admission Control assign Priority of each segments
            AdmissionControl.AssignStreamPriority(ST);
            for(StreamGOP x:ST.streamGOPs){
                System.out.println(x.getPriority());
            }
            //Scheduler
            GTS.addStream(ST);

            // Check point, enter any key to continue
            System.out.println("enter any key to terminate the system");
            scanner.next();

            //wind down process
            GTS.close();
            VMP.closeAll();
            return "success";
        } catch (Exception e) {
            return "Failed: " + e;
        }
    }
    //sandbox testing something strange, not really doing the program code
    private static String testbug() {
        try {

        } catch (Exception e) {
            return "Failed: " + e;
        }
        return "done";
    }
    //for test
    public static void main(String[] args){
        System.out.println(test());
        //System.out.println(testbug());
    }

}