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

    public static Stat getHistoricProcessTime(String VMclass,StreamGOP segment){
        HashMap<String,Stat> polled1=Table.get(VMclass);
        if(polled1!=null){
            Stat polled2=polled1.get(segment.command+segment.userSetting.settingIdentifier);
            if(polled2!=null){
                System.out.println("Historically, this task takes "+polled2.mean+" SD:"+polled2.SD +" on class:"+VMclass);
                return polled2;
            }
            //System.out.println("No historic data1!");
        }
        //System.out.println("No historic data2!");
        return new Stat(); //set at arbitary nonzero value
    }

    //function called at the beginning of running to populate data
    public static void populate(String VMclass){

        File F=new File("profile/"+VMclass);
        Scanner scanner= null;
        try {
            scanner = new Scanner(F);
        } catch (Exception e) {
            System.out.println(e);
        }

        HashMap<String,Stat> X=new HashMap<>();
        while(scanner.hasNext()) {
            int setting;
            long mean;
            double SD, plusB, plusC;
            String command;

            String[] line = scanner.nextLine().split(",");
            if (line.length == 6) {
                command = line[0];
                setting = Integer.parseInt(line[1]);
                mean = Long.parseLong(line[2]);
                SD = Double.parseDouble(line[3]);
                plusB = Double.parseDouble(line[4]);
                plusC = Double.parseDouble(line[5]);
                System.out.println("get a profile");
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