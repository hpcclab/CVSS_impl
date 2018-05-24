package TranscodingVM;

import miscTools.Tuple;

import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class runtime_report implements Serializable {
    public int queue_size;
    public long queue_executionTime,VMelapsedTime,VMspentTime;
    public long Nworkdone,workdone,Nmissed,missed;
    public double deadLineMissRate;
    public HashMap<String, Tuple<Long,Integer>> runtime_report=new HashMap<>();


    public runtime_report(int queue_size, long time, long timeElapsed, long timeSpent, long ncmp, long cmp, long miss, long nmiss, double deadLineMissRate, ConcurrentHashMap<String, Tuple<Long, Integer>> runtime_report) {
        this.runtime_report.putAll(runtime_report);
        this.queue_executionTime=time;
        this.VMelapsedTime=timeElapsed;
        this.VMspentTime=timeSpent;
        this.workdone=cmp;
        this.Nworkdone=ncmp;
        this.missed=miss;
        this.Nmissed=nmiss;
        this.deadLineMissRate=deadLineMissRate;
        this.queue_size=queue_size;
    }
}