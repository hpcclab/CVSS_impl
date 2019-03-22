package mainPackage;

import DockerManagement.DockerManager;
import Cache.Caching;
import IOWindows.OutputWindow;
import IOWindows.WebserviceRequestGate;
import Repository.VideoRepository;
import Scheduler.GOPTaskScheduler;
import Scheduler.GOPTaskScheduler_mergable;
import Scheduler.ServerConfig;
import Simulator.RequestGenerator;
import Streampkg.Settings;
import Streampkg.StreamManager;
import ResourceManagement.ResourceProvisioner;
import TimeEstimatorpkg.TimeEstimator;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class RealModeTest {

        private static void setUpCVSE_forreal(CVSE _CVSE){
            //Set things up
            _CVSE.VR = new VideoRepository(_CVSE);
            _CVSE.CACHING = new Caching(_CVSE); //change to other type if need something that work
            _CVSE.GTS = new GOPTaskScheduler_mergable(_CVSE);
            _CVSE.GTS.readlistedOperations();
            _CVSE.VMP= new ResourceProvisioner(_CVSE, ServerConfig.minVM); //says we need at least two machines
            _CVSE.OW=new OutputWindow(_CVSE); //todo, actually call its function from VMP
            _CVSE.TE=new TimeEstimator(_CVSE);
            //VMP.setGTS(GTS);
            //load Videos into Repository
            _CVSE.VR.addAllRealVideos();
            _CVSE.RG= new RequestGenerator(_CVSE);
        }

        public static void RealLocalThreads() throws IOException {

        File configfile = new File("config/config_web.xml");
        JAXBContext ctx = null;
        try {
            ctx = JAXBContext.newInstance(ServerConfig.class);

            Unmarshaller um = ctx.createUnmarshaller();
            ServerConfig rootElement = (ServerConfig) um.unmarshal(configfile);

        } catch (JAXBException e) {
            e.printStackTrace();
        }

        //Step 1: Retrieve Real Videos from Video Repository

        CVSE _CVSE=new CVSE();
        setUpCVSE_forreal(_CVSE);
        StreamManager SM = new StreamManager();
        //seems fine for tbe most part
        //checking...
        int num = Integer.MAX_VALUE;

        //Step 2 Request a stream by requesting an index number
        while (num != 0) {

            Scanner scanner = new Scanner(System.in);

            for (int i = 0; i < VideoRepository.videos.size(); i++) {
                System.out.println(VideoRepository.videos.get(i).name + ": " + i);
            }
            System.out.println("Enter the video that you would like to have streamed: ");
            num = scanner.nextInt();

            Settings newSettings = new Settings();

            newSettings.resolution = true;
            newSettings.resWidth = "640";
            newSettings.resHeight = "480";
            newSettings.videoname = VideoRepository.videos.get(num).name;

            SM.InitializeStream(num, newSettings, _CVSE.GTS);
        }

        for (int i = 0; i < VideoRepository.videos.size(); i++) {
            System.out.println(VideoRepository.videos.get(i).name);
        }

        /*

        while(!GTS.emptyQueue()){
                System.out.println("wait for pending work to finish");
                sleep(300);
            }

        System.out.println("All queue are emptied");

        //*/

        //wind down process
        _CVSE.GTS.close();
        _CVSE.VMP.closeAll();

        //Step 6: remove all the folders and contents in the streams folder
        /*
        try {
            SM.RemoveProcessedStreams();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //*/
    }
    public static void WebRequestTest() throws IOException {


        File configfile = new File("config/config_web.xml");
        JAXBContext ctx = null;
        try {
            ctx = JAXBContext.newInstance(ServerConfig.class);

            Unmarshaller um = ctx.createUnmarshaller();
            ServerConfig rootElement = (ServerConfig) um.unmarshal(configfile);

        } catch (JAXBException e) {
            e.printStackTrace();
        }
        //Step 1: Retrieve Real Videos from Video Repository

        StreamManager SM = new StreamManager();
        CVSE _CVSE=new CVSE();
        setUpCVSE_forreal(_CVSE);
        ////create open socket, receive new profile request then do similar to profiledRequests
        _CVSE.WG=new WebserviceRequestGate();
        _CVSE.WG.addr="http://localhost:9901/transcoderequest";
        if(SM == null){
            System.out.println("SM is null " + SM);
        }
        else{
            System.out.println("SM is fine " + SM);
        }

        _CVSE.WG.SM = SM;

        // example of actual request: http://localhost:9901/transcoderequest/?videoid=1,cmd=resolution,setting=180
        // (assume 10 is id of bigbuckbunny
        // TODO: figure about timing of the request, both deadline and arrival (in webservicegate class)
        ///*
        try {
            _CVSE.WG.startListener();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("webservice enabled");
        if(_CVSE.WG.SM == null){
            System.out.println("webgate SM is null " + _CVSE.WG.SM);
        }
        else{
            System.out.println("webgate SM is fine " + _CVSE.WG.SM);
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
    private static void DirectoryTest() {
        System.out.println("Directory Test");

        File configfile = new File("config/config.xml");
        JAXBContext ctx = null;
        try {
            ctx = JAXBContext.newInstance(ServerConfig.class);

            Unmarshaller um = ctx.createUnmarshaller();
            ServerConfig rootElement = (ServerConfig) um.unmarshal(configfile);

            //load video repo so we know their v numbers
            CVSE _CVSE=new CVSE();
            VideoRepository VR = new VideoRepository(_CVSE);
            VR.addAllKnownVideos();
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        System.out.println(ServerConfig.repository);

        for (int i = 0; i < VideoRepository.videos.size(); i++) {
            System.out.println(VideoRepository.videos.get(i).name);
        }
    }

    private static void CreateContainerTest() throws InterruptedException, DockerException, DockerCertificateException {
        DockerManager.CreateDockerClient();
    }


    public static void main(String[] args) throws IOException, InterruptedException, DockerException, DockerCertificateException {
        //WebRequestTest();
        CreateContainerTest();
    }
}