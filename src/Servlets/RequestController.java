package Servlets;

import Scheduler.AdmissionControl;
import Scheduler.ServerConfig;
import Stream.Settings;
import Stream.*;

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
        String absPath = "/home/pi/Documents/VHPCC/workspace/CVSS_impl";
        Settings userRequest = new Settings(request);
        userRequest.absPath = absPath;

        //check for existing output directory and create one if it does not exist
        //CreateDirectory(userRequest);

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        //response.getWriter().write(userRequest.outputDir() + "/out.m3u8");
        response.getWriter().write(userRequest.outputDir());
        //start video processing
        //
        //InitializeStream(userRequest);
        //
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    public void CreateDirectory(Settings userRequest){
        File dir = new File("web/" + userRequest.outputDir());
        if (dir.exists()){
            return;
        }

        String absPath = "/home/pi/Documents/VHPCC/workspace/CVSS_impl";

        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));

        String[] command = {"bash", absPath + "/bash/createDir.sh", userRequest.outputDir(),userRequest.absPath, userRequest.videoname};

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT); //debug,make output from bash to screen
            pb.redirectError(ProcessBuilder.Redirect.INHERIT); //debug,make output from bash to screen

            pb.start();
            Process p = pb.start();
            p.waitFor();
        } catch (Exception e){

        }
    }

    public void InitializeStream(Settings userRequest){
        // create Stream from Video, there are 3 constructor for Stream, two for making from only certain segment (not all)
        Stream ST=new Stream(VR.videos.get(0),userRequest); //admission control can work in constructor, or later?

        //Admission Control assign Priority of each segments
        AdmissionControl.AssignStreamPriority(ST);
        for(StreamGOP x:ST.streamGOPs){
            System.out.println(x.getPriority());
        }
        //Scheduler
        GTS.addStream(ST);
    }
}