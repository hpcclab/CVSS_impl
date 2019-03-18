package testpackage;

import DockerManagement.DockerManager;
import Repository.VideoRepository;
import Scheduler.GOPTaskScheduler;
import Scheduler.GOPTaskScheduler_mergable;
import Scheduler.ServerConfig;
import Streampkg.Settings;
import Streampkg.StreamManager;
import VMManagement.VMProvisioner;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class RealModeTest {
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

        StreamManager SM = new StreamManager();

        VideoRepository VR = new VideoRepository();
        VR.addAllRealVideos();

        //Step 1a Create threads that wait for requests upon an entry of the index of a real video

        //seems fine for tbe most part
        GOPTaskScheduler GTS=new GOPTaskScheduler_mergable();

        //checking...
        VMProvisioner VMP=new VMProvisioner(GTS,ServerConfig.minVM); //says we need at least two machines

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

            SM.InitializeStream(num, newSettings, GTS);
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
        GTS.close();
        VMP.closeAll();

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

        VideoRepository VR = new VideoRepository();
        VR.addAllRealVideos();

        //Step 1a Create threads that wait for requests upon an entry of the index of a real video

        //seems fine for tbe most part
        GOPTaskScheduler GTS=new GOPTaskScheduler_mergable();

        //checking...
        VMProvisioner VMP=new VMProvisioner(GTS,ServerConfig.minVM); //says we need at least two machines

        ////create open socket, receive new profile request then do similar to profiledRequests
        IOWindows.Webservicegate webrqgate=new IOWindows.Webservicegate();
        webrqgate.addr="http://localhost:9901/transcoderequest";
        webrqgate.GTS=GTS;
        if(SM == null){
            System.out.println("SM is null " + SM);
        }
        else{
            System.out.println("SM is fine " + SM);
        }

        webrqgate.SM = SM;

        // example of actual request: http://localhost:9901/transcoderequest/?videoid=1,cmd=resolution,setting=180
        // (assume 10 is id of bigbuckbunny
        // TODO: figure about timing of the request, both deadline and arrival (in webservicegate class)
        ///*
        try {
            webrqgate.startListener();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("webservice enabled");
        if(webrqgate.SM == null){
            System.out.println("webgate SM is null " + webrqgate.SM);
        }
        else{
            System.out.println("webgate SM is fine " + webrqgate.SM);
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

    private static void CreateContainerTest() throws InterruptedException, DockerException, DockerCertificateException {
        DockerManager.CreateDockerClient();
    }


    public static void main(String[] args) throws IOException, InterruptedException, DockerException, DockerCertificateException {
        //WebRequestTest();
        CreateContainerTest();
    }
}