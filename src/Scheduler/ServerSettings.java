package Scheduler;

public class ServerSettings {
    // mainstay settings
    public static String[] supportedCodecs; //not being used right now
    public static int maximumResolution;
    public static boolean enableTimeEstimator=false; //if true, use Time estimator to do scheduling
    public static boolean enableCaching=false; //if true, use caching system
    public static boolean VMscaling=false;
    public static int maxVM; //set max number of VMs
    public static int remedialVM_constantfactor=10; //default value=10
    public static int lowscalingThreshold; // for VM provisioning algorithms
    public static int highscalingThreshold;
    public static double c_const_for_utilitybased=0.1; //default value=0.1
    public static String mapping_mechanism;// can be either MM,MSD,MMU

    // questionalble settings
    public static String defaultInputPath; //
    public static String defaultOutputPath; //
    public static String defaultBatchScript; //
    public static String[] VM_address;
    public static int[] VM_ports;
    //check for each parameters if there is any Invalid
    public static boolean isSettingValid(){
        if(VMscaling){
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

        if(!(mapping_mechanism.equals("MM")||mapping_mechanism.equals("MSD")||mapping_mechanism.equals("MMU"))){
            System.out.println("mapping_mechanism must be either MM, MSD or MMU");
            return false;
        }

        //if all pass,
        return true;
    }
}
