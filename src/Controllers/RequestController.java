package Controllers;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by pi on 10/28/17.
 */
@WebServlet(name = "RequestController", urlPatterns = "/processrequest")
public class RequestController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println(request);
        System.out.println(response);
        RequestResult webrequest = RequestResult.fromRequestParameters(request);
        webrequest.setAsRequestAttributes(request);

        System.out.println(webrequest.name);
        System.out.println(webrequest.resolution);
        System.out.println(webrequest.rHeight);
        System.out.println(webrequest.rWidth);

        //forwardResponse(url, request, response);
    }

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
