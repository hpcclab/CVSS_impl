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
    }
    public static void SetSegmentProcessingTime(StreamGOP segment)
    {

    }

}