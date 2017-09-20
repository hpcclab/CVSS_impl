package Servlet;

/**
 * Created by pi on 7/13/17.
 */
import GOP.GOP;
import TranscodingVM.TranscodingVM;

import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;

public class Servlet extends HttpServlet{
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        response.setContentType("text/html");
        PrintWriter pw = response.getWriter();

        pw.println("<p>");
        pw.println("Hello1");
        pw.println("</p>");

        pw.close();

        try {
            TranscodingVM t = new TranscodingVM();
            GOP gop = new GOP("/home/pi/apache-tomcat-7.0.78/webapps/CVSS_Implementation_Interface_war/videos/bbb_trailer.mp4");
            t.AddJob(gop);
            t.TranscodeSegment();
        } catch (Exception e) {
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        response.setContentType("text/html");
        PrintWriter pw = response.getWriter();

        pw.println("<p>");
        pw.println("Hello2");
        pw.println("</p>");

        pw.close();

        try {
            TranscodingVM t = new TranscodingVM();
            GOP gop = new GOP("/home/pi/apache-tomcat-7.0.78/webapps/CVSS_Implementation_Interface_war/videos/bbb_trailer.mp4");
            t.AddJob(gop);
            t.TranscodeSegment();
        } catch (Exception e) {
        }
    }
}
