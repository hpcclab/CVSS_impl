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
    public static String path;
    public static ArrayList<String> CR_type =new ArrayList<>();
    public static ArrayList<String> CR_class =new ArrayList<>();
    public static ArrayList<Boolean> CR_autoschedule =new ArrayList<>();
    public static ArrayList<String> CR_address =new ArrayList<>();
    public static ArrayList<Integer> CR_ports =new ArrayList<>();


    public static boolean addProfiledDelay=false; //this is profiled delay, from GOPS
    public static boolean consideratemerge =true; ////set to false for testing dumb merge
    public static String batchqueuesortpolicy="Deadline";
    public static boolean taskmerge=false;
    public static boolean profiledRequests=false;
    public static boolean openRequests=false;
    public static String profileRequestsBenhmark;
    public static boolean mergeOverwriteQueuePolicy=true;
    public static String overwriteQueuePolicyHeuristic;

    public static String outputDir;

    public static String run_mode="real";
    public static int localqueuelengthperCR=4;

    // mainstay settings
    public static ArrayList<String> supportedCodecs=new ArrayList<>(8); //not being used right now
    public static ArrayList<String> repository=new ArrayList<>();
    //public static int maximumResolution;
    public static boolean enableTimeEstimator=false; //if true, use Time estimator to do scheduling
    public static String timeEstimatorMode="";
    public static String schedulerPolicy;
    public static boolean enableCaching=false; //if true, use caching system
    public static boolean enableCRscaling=false;
    public static int dataUpdateInterval=1000; //millisecond, 0 to disable
    public static int CRscalingIntervalTick=10; //millisecond, 0 to disable
    public static boolean enableCRscalingoutofInterval=false;
    public static int maxCR; //set max number of ComputingResources
    public static int minCR; //set max number of ComputingResources
    public static int remedialVM_constantfactor=10; //default value=10
    public static double lowscalingThreshold; // for VM provisioning algorithms
    public static double highscalingThreshold;
    public static double c_const_for_utilitybased=0.1; //default value=0.1
    public static String mapping_mechanism;// can be either MM,MSD,MMU
    public static boolean useEC2;
    public static String file_mode="S3"; //can be S3 or
    //public static boolean mergeOverwriteQueuePolicy=true;

    //check for each parameters if there is any Invalid
    public static boolean isSettingValid(){
        if(enableCRscaling){
            if(maxCR<1){
                System.out.println("maxCR must be >0");
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
    @XmlElement(name = "path")
    public void setPath(String path) {
        ServerConfig.path = path;
    }
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
    @XmlElement(name = "addProfiledDelay")
    public void setAddProfiledDelay(String check) {
        if(check.equalsIgnoreCase("True")) {
            ServerConfig.addProfiledDelay = true;
        }else{
            ServerConfig.addProfiledDelay = false;
        }
    }
    @XmlElement(name = "consideratemerge")
    public void setconsideratemerge(String check) {
        if(check.equalsIgnoreCase("True")) {
            ServerConfig.consideratemerge = true;
        }else{
            ServerConfig.consideratemerge = false;
        }
    }
    @XmlElement(name = "batchqueuesortpolicy")
    public void setbatchqueuesortpolicy(String defaultInputPath) {
        ServerConfig.batchqueuesortpolicy = defaultInputPath;
    }
    @XmlElement(name = "profiledRequests")
    public void setprofiledRequests(String check) {
        if(check.equalsIgnoreCase("True")) {
            ServerConfig.profiledRequests = true;
        }else{
            ServerConfig.profiledRequests = false;
        }
    }
    @XmlElement(name = "openWebRequests")
    public void setopenRequests(String check) {
        if(check.equalsIgnoreCase("True")) {
            ServerConfig.openRequests = true;
        }else{
            ServerConfig.openRequests = false;
        }
    }
    @XmlElement(name = "profileRequestsBenhmark")
    public void setprofileRequestsBenhmark(String input) {
        ServerConfig.profileRequestsBenhmark=input;
    }
    @XmlElement(name = "repository")
    public void setRepository(String repository) {
        ServerConfig.repository.add(repository);
    }

    @XmlElement(name = "CR")
    public void setCR(String CR_Texts) {
        String s[]=CR_Texts.split(",");
        if(s.length==5) {
            ServerConfig.CR_type.add(s[0]);
            ServerConfig.CR_class.add(s[1]);
            ServerConfig.CR_address.add(s[2]);
            ServerConfig.CR_ports.add(Integer.parseInt(s[3]));
            ServerConfig.CR_autoschedule.add(Boolean.parseBoolean(s[4]));
            //System.out.println(s[0]+" "+s[1]+" "+s[2]+" "+s[3]+" ");

        }else {
            System.out.println("invalid format");
        }
    }
    @XmlElement(name = "supportedCodecs")
    public void setSupportedCodecs(String supportedCodecs) {
        ServerConfig.supportedCodecs.add(supportedCodecs);
    }
    @XmlElement(name = "enableTimeEstimator")
    public void setEnableTimeEstimator(String check) {
        if(check.equalsIgnoreCase("True")) {
            ServerConfig.enableTimeEstimator = true;
        }else{
            ServerConfig.enableTimeEstimator = false;
        }
    }
    @XmlElement(name = "timeEstimatorMode")
    public void setTimeEstimatorMode(String timeEstimatorMode) {
        ServerConfig.timeEstimatorMode = timeEstimatorMode;
    }

    @XmlElement(name = "schedulerPolicy")
    public void setSchedulerPolicy(String schedulerPolicy) {
        ServerConfig.schedulerPolicy = schedulerPolicy;
    }
    @XmlElement(name = "enableCaching")
    public void setEnableCaching(String check) {
        if(check.equalsIgnoreCase("True")) {
            ServerConfig.enableCaching = true;
        }else{
            ServerConfig.enableCaching = false;
        }
    }
    @XmlElement(name = "enableCRscaling")
    public void setenableCRscaling(String check) {
        if(check.equalsIgnoreCase("True")) {
            ServerConfig.enableCRscaling = true;
        }else{
            ServerConfig.enableCRscaling = false;
        }
    }
    @XmlElement(name = "CRscalingIntervalTick")
    public void setCRscalingIntervalTick(int CRscalingIntervalTick) {
        ServerConfig.CRscalingIntervalTick = CRscalingIntervalTick;
    }
    @XmlElement(name = "localqueuelengthperCR")
    public void setlocalqueuelengthperCR(int ilocalqueuelengthperCR) {
        ServerConfig.localqueuelengthperCR = ilocalqueuelengthperCR;
    }
    @XmlElement(name = "taskmerge")
    public void settaskmerge(String check) {
        if(check.equalsIgnoreCase("True")) {
            ServerConfig.taskmerge = true;
        }else{
            ServerConfig.taskmerge = false;
        }
    }
    @XmlElement(name = "dataUpdateInterval")
    public void setdataUpdateInterval(int dataUpdateInterval) {
        ServerConfig.dataUpdateInterval = dataUpdateInterval;
    }
    @XmlElement(name = "enableCRscalingoutofInterval")
    public void setenableCRscalingoutofInterval(String check) {
        if(check.equalsIgnoreCase("True")) {
            ServerConfig.enableCRscalingoutofInterval = true;
        }else{
            ServerConfig.enableCRscalingoutofInterval = false;
        }
    }
    @XmlElement(name = "mergeOverwriteQueuePolicy")
    public void setmergeOverwriteQueuePolicy(String check) {
        if(check.equalsIgnoreCase("True")) {
            ServerConfig.mergeOverwriteQueuePolicy = true;
        }else{
            ServerConfig.mergeOverwriteQueuePolicy = false;
        }
    }
    @XmlElement(name = "overwriteQueuePolicyHeuristic")
    public void setoverwriteQueuePolicyHeuristic(String value) {
        ServerConfig.overwriteQueuePolicyHeuristic = value;
    }
    @XmlElement(name = "maxCR")
    public void setmaxCR(int maxCR) {
        ServerConfig.maxCR = maxCR;
    }
    @XmlElement(name = "minCR")
    public void setminCR(int minCR) {
        ServerConfig.minCR = minCR;
    }
    @XmlElement(name = "remedialVM_constantfactor")
    public void setRemedialVM_constantfactor(int remedialVM_constantfactor) {
        ServerConfig.remedialVM_constantfactor = remedialVM_constantfactor;
    }
    @XmlElement(name = "lowscalingThreshold")
    public void setLowscalingThreshold(Double lowscalingThreshold) {
        ServerConfig.lowscalingThreshold = lowscalingThreshold;
    }
    @XmlElement(name = "highscalingThreshold")
    public void setHighscalingThreshold(Double highscalingThreshold) {
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

    @XmlElement(name = "useEC2")
    public void setuseEC2(String useEC2) {
        ServerConfig.useEC2 = Boolean.parseBoolean(useEC2);
    }
    @XmlElement(name = "file_mode")
    public void setfile_mode(String mode) {
        ServerConfig.file_mode = mode;
    }
    @XmlElement(name = "run_mode")
    public void setrunMode(String mode) {
        ServerConfig.run_mode = mode;
    }
    @XmlElement(name = "outputDir")
    public void setOutputDir(String dir) {ServerConfig.outputDir = dir; }
}

