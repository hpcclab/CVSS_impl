package mainPackage;

import Cache.Caching;
import IOWindows.OutputWindow;
import IOWindows.WebserviceRequestGate;
import Repository.VideoRepository;
import ResourceManagement.ResourceProvisioner;
import Scheduler.AdmissionControl;
import Scheduler.GOPTaskScheduler_common;
//import Scheduler.GOPTaskScheduler_mergable;
import Scheduler.SystemConfig;
import SessionPkg.SessionManager;
import Simulator.RequestGenerator_GOPlevel;
import TimeEstimatorpkg.TimeEstNone;

import java.util.Scanner;

import static java.lang.Thread.sleep;

public class RealModeTest {
    private static void setUpCVSE_forreal(String configfile){
        //Set things up
        CVSE.config=new SystemConfig(configfile);
        CVSE.VR = new VideoRepository();
        CVSE.VR.addAllKnownVideos();
        CVSE.AC = new AdmissionControl();
        CVSE.GTS = new GOPTaskScheduler_common();
        CVSE.GTS.readlistedOperations();
        CVSE.TE=new TimeEstNone(); //using no TimeEstimator
        CVSE.VMP= new ResourceProvisioner( CVSE.config.minCR); //says we need at least two machines
        CVSE.CACHING = new Caching(); //change to other type if need something that work
        CVSE.OW=new OutputWindow(); //todo, actually call its function from VMP

        //VMP.setGTS(GTS);
        //load Videos into Repository
        CVSE.VR.addAllRealVideos();
        CVSE.RG= new RequestGenerator_GOPlevel();
    }
    public static String trysleep(int time){
        try {
            sleep(time);
        }catch (Exception e) {
            return "Failed: " + e;
        }
        return "";
    }

    public static void WebRequestTest() {


        //Step 1: Retrieve Real Videos from Video Repository

        SessionManager SM = new SessionManager();
        //setUpCVSE_forreal("config/nuConfigWeb.properties");
        setUpCVSE_forreal("config/nuConfig.properties");
        ////create open socket, receive new profile request then do similar to profiledRequests
        CVSE.WG=new WebserviceRequestGate();
        CVSE.WG.addr="http://localhost:9901/transcoderequest";
        if(SM == null){
            System.out.println("SM is null " + SM);
        }
        else{
            System.out.println("SM is fine " + SM);
        }

        CVSE.WG.SM = SM;

        // example of actual request: http://localhost:9901/transcoderequest/?videoid=1,cmd=resolution,setting=180
        // (assume 10 is id of bigbuckbunny
        // TODO: figure about timing of the request, both deadline and arrival (in webservicegate class)
        ///*
        try {
            CVSE.WG.startListener();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("webservice enabled");
        if(CVSE.WG.SM == null){
            System.out.println("webgate SM is null " + CVSE.WG.SM);
        }
        else{
            System.out.println("webgate SM is fine " + CVSE.WG.SM);
        }
        while (true) {
            trysleep(4000);
           // CVSE.RG.contProfileRequestsGen();
            CVSE.GTS.taskScheduling();
            System.out.println("wait for sim to finish");

            //CVSE.RG.contProfileRequestsGen(); //probably good idea to call here...
        }
        //*/
        // int num = Integer.MAX_VALUE;

        /*
        //Step 2 Request a stream by requesting an index number
        while (num != 0) {

            Scanner scanner=new Scanner(System.in);

            for (int i=0;i< VideoRepository.videos.size();i++){
                System.out.println(VideoRepository.videos.get(i).name + ": " + i);
            }
            System.out.println("Enter the video that you would like to have streamed: ");
            num = scanner.nextInt();

            Settings newSettings = new Settings();

            newSettings.resolution = true;
            newSettings.resWidth = "640";
            newSettings.resHeight = "480";
            newSettings.videoname = VideoRepository.videos.get(num).name;

            SM.InitializeStream(num, newSettings, GTS);
        }

        //VMP.closeAll();
        //GTS.close();

        */
    }

    //for test
    public static void main(String[] args) {
        WebRequestTest();
    }

}