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
                RequestGenerator.ReadProfileRequests(opt);
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
        /*
        try {

            //this thing work!
            AmazonEC2 instance=AmazonEC2ClientBuilder.defaultClient(); //use system default credential also works
            //AmazonEC2 instance=AmazonEC2ClientBuilder.standard().withCredentials().build();
            StartInstancesRequest start=new StartInstancesRequest().withInstanceIds("i-0bce5f77aa6e0f3a2");
            StopInstancesRequest stop=new StopInstancesRequest().withInstanceIds("i-0bce5f77aa6e0f3a2");
            instance.startInstances(start);
            //wait until online then get IP
            sleep(5000);
            while(true){
                sleep(2000);
                List<InstanceStatus> poll=instance.describeInstanceStatus().getInstanceStatuses();
                //System.out.println("poll:"+poll);
                if(poll.size()>0){
                    System.out.println("poll:" +poll);
                    break;
                }
            }
            System.out.println(instance.describeNetworkInterfaces().getNetworkInterfaces().get(0).getAssociation().getPublicIp());
            //instance.
            //instance.stopInstances(stop);
            //instance.shutdown();
        } catch (Exception e) {
            return "Failed: " + e;
        }
        */
        try {
            RequestGenerator.generateDistributedRandomRequests("test",500,27,100,800000,40000,15000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "done";

    }
    //for test
    public static void main(String[] args){
        System.out.println(test("test.txt"));
        //System.out.println(testbug());
    }

}