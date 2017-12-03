package Controller;

import Scheduler.ServerConfig;
import Singletons.GTSSingleton;
import Singletons.VMPSingleton;
import Singletons.VRSingleton;
import TranscodingVM.VMProvisioner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;

import static Singletons.GTSSingleton.GTS;
import static Singletons.VMPSingleton.VMP;
import static Singletons.VRSingleton.VR;

/**
 * Created by pi on 11/1/17.
 */
public class GlobalController {

    public static void InitializeComponents(int initialClusterSize){
        File configfile=new File("config.xml");
        try {
            JAXBContext ctx = JAXBContext.newInstance(ServerConfig.class);
            Unmarshaller um = ctx.createUnmarshaller();
            ServerConfig rootElement = (ServerConfig) um.unmarshal(configfile);
        }
        catch(Exception e){ }

        VRSingleton VRS = VRSingleton.getInstance();
        try{
            //VMProvisioner VMP = new VMProvisioner(ServerConfig.minVM);
            VMPSingleton VMPS = VMPSingleton.getInstance(ServerConfig.minVM);
        }
        catch (Exception e){
            System.out.println("Provisioner error " + e.toString());
        }
        GTSSingleton GTSS = GTSSingleton.getInstance();

        //load Videos into Repository
        VR.addAllKnownVideos();
    }

    public static void DestroyComponents(){
        GTS.close();
        VMP.closeAll();
    }

}
