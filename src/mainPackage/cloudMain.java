package mainPackage;

import Repository.VideoRepository;
import Scheduler.SystemConfig;
import TimeEstimatorpkg.TimeEstNone;
import TranscodingVM.TranscodingVM;

//import javax.xml.bind.JAXBContext;
//import javax.xml.bind.Unmarshaller;
import java.io.*;


//This class is meant to be main Jar of the cloud machine

public class cloudMain {
    public static void main(String[] args) throws InterruptedException {
//        String port="15061";
//        try {
//            BufferedReader idread= new BufferedReader(new FileReader("/home/shared/portid"));
//            String tmp;
//            while ((tmp = idread.readLine()) != null) {
//                port=tmp;
//            }
//            idread.close();
//
//            //DEBUG, ack what i've read
//            /*
//                BufferedWriter writer = new BufferedWriter(new FileWriter("/home/shared/portid_ack"));
//                writer.write(port);
//                writer.close();
//            */
//        }catch(Exception e){
//            System.out.println("read bug");
//        }
//        //create
//        TranscodingVM TC = new TranscodingVM("localContainer","chameleonBM","0.0.0.0", Integer.parseInt(port));
//        TC.start();
//        TC.join();
    }
}
