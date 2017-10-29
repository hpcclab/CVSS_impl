package testpackage;

import Scheduler.AdmissionControl;
import Scheduler.GOPTaskScheduler;
import Scheduler.ServerSettings;
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
            TranscodingVM TC = new TranscodingVM(ServerSettings.VM_ports[0]); //new thread waiting at that port
            TranscodingVM TC2 = new TranscodingVM(ServerSettings.VM_ports[1]);
            TC.start();
            TC2.start();
            GTS.add_VM(ServerSettings.VM_address[0],ServerSettings.VM_ports[0]); //connect to that machine (localhost) and that port
            GTS.add_VM(ServerSettings.VM_address[1],ServerSettings.VM_ports[1]);
            //TSC.add_VM(TC);

            //load Videos into Repository
                //path to be changed
            Video V1=new Video("./repositoryvideos/ff_trailer_part1/");
            Video V2=new Video("./repositoryvideos/ff_trailer_part3/");

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
            GTS.close();
            TC.close();
            TC2.close();
            return "success";
        } catch (Exception e) {
            return "Failed: " + e;
        }
    }
    //sandbox testing something strange, not really doing the program code
    private static String testbug() {
        try  {


        } catch (Exception e) {
            return "Failed: " + e;
        }
        return "done";
    }
    //for test
    public static void main(String[] args){
        //System.out.println(test());
        System.out.println(testbug());
    }

}