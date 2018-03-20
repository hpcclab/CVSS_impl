package testpackage;

import Scheduler.AdmissionControl;
import Scheduler.GOPTaskScheduler;
import Scheduler.ServerConfig;
import TranscodingVM.*;
import Repository.*;
import Stream.*;
/*import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
*/
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.List;
import java.util.Scanner;

import static java.lang.Thread.sleep;

/**
 * Created by pi on 6/29/17.
 */
public class Test {

    public static String test(String opt) {
        /*
        AWSCredentials credentials = new BasicAWSCredentials(
                "AKIAJTLLH5SVF74IJ6NQ",
                "ZJCx4bcqYb78EZc/1d1foHSSgJSIDMhV+uiQkFKG");
        */
        try {
            Scanner scanner=new Scanner(System.in);
            //read config file

            File configfile=new File("config.xml");
            JAXBContext ctx = JAXBContext.newInstance(ServerConfig.class);
            Unmarshaller um = ctx.createUnmarshaller();
            ServerConfig rootElement = (ServerConfig) um.unmarshal(configfile);

            //Set things up
            VideoRepository VR=new VideoRepository();
            VMProvisioner VMP=new VMProvisioner(ServerConfig.minVM); //says we need at least two machines
            GOPTaskScheduler GTS=new GOPTaskScheduler();
            VMP.setGTS(GTS);
            //load Videos into Repository
            VR.addAllKnownVideos();


            int rqn=1,interval,n;
            if(ServerConfig.profiledRequests){
                if(opt.equalsIgnoreCase("config")){
                    RequestGenerator.ReadProfileRequests(ServerConfig.profileRequestsBenhmark);
                }else {
                    RequestGenerator.ReadProfileRequests(opt);
                }
                RequestGenerator.contProfileRequestsGen(GTS);
            }else {
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
            System.out.println("enter any key to terminate the system");
            scanner.next();

            //wind down process
            GTS.close();
            VMP.closeAll();
            return "success";
        } catch (Exception e) {
            return "Failed: " + e;
        }

    }
    //sandbox testing something strange, not really doing the program code
    private static String testbug() {
        Scanner scanner=new Scanner(System.in);
        //read config file

        File configfile=new File("config.xml");
        JAXBContext ctx = null;
        try {
            ctx = JAXBContext.newInstance(ServerConfig.class);

        Unmarshaller um = ctx.createUnmarshaller();
        ServerConfig rootElement = (ServerConfig) um.unmarshal(configfile);

        //load video repo so we know their v numbers
        VideoRepository VR=new VideoRepository();
            VR.addAllKnownVideos();
            RequestGenerator.generateProfiledRandomRequests("test130v_180000_10000_3000_s699",699,27,130,180000,10000,3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "done";
    }
    //for test
    public static void main(String[] args){
        //System.out.println(testbug());
        System.out.println(test("config"));
    }

}