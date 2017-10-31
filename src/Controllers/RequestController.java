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
//@WebServlet(name = "RequestController", urlPatterns = "/processrequest")
public class RequestController extends HttpServlet {


    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("beginning of servlet");

        String name = request.getParameter("videoname");
        String resolution = request.getParameter("resolution");

        if(name.equals("")){
            name = "Hello User";
        }
        name = "repositoryvideos/" + name + "/out.m3u8";
        System.out.println(name);
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(name);
        System.out.println("end of servlet");
    }
/*
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
*/
   ///*
        protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

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
