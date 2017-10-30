package Servlet;

/**
 * Created by pi on 7/13/17.
 */
import Repository.RepositoryGOP;
import TranscodingVM.TranscodingVM;

import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;

public class Servlet extends HttpServlet{

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String text = "Update maybe successfull"; //message you will recieve
        String name = request.getParameter("name");
        //PrintWriter out = response.getWriter();
        //out.println(name + " " + text);
        System.out.println(text + " " + name);

    }

    /*
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{

        response.setContentType("text/html");
        PrintWriter pw = response.getWriter();

        pw.println("<p>");
        pw.println("Hello1");
        pw.println("</p>");

        pw.close();

        try {
            //TranscodingVM t = new TranscodingVM();
            //RepositoryGOP repositoryGop = new RepositoryGOP("/home/pi/apache-tomcat-7.0.78/webapps/CVSS_Implementation_Interface_war/videos/bbb_trailer.mp4");
           // t.AddJob(repositoryGop);
        } catch (Exception e) {
        }
    }
    */

   /* public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        response.setContentType("text/html");
        PrintWriter pw = response.getWriter();

        pw.println("<p>");
        pw.println("Hello2");
        pw.println("</p>");

        pw.close();

        try {
            //TranscodingVM t = new TranscodingVM();
            //RepositoryGOP repositoryGop = new RepositoryGOP("/home/pi/apache-tomcat-7.0.78/webapps/CVSS_Implementation_Interface_war/videos/bbb_trailer.mp4");
            //t.AddJob(repositoryGop);
        } catch (Exception e) {
        }
    }*/
}
