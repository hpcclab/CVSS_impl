package Servlets;

import CloudTesting.s3Control;
import Scheduler.AdmissionControl;
import Scheduler.ServerConfig;
import Stream.Settings;
import Stream.Stream;
import Stream.StreamGOP;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.S3ClientOptions;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

import static Singletons.GTSSingleton.GTS;
import static Singletons.VRSingleton.VR;

/**
 * Created by pi on 10/28/17.
 */
//@WebServlet(name = "RequestController", urlPatterns = "/processrequest")
public class RequestController extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("In servlet");
        String path = request.getContextPath();
        String absPath = "/home/pi/Documents/VHPCC/workspace/CVSS_impl";
        Settings userRequest = new Settings(request);
        userRequest.absPath = absPath;

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        //response.getWriter().write(userRequest.outputDir() + "/out.m3u8");
        String test = userRequest.videoDir();
        //start video processing
        //

        String prefix = "";

        if(ServerConfig.file_mode.equalsIgnoreCase("S3")) {

            AWSCredentials credentials = new BasicAWSCredentials("AKIAIWLF5HX335BP23RQ", "JP0AWhKmzMvV15Lq69/Az3jJZxUF2FxKvybDyFem");

            Region region = Region.getRegion(Regions.US_EAST_2);

            String bucket_name = "cvss-video-bucket";

            AmazonS3Client s3 = new AmazonS3Client(credentials);

            //AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_2).withForceGlobalBucketAccessEnabled(true).build();

          //  s3.setS3ClientOptions(S3ClientOptions.builder().setPathStyleAccess(true).disableChunkedEncoding().build());

            boolean exists = s3.doesBucketExist(bucket_name);

            CreateS3Dir(userRequest, s3);

            prefix = "http://cvss-video-bucket.s3.amazonaws.com/";
            //prefix = "http://d2fl8y9ld5lot6.cloudfront.net/";
        }

        //if (CreateDirectory(userRequest)){
        InitializeStream(userRequest);
        //}

        response.getWriter().write(prefix + userRequest.videoDir());
        //
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    public void CreateS3Dir(Settings userRequest, AmazonS3 s3){
        String bucket_name = "cvss-video-bucket";

       // s3.setS3ClientOptions(S3ClientOptions.builder().setPathStyleAccess(true).disableChunkedEncoding().build());

        try {
            s3Control.CreateVideoDirectory("cvss-video-bucket", userRequest.videoname, userRequest.videoname+userRequest.resWidth+userRequest.resHeight, s3);
        } catch (IOException e) {
            e.printStackTrace();
        }
      //  */
    }


    public boolean CreateDirectory(Settings userRequest){
        File dir = new File(userRequest.outputDir());
        if (dir.exists()){
            return false;
        }

        String absPath = "/home/pi/Documents/VHPCC/workspace/CVSS_impl";

        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));

        String[] command = {"bash",ServerConfig.path + "bash/createDir.sh", userRequest.outputDir(),ServerConfig.path, userRequest.videoname};

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT); //debug,make output from bash to screen
            pb.redirectError(ProcessBuilder.Redirect.INHERIT); //debug,make output from bash to screen

            pb.start();
            Process p = pb.start();
            p.waitFor();
        } catch (Exception e){

        }
        return true;
    }

    public void InitializeStream(Settings userRequest){
        System.out.println("before stream");
        // create Stream from Video, there are 3 constructor for Stream, two for making from only certain segment (not all)

        Settings newRequest = new Settings(userRequest.videoname, userRequest.resHeight, userRequest.resWidth);

        Stream ST=new Stream(VR.videos.get(0),newRequest); //admission control can work in constructor, or later?

        //Admission Control assign Priority of each segments
        AdmissionControl.AssignStreamPriority(ST);
        for(StreamGOP x:ST.streamGOPs){
            System.out.println(x.getPriority());
        }
        //Scheduler
        System.out.println("test1");
        GTS.addStream(ST);
        System.out.println("test2");
        System.out.println("after stream");
    }
}