package mainPackage;

import Cache.Caching;
import IOWindows.OutputWindow;
import Repository.VideoRepository;
import ResourceManagement.ResourceProvisioner;
import Scheduler.AdmissionControl;
import Scheduler.GOPTaskScheduler_mergable;
import Scheduler.ServerConfig;
import Simulator.RequestGenerator;
import TimeEstimatorpkg.TimeEstNone;
import TranscodingVM.TranscodingVM;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

import static java.lang.Thread.sleep;


//This class is meant to be main Jar of the cloud machine

public class cloudMain {
    private static void setUpCVSE_forNode(){
        //Set things up
        CVSE.VR = new VideoRepository();
        CVSE.AC = new AdmissionControl();
        //CVSE.GTS = new GOPTaskScheduler_mergable();
        //CVSE.GTS.readlistedOperations();
        CVSE.TE=new TimeEstNone(); //using no TimeEstimator
        //CVSE.VMP= new ResourceProvisioner( ServerConfig.minVM); //says we need at least two machines
        //CVSE.CACHING = new Caching(); //change to other type if need something that work
        //CVSE.OW=new OutputWindow(); //todo, actually call its function from VMP

        //VMP.setGTS(GTS);
        //load Videos into Repository
        CVSE.VR.addAllRealVideos();
        //CVSE.RG= new RequestGenerator();
    }

    public static void main(String[] args) {
        File configfile = new File("config/config_node.xml");
        JAXBContext ctx = null;
        try {
            ctx = JAXBContext.newInstance(ServerConfig.class);

            Unmarshaller um = ctx.createUnmarshaller();
            ServerConfig rootElement = (ServerConfig) um.unmarshal(configfile);

        } catch (JAXBException e) {
            e.printStackTrace();
        }
        setUpCVSE_forNode();

        //create
        TranscodingVM TC = new TranscodingVM("localContainer","","0.0.0.0", 0);
        //CVSE.TE.populate("localContainer");
        TC.start();
    }
}
