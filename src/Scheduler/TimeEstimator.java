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
class Stat{
    long mean=0;
    double SD=0,plusB=0,plusC=0;

    Stat(long m,double s,double b,double c){
    mean=m;SD=s;plusB=b;plusC=c;
    }
    Stat(){

    }
}
public class TimeEstimator {
    //HashMap<(str)machinetype,  Hashmap<str(command+paramID),class stat >    >
    static HashMap<String,HashMap<String,Stat>> Table=new HashMap<>();



    //updateTable is BROKEN
    public static void updateTable(String VMclass,HashMap<Integer, Tuple<Long,Integer>> runtime_report){
        System.out.println("this function is now broken, need fix later");
    //    Table.put(VMclass,runtime_report);
        System.out.println("Update TimeEstimator table of VM "+VMclass+" to "+runtime_report);
    }

    //design decision, should this return per segment or per action?
    //select do it all in here!
    public static Long getHistoricProcessTime(String VMclass,StreamGOP segment,double SDcoefficient){
        //SDcoefficient=1 is Worst case, -1 is BestCase,
        HashMap<String,Stat> polled1=Table.get(VMclass);
        long Time=0;
        if(polled1!=null){ //have thay machine type data

            for (String cmd : segment.cmdSet.keySet()) {

                for(String param: segment.cmdSet.get(cmd)) {
                    System.out.println(cmd+param);
                    Stat polled2 = polled1.get(cmd + param);

                    if (polled2 != null) {
                        System.out.println("Historically, this task takes " + polled2.mean + " SD:" + polled2.SD + " on class:" + VMclass);
                        Time+=(long) (polled2.mean*polled2.SD*SDcoefficient);
                    }else{
                        System.out.println("No historic data for this cmd!:"+cmd + param);
                    }
                }
            }
            return Time;

            //System.out.println("No historic data1!");
        }
        System.out.println();
        System.out.println("No historic data for this machine type!: "+VMclass);
        return 0L; //set at arbitary value
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

        HashMap<String,Stat> X=new HashMap<>();
        while(scanner.hasNext()) {

            long mean;
            double SD, plusB, plusC;
            String command;
            String setting;

            String[] line = scanner.nextLine().split(",");
            if (line.length == 6) {
                command = line[0];
                setting = line[1];
                mean = Long.parseLong(line[2]);
                SD = Double.parseDouble(line[3]);
                plusB = Double.parseDouble(line[4]);
                plusC = Double.parseDouble(line[5]);
                Stat S = new Stat(mean, SD, plusB, plusC);
                X.put(command + setting, S);
            }
        }
        Table.put(VMclass,X);
    }
    public static void SetSegmentProcessingTime(StreamGOP segment)
    {
        // set deadline Time to System.currentTime() +getHistoricProcessTime(segment)+ constant ?
        // don't use System.nanoTime Across the machine
    }

}