package Controllers;

import Stream.Settings;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.*;

/**
 * Created by pi on 10/28/17.
 */
//@WebServlet(name = "RequestController", urlPatterns = "/processrequest")
public class RequestController extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("beginning of servlet");
        Settings userRequest = new Settings();
        String name = request.getParameter("videoname");
        String resolution = request.getParameter("resolution");

        userRequest.videoname = name;

        if(resolution.equals(null))
        {
            userRequest.resolution = true;
            String[] resData = resolution.split("x");
            userRequest.resWidth = resData[0];
            userRequest.resHeight = resData[1];
        }

        System.out.println(name);
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(userRequest.outputDir());
        System.out.println("end of servlet");

    }

        protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
