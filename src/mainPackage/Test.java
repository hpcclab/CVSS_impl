package mainPackage;

import Cache.Caching;
import IOWindows.OutputWindow;
import IOWindows.WebserviceRequestGate;
import Repository.VideoRepository;
import ResourceManagement.ResourceProvisioner;
import Scheduler.AdmissionControl;
import Scheduler.GOPTaskScheduler_mergable;
import Scheduler.SystemConfig;
import Simulator.RequestGenerator;
import TimeEstimatorpkg.TimeEstProfileMode;

import java.util.Scanner;

import static java.lang.Thread.sleep;

/**
 * Created by pi on 6/29/17.
 */
public class Test {
    private static void setUpCVSE_forsim(String configfile){
        //Set things up
        CVSE.config=new SystemConfig(configfile);
        CVSE.VR = new VideoRepository();
        CVSE.VR.addAllKnownVideos();
        CVSE.AC = new AdmissionControl();
        CVSE.GTS = new GOPTaskScheduler_mergable();
        CVSE.GTS.readlistedOperations();
        CVSE.TE=new TimeEstProfileMode();
        CVSE.VMP= new ResourceProvisioner(CVSE.config.minCR); //says we need at least two machines
        CVSE.CACHING = new Caching(); //change to other type if need something that work
        CVSE.OW=new OutputWindow(); //todo, actually call its function from VMP
        //VMP.setGTS(GTS);
        //load Videos into Repository
        CVSE.RG= new RequestGenerator();

    }

    public static String test(String confFile, String opt) {
        try {
            Scanner scanner = new Scanner(System.in);
            //read config file

            setUpCVSE_forsim("config/" + confFile);

            int rqn = 1, interval, n;
            if (CVSE.config.profiledRequests) {
                if (opt.equalsIgnoreCase("config")) {
                    CVSE.RG.ReadProfileRequests(CVSE.config.profileRequestsBenchmark);
                } else {
                    System.out.println("overwrite profileRequestBenhmark with " + opt);
                    CVSE.config.profileRequestsBenchmark = opt;
                    CVSE.RG.ReadProfileRequests(opt);
                }
                //sleep(3000);
                System.out.println("start sim");
                CVSE.RG.contProfileRequestsGen();
                while (!CVSE.RG.finished) {
                    sleep(300);
                    System.out.println("wait for sim to finish");
                }
                System.out.println("\nAll request have been released\n");

                while (!CVSE.GTS.emptyQueue()) {
                    System.out.println("wait for pending work to finish");
                    sleep(300);
                }
                System.out.println("All queue are emptied");
            } else if (CVSE.config.openWebRequests) {
                ////create open socket, receive new profile request then do similar to profiledRequests
                CVSE.WG=new WebserviceRequestGate();
                CVSE.WG.addr="http://localhost:9902/transcoderequest";

                // example of actual request: http://localhost:9902/transcoderequest/?videoid=1,cmd=resolution,setting=180
                // (assume 10 is id of bigbugbunny
                // TODO: figure about timing of the request, both deadline and arrival (in webservicegate class)
                CVSE.WG.startListener();
                System.out.println("webservice enabled");
            } else {
                while (rqn != 0) {
                    System.out.println("enter video request numbers to generate and their interval and how many times");
                    rqn = scanner.nextInt();
                    interval = scanner.nextInt();
                    n = scanner.nextInt();
                    //create a lot of request to test out
                    CVSE.RG.nRandomRequest(rqn, interval, n);
                }
            }
            // Check point, enter any key to continue
            //System.out.println("enter any key to terminate the system");
            //scanner.next();
            //wind down process
            if(CVSE.VMP==null){
                System.out.println("VMP is down");
            }else{
                if(CVSE.VMP.DU==null){
                    System.out.println("DU is down");
                }
                System.out.println("Okay");
            }
            CVSE.VMP.DU.printstat();
            CVSE.VMP.DU.graphplot();

            sleep(300);
            CVSE.GTS.close();
            CVSE.VMP.closeAll();

            return "success";

        } catch (Exception e) {
            return "Failed: " + e;
        }

    }

    //sandbox testing something strange, not really doing the program code
    private static String testbug(int seed) {



        CVSE _CVSE=new CVSE();
            setUpCVSE_forsim("config/nuConfig.properties");
        if (seed == 0) {
            //int[] sr={699,1911,16384,9999,555,687,9199,104857,212223,777}; // first 10
            int[] sr = {1920, 1080, 768, 1990, 4192, 262144, 800, 12345, 678, 521, 50, 167, 1, 251, 68, 6, 333, 1048575, 81, 7};
            for (int j = 0; j < sr.length; j++) {
                for (int i = 2000; i <= 3400; i += 200) {
                    _CVSE.RG.generateProfiledRandomRequests("nocodec" + i + "r_180000_10000_3000_s" + sr[j], sr[j], 88, i, 180000, 10000, 3000);
                }
            }
        } else {
            for (int i = 2000; i <= 3400; i += 200) {
                _CVSE.RG.generateProfiledRandomRequests("nocodec" + i + "r_180000_10000_3000_s" + seed, seed, 88, i, 180000, 10000, 3000);
            }
        }


        //CVSE.VMP.DU.graphplot();

        return "done";
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
            System.out.println(test("nuConfig.properties", "config"));
        }
    //System.out.println(testbug(0));
            //DirectoryTest();


    }

}