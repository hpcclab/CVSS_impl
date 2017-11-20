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
            VMProvisioner VMP=new VMProvisioner(2); //says we need at least two machines
            GOPTaskScheduler GTS=new GOPTaskScheduler();

            //load Videos into Repository
            VR.addAllKnownVideos();


            // Check point, enter any key to continue
            System.out.println("system start and video loaded, enter any key to continue");
            scanner.next();
            //create a lot of request to test out
            RequestGenerator.nRandomRequest(GTS,6,500);

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