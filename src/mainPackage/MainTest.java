package mainPackage;

import Cache.Caching;
import IOWindows.OutputWindow;
import IOWindows.WebserviceRequestGate;
import Repository.ServiceRepository;
import Repository.VideoRepository;
import ResourceManagement.ResourceProvisioner;
import Scheduler.AdmissionControl;
import Scheduler.GOPTaskScheduler_common;
//import Scheduler.GOPTaskScheduler_mergable;
import Scheduler.SystemConfig;
import Simulator.RequestGenerator_GOPlevel;
import TimeEstimatorpkg.TimeEstNone;
import TimeEstimatorpkg.TimeEstProfileMode;


import static java.lang.Thread.sleep;
import static mainPackage.SimMode.Sim;
import static mainPackage.SimMode.genbenchmarkTrace;

//this is now a real entrance of the program
public class MainTest {
    private static void setUpCVSE_common(String configfile,String overwriteOpt){
        CVSE.config=new SystemConfig(configfile);
        if(overwriteOpt!=null){
            CVSE.config.profileRequestsBenchmark=overwriteOpt;
        }
        CVSE.VR = new VideoRepository();
        CVSE.VR.addAllKnownVideos();
        CVSE.AC = new AdmissionControl();
        CVSE.GTS = new GOPTaskScheduler_common();
        CVSE.SR =new ServiceRepository();

        CVSE.SR.readlistedOperations();
        CVSE.CACHING = new Caching(); //change to other type if need something that work
        CVSE.OW=new OutputWindow(); //todo, actually call its function from RP

        //RP.setGTS(GTS);
        //load Videos into Repository
        //CVSE.VR.addAllRealVideos(); //not needed, all profile included in sim
        CVSE.RG= new RequestGenerator_GOPlevel();

    }
    //set something different between real mode and sim mode?
    private static void setUpCVSE_forreal(){
        CVSE.TE=new TimeEstNone(); //using no TimeEstimator
        CVSE.RP = new ResourceProvisioner( CVSE.config.minCR); //says we need at least two machines
    }
    private static void setUpCVSE_forsim(){
        CVSE.TE=new TimeEstProfileMode();
        CVSE.RP = new ResourceProvisioner( CVSE.config.minCR); //says we need at least two machines
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

        System.out.println("Starting WebRequest listening");
        //Step 1: Retrieve Real Videos from Video Repository
//        SessionManager SM = new SessionManager();
//        CVSE.WG.SM = SM;
        ////create open socket, receive new profile request then do similar to profiledRequests
        CVSE.WG=new WebserviceRequestGate();
        CVSE.WG.addr="http://localhost:9901/transcoderequest";
//        if(SM == null){
//            System.out.println("SM is null " + SM);
//        }
//        else{
//            System.out.println("SM is fine " + SM);
//        }



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
    }

    //for test

    public static void start(String confFile, String opt){
        //catch common mistake of input arguments
        if(opt.contains("BenchmarkInput/")){
            opt=opt.replaceFirst("BenchmarkInput/","");
            System.out.println("config trim BenchmarkInput/ out");
        }
        if(confFile.contains("config/")){ // will auto insert anyway
            opt=opt.replaceFirst("config/","");
        }

        setUpCVSE_common("config/"+confFile,opt);

        //if(CVSE.config.Pool)
        if(CVSE.config.openWebRequests==true){ //real mode
            setUpCVSE_forreal(); //for web request real mode
            WebRequestTest();
        }else{ //sim mode
            System.out.println("Sim user Input");
            setUpCVSE_forsim(); //for web request with simulation
            Sim(confFile,opt);
        }
    }

    public static void main(String[] args) {
        if (args.length > 1) { //if having overwrite args
            if (args[0].equalsIgnoreCase("makeconfig")) { //special mode to generate bechmark input
                System.out.println("run special mode to generate benchmark request");
                setUpCVSE_common(args[2], args[1]);
                setUpCVSE_forsim();

                System.out.println(genbenchmarkTrace(Integer.parseInt(args[1])));
            } else { //run
                System.out.println("ru6n "+args[2]+" with "+args[1]);

                start(args[2], args[1]);
            }
        }else {
            //for sim mode
            String Configname="testConfig8Cold.properties"; // simModeTest

            //for real mode
            //String Configname="nuConfigWeb.properties"; // realModeTest

            //String benchmarkname=""; //avg 0.3s per task, so
            String benchmarkname="start0_400r_100000_20000_3000_s555";
//            String Configname="nuConfig.properties"; // simModeTest
//
//            String benchmarkname="start0_1200r_180000_10000_3000_s555";
            start(Configname, benchmarkname+".txt");

            /// Force gen benchmark
//            String benchmarkname="xshortTest";
//            String Configname="nuconfig_simlocal.properties"; // realModeTest
//            setUpCVSE_common("config/"+Configname, "BenchmarkInput/"+benchmarkname+".txt");
//            setUpCVSE_forsim();
//            genbenchmarkTrace(0);

            System.out.println("exiting");
            System.exit(1); //make sure the program exit
            System.out.println("why am I here?");

        }

    }

}