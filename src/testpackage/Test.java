package testpackage;

import Scheduler.GOPTaskScheduler;
import Scheduler.GOPTaskScheduler_mergable;
import Scheduler.ServerConfig;
import Simulator.RequestGenerator;
import Repository.*;
import VMManagement.VMProvisioner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.Scanner;

import static java.lang.Thread.sleep;
import static testpackage.RealModeTest.WebRequestTest;

/**
 * Created by pi on 6/29/17.
 */
public class Test {

    public static String test(String confFile, String opt) {
        try {
            Scanner scanner = new Scanner(System.in);
            //read config file

            File configfile = new File("config/" + confFile);
            JAXBContext ctx = JAXBContext.newInstance(ServerConfig.class);
            Unmarshaller um = ctx.createUnmarshaller();
            ServerConfig rootElement = (ServerConfig) um.unmarshal(configfile);

            //Set things up
            VideoRepository VR = new VideoRepository();
            GOPTaskScheduler GTS = new GOPTaskScheduler_mergable();
            GTS.readcommonOperations();
            VMProvisioner VMP = new VMProvisioner(GTS, ServerConfig.minVM); //says we need at least two machines
            //VMP.setGTS(GTS);
            //load Videos into Repository
            VR.addAllKnownVideos();


            int rqn = 1, interval, n;
            if (ServerConfig.profiledRequests) {
                if (opt.equalsIgnoreCase("config")) {
                    RequestGenerator.ReadProfileRequests(ServerConfig.profileRequestsBenhmark);
                } else {
                    System.out.println("overwrite profileRequestBenhmark with " + opt);
                    ServerConfig.profileRequestsBenhmark = opt;
                    RequestGenerator.ReadProfileRequests(opt);
                }
                RequestGenerator.contProfileRequestsGen(GTS);
                while (!RequestGenerator.finished) {
                    sleep(300);
                }
                System.out.println("\nAll request have been released\n");

                while (!GTS.emptyQueue()) {
                    System.out.println("wait for pending work to finish");
                    sleep(300);
                }
                System.out.println("All queue are emptied");
            } else if (ServerConfig.openRequests) {
                ////create open socket, receive new profile request then do similar to profiledRequests
                IOWindows.Webservicegate webrqgate=new IOWindows.Webservicegate();
                webrqgate.addr="http://localhost:9902/transcoderequest";
                webrqgate.GTS=GTS;

                // example of actual request: http://localhost:9902/transcoderequest/?videoid=1,cmd=resolution,setting=180
                // (assume 10 is id of bigbugbunny
                // TODO: figure about timing of the request, both deadline and arrival (in webservicegate class)
                webrqgate.startListener();
                System.out.println("webservice enabled");
            } else {
                while (rqn != 0) {
                    System.out.println("enter video request numbers to generate and their interval and how many times");
                    rqn = scanner.nextInt();
                    interval = scanner.nextInt();
                    n = scanner.nextInt();
                    //create a lot of request to test out
                    RequestGenerator.nRandomRequest(GTS, rqn, interval, n);
                }
            }
            // Check point, enter any key to continue
            //System.out.println("enter any key to terminate the system");
            //scanner.next();
            sleep(300);
            //wind down process
            GTS.close();
            VMP.closeAll();
            return "success";

        } catch (Exception e) {
            return "Failed: " + e;
        }

    }

    //sandbox testing something strange, not really doing the program code
    private static String testbug(int seed) {
        Scanner scanner = new Scanner(System.in);
        //read config file

        File configfile = new File("config/config.xml");
        JAXBContext ctx = null;
        try {
            ctx = JAXBContext.newInstance(ServerConfig.class);

            Unmarshaller um = ctx.createUnmarshaller();
            ServerConfig rootElement = (ServerConfig) um.unmarshal(configfile);

            //load video repo so we know their v numbers
            VideoRepository VR = new VideoRepository();
            VR.addAllKnownVideos();
            //sweep create many requests
            if (seed == 0) {
                //int[] sr={699,1911,16384,9999,555,687,9199,104857,212223,777}; // first 10
                int[] sr = {1920, 1080, 768, 1990, 4192, 262144, 800, 12345, 678, 521, 50, 167, 1, 251, 68, 6, 333, 1048575, 81, 7};
                for (int j = 0; j < sr.length; j++) {
                    for (int i = 2000; i <= 3400; i += 200) {
                        RequestGenerator.generateProfiledRandomRequests("test" + i + "r_180000_10000_3000_s" + sr[j], sr[j], 27, 4, i, 180000, 10000, 3000);
                    }
                }
            } else {
                for (int i = 2000; i <= 3400; i += 200) {
                    RequestGenerator.generateProfiledRandomRequests("test" + i + "r_180000_10000_3000_s" + seed, seed, 27, 4, i, 180000, 10000, 3000);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "done";
    }

    private static void DirectoryTest() {
        System.out.println("Directory Test");

        File configfile = new File("config/config.xml");
        JAXBContext ctx = null;
        try {
            ctx = JAXBContext.newInstance(ServerConfig.class);

            Unmarshaller um = ctx.createUnmarshaller();
            ServerConfig rootElement = (ServerConfig) um.unmarshal(configfile);

            //load video repo so we know their v numbers
            VideoRepository VR = new VideoRepository();
            VR.addAllKnownVideos();
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        System.out.println(ServerConfig.repository);

        for (int i = 0; i < VideoRepository.videos.size(); i++) {
            System.out.println(VideoRepository.videos.get(i).name);
        }
    }


    //for test
    public static void main(String[] args) {


        if (args.length > 1) {
            if (args[0].equalsIgnoreCase("makeconfig")) {
                System.out.println(testbug(Integer.parseInt(args[1])));
            } else { //run
                System.out.println(test(args[2], args[1]));
            }
        } else {
            //System.out.println(testbug(0));
            System.out.println(test("config.xml", "test3000r_180000_10000_3000_s1920.txt"));
            /*
    try {
        RealModeTest.RealLocalThreads();
    }catch(Exception e){
        System.out.println("main fn error "+e);
    }
            //DirectoryTest();
            */
        }
    }

}