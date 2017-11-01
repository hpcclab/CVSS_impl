package Servlets;

import Controller.GlobalController;
import Repository.VideoRepository;
import Scheduler.GOPTaskScheduler;
import Scheduler.ServerConfig;
import TranscodingVM.VMProvisioner;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * Created by pi on 11/1/17.
 */
public class ApplicationListener implements ServletContextListener {
    private ServletContext sc = null;

    VideoRepository VR;
    VMProvisioner VMP;
    GOPTaskScheduler GTS;

    public void contextInitialized(ServletContextEvent arg0) {
        this.sc = arg0.getServletContext();
        try {
            // initialization code
            GlobalController.InitializeComponents(2);
        } catch (Exception e) {
            System.out.println("oops");
        }
        System.out.println("webapp started");
    }

    public void contextDestroyed(ServletContextEvent arg0) {
        try {
            // shutdown code
            GlobalController.DestroyComponents();
        } catch (Exception e) {
            System.out.println("oops");
        }
        this.sc = null;
        System.out.println("webapp stopped");
    }
}
