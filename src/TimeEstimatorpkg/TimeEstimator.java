package TimeEstimatorpkg;

import ResourceManagement.MachineInterface;
import Streampkg.StreamGOP;

import java.io.File;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by pi on 5/21/17.
 */


public class TimeEstimator {
    //HashMap<(str)machinetype,  Hashmap<str(command+paramID),class stat >    >
    HashMap<String,HashMap<String,histStat>> detailedTable=new HashMap<>();
    public TimeEstimator(){
    }

    public void updateTable(String VMclass, HashMap<String,histStat> runtime_report) {

    }

    //design decision, should this return per segment or per action?
    //select do it all in here!

    //get port data just because fixedBandwidth machine store Bandwidth in their port

    public retStat getHistoricProcessTime(MachineInterface VM, StreamGOP segment){
        //for fixedBandwidth case
        if(VM.VM_class.equalsIgnoreCase("fixedBandwidth")) {
            long bandwidth = VM.port;
            System.out.println("bandwidth=" + bandwidth * 8);
            System.out.println("request HistoricProcessTime of a fixedBandwidth VM, data is not reliable at the moment, use with caution-- only return transferTIme, no transmission delay");
            System.out.println("segment size=" + segment.fileSize);
            return new retStat(segment.fileSize / bandwidth + 1, 1);
        }
        //for other case
        //poll machine type
        HashMap<String, histStat> polled1 = detailedTable.get(VM.VM_class);
        if (polled1 != null) { //have the machine type data
            //if(CVSE.config.timeEstimatorMode.equalsIgnoreCase("profiled")) {
                return searchHistoricProcessTime(polled1, segment,"profiled");
            //}
        }else{
            System.out.println("\nNo historic data for this machine type!: " + VM.VM_class);
            return new retStat(-1, -1); //set at arbitary value
        }
    }
    public retStat searchHistoricProcessTime(HashMap<String,histStat> table,StreamGOP segment,String searchMode){
        //SDcoefficient=1 is Worst case, -1 is BestCase,
        long ESTTime = 0;
        double SD = 0;
        boolean firstCmd = true;
        int cmdcount=segment.cmdSet.keySet().size();
        for (String cmd : segment.cmdSet.keySet()) {
            //System.out.println("cmd="+cmd);
            boolean newcmd = true;
            for (String param : segment.cmdSet.get(cmd)) {
                int paramcount=segment.cmdSet.get(cmd).size();
                String pollstr;
                if(searchMode.equalsIgnoreCase("profiled")) {
                    //System.out.println("Time Estimator for cmd="+cmd+" param="+param+" segment="+segment.segment+" vname="+segment.videoname);
                    pollstr=cmd + param + "_"+segment.segment+"_"+segment.videoname;
                }else if (searchMode.equalsIgnoreCase("operation")) {
                    pollstr=cmd;
                }else{
                    pollstr= cmd + param;
                }
                histStat polled2 = table.get(pollstr);
                //System.out.println("keyset=" + polled1.keySet());
                if (polled2 != null) {
                    if (firstCmd) { //first base cmd
                        //System.out.println("Historically, this task takes " + polled2.mean + " SD:" + polled2.SD + " on class:" + VMclass);
                        if(cmdcount>1) { //first of b mergeable
                            ESTTime = (long)(polled2.plusB* polled2.mean);
                            SD = polled2.SD * polled2.plusB;
                        }else
                        if(cmdcount>1) { //first of c mergeable
                            ESTTime = (long)(polled2.plusC* polled2.mean);
                            SD = polled2.SD * polled2.plusC;
                        }else{ //first, and only one
                            ESTTime = polled2.mean ;
                            SD = polled2.SD ;
                        }
                        firstCmd = false;
                        newcmd = false;
                    } else if (newcmd) { //new command but not first cmd, count as case C merged
                        ESTTime += polled2.plusC * polled2.mean;
                        SD += polled2.plusC * polled2.SD;
                        newcmd = false;
                    } else {
                        //System.out.println("case C time estimate");
                        ESTTime += polled2.plusB * polled2.mean;
                        SD += polled2.plusB * polled2.SD;
                    }
                } else {
                    System.out.println("No historic data for this cmd!:" + cmd +" param:"+ param+" segment id="+segment.segment+" pollstr="+pollstr);
                }
            }
        }
        //System.out.println("Normal Estimation");
        return new retStat(ESTTime, SD);
    }

    public long getHistoricProcessTimeLong(MachineInterface VM,StreamGOP segment,double SDco) {
    retStat rS=getHistoricProcessTime(VM,segment);
    return rS.mean+ (long)(rS.SD*SDco);
    }
    //function called at the beginning of running to populate data
    public void populate(String VMclass){
        if(VMclass.equalsIgnoreCase("fixedBandwidth")){
            System.out.println("fixed bandwidth machine, no need to populate TimeEstimator table");
            //doNothing
        }else {
            //System.out.println("populate table"+VMclass);
            File F = new File("profile/" + VMclass + ".txt");
            Scanner scanner = null;
            try {
                scanner = new Scanner(F);
            } catch (Exception e) {
                System.out.println("Time estimator error"+e);
            }

            HashMap<String, histStat> X = new HashMap<>();
            while (scanner.hasNext()) {

                long mean;
                double SD, plusB, plusC;
                String command;
                String setting;
                String fullline = scanner.nextLine();
                //System.out.println(fullline);
                String[] line = fullline.split(",");
                if (line.length == 7) {
                    command = line[0];
                    setting = line[1]+"_"+line[2];
                    mean = (long)Double.parseDouble(line[3]);
                    SD = Double.parseDouble(line[4]);
                    plusB = Double.parseDouble(line[5]) / 100;
                    plusC = Double.parseDouble(line[6]) / 100;
                    histStat S = new histStat(mean, SD, plusB, plusC);
                    X.put(command + setting, S);
                    //System.out.println(line[1]);
                } else {
                    System.out.println("profile not correctly formatted");
                }
            }
            //System.out.println("read a profile");
            detailedTable.put(VMclass, X);
        }
    }
    public void SetSegmentProcessingTime(StreamGOP segment)
    {
        // set deadline Time to System.currentTime() +getHistoricProcessTime(segment)+ constant ?
        // don't use System.nanoTime Across the machine
    }

}