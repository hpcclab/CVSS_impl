package Scheduler;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ServerConfig {

    // questionable,debug settings
    public static String defaultInputPath; //
    public static String defaultOutputPath; //
    public static String defaultBatchScript; //
    public static ArrayList<String> VM_address=new ArrayList<>();
    public static ArrayList<Integer> VM_ports=new ArrayList<>();
    public static ArrayList<String> videoList=new ArrayList<>();

    // mainstay settings
    public static ArrayList<String> supportedCodecs=new ArrayList<>(8); //not being used right now
    //public static int maximumResolution;
    public static boolean enableTimeEstimator=false; //if true, use Time estimator to do scheduling
    public static boolean enableCaching=false; //if true, use caching system
    public static boolean enableVMscaling=false;
    public static int maxVM; //set max number of VMs
    public static int remedialVM_constantfactor=10; //default value=10
    public static int lowscalingThreshold; // for VM provisioning algorithms
    public static int highscalingThreshold;
    public static double c_const_for_utilitybased=0.1; //default value=0.1
    public static String mapping_mechanism;// can be either MM,MSD,MMU


    //check for each parameters if there is any Invalid
    public static boolean isSettingValid(){
        if(enableVMscaling){
            if(maxVM<1){
                System.out.println("maxVM must be >0");
                return false;
            }
            if((lowscalingThreshold<0)||(highscalingThreshold<0)||(lowscalingThreshold>1)||(highscalingThreshold>1)||(highscalingThreshold<lowscalingThreshold) ){
                System.out.println("Invalid scaling threshold");
                return false;
            }
        }

        if(c_const_for_utilitybased>1 || c_const_for_utilitybased<0){
            System.out.println("c_const_for utilitybased must be 0<c<1");
            return false;
        }

        if(!(mapping_mechanism.equals("RR")||mapping_mechanism.equals("MM")||mapping_mechanism.equals("MSD")||mapping_mechanism.equals("MMU"))){
            System.out.println("mapping_mechanism must be either RR, MM, MSD or MMU");
            return false;
        }

        //if all pass,
        return true;
    }

    ////belows are all function intended for unmasher code to use parsing config

    @XmlElement(name = "defaultInputPath")
    public void setDefaultInputPath(String defaultInputPath) {
        ServerConfig.defaultInputPath = defaultInputPath;
    }
    @XmlElement(name = "defaultOutputPath")
    public void setDefaultOutputPath(String defaultOutputPath) {
        ServerConfig.defaultOutputPath = defaultOutputPath;
    }
    @XmlElement(name = "defaultOutputPath")
    public void setDefaultBatchScript(String defaultBatchScript) {
        ServerConfig.defaultBatchScript = defaultBatchScript;
    }
    @XmlElement(name = "VM_address")
    public void setVM_address(String VM_address) {
        ServerConfig.VM_address.add(VM_address);
    }
    @XmlElement(name = "VM_ports")
    public void setVM_ports(int VM_ports) {
        ServerConfig.VM_ports.add(VM_ports);
    }
    @XmlElement(name = "supportedCodecs")
    public void setSupportedCodecs(String supportedCodecs) {
        ServerConfig.supportedCodecs.add(supportedCodecs);
    }
    @XmlElement(name = "videoList")
    public void setvideoList(String videoList) {
        ServerConfig.videoList.add(videoList);
    }
    @XmlElement(name = "enableTimeEstimator")
    public void setEnableTimeEstimator(String check) {
        if(check.equalsIgnoreCase("True")) {
            ServerConfig.enableTimeEstimator = true;
        }else{
            ServerConfig.enableTimeEstimator = false;
        }
    }
    @XmlElement(name = "enableCaching")
    public void setEnableCaching(String check) {
        if(check.equalsIgnoreCase("True")) {
            ServerConfig.enableCaching = true;
        }else{
            ServerConfig.enableCaching = false;
        }
    }
    @XmlElement(name = "enableVMscaling")
    public void setVMscaling(String check) {
        if(check.equalsIgnoreCase("True")) {
            ServerConfig.enableVMscaling = true;
        }else{
            ServerConfig.enableVMscaling = false;
        }
    }
    @XmlElement(name = "maxVM")
    public void setMaxVM(int maxVM) {
        ServerConfig.maxVM = maxVM;
    }
    @XmlElement(name = "remedialVM_constantfactor")
    public void setRemedialVM_constantfactor(int remedialVM_constantfactor) {
        ServerConfig.remedialVM_constantfactor = remedialVM_constantfactor;
    }
    @XmlElement(name = "lowscalingThreshold")
    public void setLowscalingThreshold(int lowscalingThreshold) {
        ServerConfig.lowscalingThreshold = lowscalingThreshold;
    }
    @XmlElement(name = "highscalingThreshold")
    public void setHighscalingThreshold(int highscalingThreshold) {
        ServerConfig.highscalingThreshold = highscalingThreshold;
    }
    @XmlElement(name = "c_const_for_utilitybased")
    public void setC_const_for_utilitybased(double c_const_for_utilitybased) {
        ServerConfig.c_const_for_utilitybased = c_const_for_utilitybased;
    }
    @XmlElement(name = "mapping_mechanism")
    public void setMapping_mechanism(String mapping_mechanism) {
        ServerConfig.mapping_mechanism = mapping_mechanism;
    }
}
