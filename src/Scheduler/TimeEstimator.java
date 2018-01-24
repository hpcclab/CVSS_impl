package Scheduler;

import Stream.*;
import Repository.*;
import miscTools.Tuple;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by pi on 5/21/17.
 */
public class TimeEstimator {
    static HashMap<Integer,HashMap<Integer, Tuple<Long,Integer>> > Table=new HashMap<>();

    public static void updateTable(int id,HashMap<Integer, Tuple<Long,Integer>> runtime_report){
        Table.put(id,runtime_report);
        System.out.println("Update TimeEstimator table of VM "+id+" to "+runtime_report);
    }

    public static long getHistoricProcessTime(int id,StreamGOP segment){
        HashMap<Integer, Tuple<Long,Integer>> polled1=Table.get(id);
        if(polled1!=null){
            Tuple<Long,Integer> polled2=polled1.get(segment.userSetting.settingIdentifier);
            if(polled2!=null){
                System.out.println("Historically, this task takes "+polled2.x+" on id:"+id);
                return polled2.x;
            }
            //System.out.println("No historic data1!");
        }
        //System.out.println("No historic data2!");
        return 2000; //set at arbitary nonzero value
    }
    public static void SetSegmentProcessingTime(StreamGOP segment)
    {
        // set deadline Time to System.currentTime() +getHistoricProcessTime(segment)+ constant ?
        // don't use System.nanoTime Across the machine
    }

}