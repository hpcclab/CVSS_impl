package Scheduler;

import Stream.*;
import Repository.*;
import miscTools.Tuple;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by pi on 5/21/17.
 */
class histStat{
    long mean=0;
    double SD=0,plusB=0,plusC=0;

    histStat(long m,double s,double b,double c){
    mean=m;SD=s;plusB=b;plusC=c;
    }
}
class retStat{
    long mean=0;
    double SD=0;

    retStat(long m,double s){
        mean=m;SD=s;
    }
}
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


    public static retStat getHistoricProcessTime(String VMclass,StreamGOP segment){
        //SDcoefficient=1 is Worst case, -1 is BestCase,
        HashMap<String,histStat> polled1=Table.get(VMclass);
        long ESTTime=0;
        double SD=0;
        if(polled1!=null){ //have the machine type data


            for (String cmd : segment.cmdSet.keySet()) {
                //System.out.println("cmd="+cmd);
                for(String param: segment.cmdSet.get(cmd)) {
                    histStat polled2 = polled1.get(cmd + param);

                    if (polled2 != null) {
                        //System.out.println("Historically, this task takes " + polled2.mean + " SD:" + polled2.SD + " on class:" + VMclass);
                        ESTTime+=polled2.mean;
                        SD+=polled2.SD;
                    }else{
                        System.out.println("No historic data for this cmd!:"+cmd + param);
                        System.out.println("keyset="+polled1.keySet());
                    }
                }
            }

            return new retStat(ESTTime,SD);

            //System.out.println("No historic data1!");
        }
        System.out.println();
        System.out.println("No historic data for this machine type!: "+VMclass);
        return new retStat(0,0); //set at arbitary value
    }

    //function called at the beginning of running to populate data
    public static void populate(String VMclass){

        File F=new File("profile/"+VMclass+".txt");
        Scanner scanner= null;
        try {
            scanner = new Scanner(F);
        } catch (Exception e) {
            System.out.println(e);
        }

        HashMap<String,histStat> X=new HashMap<>();
        while(scanner.hasNext()) {

            long mean;
            double SD, plusB, plusC;
            String command;
            String setting;
            String fullline=scanner.nextLine();
            //System.out.println(fullline);
            String[] line = fullline.split(",");
            if (line.length == 6) {
                command = line[0];
                setting = line[1];
                mean = Long.parseLong(line[2]);
                SD = Double.parseDouble(line[3]);
                plusB = Double.parseDouble(line[4]);
                plusC = Double.parseDouble(line[5]);
                histStat S = new histStat(mean, SD, plusB, plusC);
                X.put(command + setting, S);
                //System.out.println(line[1]);
            }else{
                System.out.println("profile not correctly formatted");
            }
        }
        //System.out.println("read a profile");
        Table.put(VMclass,X);
    }
    public static void SetSegmentProcessingTime(StreamGOP segment)
    {
        // set deadline Time to System.currentTime() +getHistoricProcessTime(segment)+ constant ?
        // don't use System.nanoTime Across the machine
    }

}