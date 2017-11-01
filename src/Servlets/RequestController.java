package Servlets;

import Scheduler.AdmissionControl;
import Scheduler.ServerConfig;
import Stream.Settings;
import Stream.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        Settings userRequest = new Settings(request);
        //start video processing
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(userRequest.outputDir());
        //

        //
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    public void InitializeStream(){
        // create Stream from Video, there are 3 constructor for Stream, two for making from only certain segment (not all)
        Stream ST=new Stream(VR.videos.get(0)); //admission control can work in constructor, or later?
        ST.setting = ServerConfig.defaultBatchScript; //setting creation or selection?

        //Admission Control assign Priority of each segments
        AdmissionControl.AssignStreamPriority(ST);
        for(StreamGOP x:ST.streamGOPs){
            System.out.println(x.getPriority());
        }
        //Scheduler
        GTS.addStream(ST);
    }
}
