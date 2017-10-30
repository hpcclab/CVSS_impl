package Controllers;

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
@WebServlet(name = "RequestController", urlPatterns = "/processrequest")
public class RequestController extends HttpServlet {


    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    /*
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String name = request.getParameter("videoName");
        String resolution = request.getParameter("resolution");
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(resolution +" "+ name);
        //String text = "Update successfull"; //message you will recieve
        //String name = request.getParameter("name");
        //PrintWriter out = response.getWriter();
      //  out.println(name + " " + text);
        // System.out.println(request);

        //  PrintWriter out = response.getWriter();
        //  out.println(text + " " + name);
    }
    */


    //@Override
    private void forwardResponse(HttpServletRequest request, HttpServletResponse response)
    {
        try {
            request.getRequestDispatcher("/index.jsp").forward(request, response);
        }
        catch (ServletException e){
            System.out.print(e);
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
            System.out.print(e);
        }

    }

   ///*
        protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println(request);
        System.out.println(response);

        String name = request.getParameter("videoName");
        String resolution = request.getParameter("resolution");

        System.out.println(name);
        System.out.print(resolution);
        //        RequestResult webrequest = RequestResult.fromRequestParameters(request);
       // webrequest.setAsRequestAttributes(request);

  //      System.out.println(webrequest.name);
    //    System.out.println(webrequest.resolution);
      //  System.out.println(webrequest.rHeight);
        //System.out.println(webrequest.rWidth);

        forwardResponse(request, response);
    }
    //*/

    private static class RequestResult {

        private final String name;
        private final String resolution;
        private final String rHeight;
        private final String rWidth;

        private RequestResult(String name,String resolution,String rHeight,String rWidth) {
            this.name = name;
            this.resolution = resolution;
            this.rHeight = rHeight;
            this.rWidth = rWidth;
        }

        public static RequestResult fromRequestParameters(HttpServletRequest request) {
            return new RequestResult(
                    request.getParameter("name"),
                    request.getParameter("resolution"),
                    request.getParameter("rHeight"),
                    request.getParameter("rWidth"));
        }

        public void setAsRequestAttributes(HttpServletRequest request) {
            request.setAttribute("name", name);
            request.setAttribute("resolution", resolution);
            request.setAttribute("rHeight", rHeight);
            request.setAttribute("rWidth", rWidth);
        }


    }

}
