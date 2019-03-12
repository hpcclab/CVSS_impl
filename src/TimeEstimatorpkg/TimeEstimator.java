package TimeEstimatorpkg;

import Scheduler.ServerConfig;
import Streampkg.*;
import miscTools.Tuple;

import java.io.File;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by pi on 5/21/17.
 */


public class TimeEstimator {
    //HashMap<(str)machinetype,  Hashmap<str(command+paramID),class stat >    >
    static HashMap<String,HashMap<String,histStat>> Table=new HashMap<>();



    //updateTable is BROKEN
    public static void updateTable(String VMclass,HashMap<Integer, Tuple<Long,Integer>> runtime_report){
        System.out.println("this function is now broken, need fix later");
    //    Table.put(VMclass,runtime_report);
        System.out.println("Update TimeEstimator table of VM "+VMclass+" to "+runtime_report);
    }

    //design decision, should this return per segment or per action?
    //select do it all in here!

    //get port data just because fixedBandwidth machine store Bandwidth in their port
    public static retStat getHistoricProcessTime(String VMclass,Integer port,StreamGOP segment){
        //SDcoefficient=1 is Worst case, -1 is BestCase,
        if(VMclass.equalsIgnoreCase("fixedBandwidth")){
            long bandwidth= port;
            System.out.println("bandwidth="+bandwidth*8);
            System.out.println("request HistoricProcessTime of a fixedBandwidth VM, data is not reliable at the moment, use with caution-- only return transferTIme, no transmission delay");
            System.out.println("segment size="+segment.videoSize);
            return new retStat(segment.videoSize/bandwidth+1, 1);
        }else {
            //poll machine type
            HashMap<String, histStat> polled1 = Table.get(VMclass);
            long ESTTime = 0;
            double SD = 0;
            if (polled1 != null) { //have the machine type data
                boolean firstCmd = true;
                for (String cmd : segment.cmdSet.keySet()) {
                    //System.out.println("cmd="+cmd);
                    boolean newcmd = true;
                    for (String param : segment.cmdSet.get(cmd)) {
                        String pollstr;
                        if(ServerConfig.timeEstimatorMode.equalsIgnoreCase("profiled")) {

                            pollstr=cmd + param + "_"+segment.segment+"_"+segment.videoname;
                        }else{
                            pollstr= cmd + param;
                        }
                        histStat polled2 = polled1.get(pollstr);
                        if (polled2 != null) {
                            if (firstCmd) { //first base cmd
                                //System.out.println("Historically, this task takes " + polled2.mean + " SD:" + polled2.SD + " on class:" + VMclass);
                                ESTTime += polled2.mean;
                                SD += polled2.SD;
                                firstCmd = false;
                                newcmd = false;
                            } else if (newcmd) { //new command, count as case C merged
                                ESTTime += polled2.plusC * polled2.mean;
                                SD += polled2.plusC * polled2.SD;
                                newcmd = false;
                            } else {
                                System.out.println("case B time estimate");
                                ESTTime += polled2.plusB * polled2.mean;
                                SD += polled2.plusB * polled2.SD;
                            }


                        } else {
                            System.out.println("No historic data for this cmd!:" + cmd +" param:"+ param+" pollstr="+pollstr);
                            System.out.println("keyset=" + polled1.keySet());

                        }
                    }
                }
                //System.out.println("Normal Estimation");
                return new retStat(ESTTime, SD);

                //System.out.println("No historic data1!");
            }
            System.out.println();
            System.out.println("No historic data for this machine type!: " + VMclass);
            return new retStat(0, 0); //set at arbitary value
        }
    }

    //work with fixedBandwidth too
    //get port data just because fixedBandwidth machine store Bandwidth in their port
    public static long getHistoricProcessTimeLong(String VMclass,Integer port,StreamGOP segment,double SDr) {
    retStat rS=getHistoricProcessTime(VMclass,port,segment);
    return rS.mean+ (long)(rS.SD*SDr);
    }
    //function called at the beginning of running to populate data
    public static void populate(String VMclass){
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
                System.out.println(e);
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
            Table.put(VMclass, X);
        }
    }
    public static void SetSegmentProcessingTime(StreamGOP segment)
    {
        // set deadline Time to System.currentTime() +getHistoricProcessTime(segment)+ constant ?
        // don't use System.nanoTime Across the machine
    }

}