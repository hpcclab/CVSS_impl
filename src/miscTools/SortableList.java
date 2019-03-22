package miscTools;

import Scheduler.GOPTaskScheduler;
import Scheduler.ServerConfig;
import Streampkg.StreamGOP;
import TimeEstimatorpkg.TimeEstimator;
import mainPackage.CVSE;

import java.util.Collection;
import java.util.LinkedList;
import java.util.ListIterator;

//this List can be used as priority queue by take removeHighestPrio, instead of removeFirst.

public class SortableList extends LinkedList<Streampkg.StreamGOP> {
    CVSE _CVSE;
    public SortableList(CVSE cvse,Collection<? extends StreamGOP> collection) {
        super(collection);
        _CVSE=cvse;
    }
    public SortableList(CVSE cvse) {
        super();
        _CVSE=cvse;
    }

    public Streampkg.StreamGOP pollHighestPrio() {
        Streampkg.StreamGOP highest = peekFirst();
        ListIterator<Streampkg.StreamGOP> it = listIterator(1);
        while (it.hasNext()) {
            Streampkg.StreamGOP t = it.next();
            if (t.compareTo(highest)>0) {
                highest = t;
            }
        }
        return highest;
    }

    public Streampkg.StreamGOP pollEDL(){
        Streampkg.StreamGOP earliest = peekFirst();
        ListIterator<Streampkg.StreamGOP> it = listIterator(1);
        while (it.hasNext()) {
            Streampkg.StreamGOP t = it.next();
            if (t.deadLine<earliest.deadLine) {
                earliest = t;
            }
        }
        return earliest;
    }
    public Streampkg.StreamGOP pollMaxUrgency(){ //Homogeneous Only
        Streampkg.StreamGOP earliest = peekFirst();
        long earliestvalue=earliest.deadLine-_CVSE.TE.getHistoricProcessTimeLong(_CVSE.GTS.machineInterfaces.get(0).VM_class,_CVSE.GTS.machineInterfaces.get(0).port, earliest,1);
        ListIterator<Streampkg.StreamGOP> it = listIterator(1);
        while (it.hasNext()) {
            Streampkg.StreamGOP t = it.next();
            long checkvalue=t.deadLine- _CVSE.TE.getHistoricProcessTimeLong(_CVSE.GTS.machineInterfaces.get(0).VM_class,_CVSE.GTS.machineInterfaces.get(0).port, t,1);
            if ( checkvalue<earliestvalue) {
                earliest = t;
                earliestvalue=checkvalue;
            }
        }
        return earliest;
    }
    public Streampkg.StreamGOP removeHighestPrio() {
        Streampkg.StreamGOP highest = pollHighestPrio();
        removeFirstOccurrence(highest);
        return highest;
    }
    public Streampkg.StreamGOP removeEDL(){
        Streampkg.StreamGOP earliest = pollEDL();
        removeFirstOccurrence(earliest);
        return earliest;
     }
    public Streampkg.StreamGOP removeMaxUrgency(){ //Homogeneous Only
        Streampkg.StreamGOP earliest = pollMaxUrgency();
        removeFirstOccurrence(earliest);
        return earliest;
    }
    public Streampkg.StreamGOP removeDefault() {
        if(ServerConfig.batchqueuesortpolicy.equalsIgnoreCase("None")) { //not sorting batch queue
            //X= Batchqueue.poll();
            return remove();
        }else if(ServerConfig.batchqueuesortpolicy.equalsIgnoreCase("Priority")) {
            return removeHighestPrio();
        }else if(ServerConfig.batchqueuesortpolicy.equalsIgnoreCase("Deadline")) {
            return removeEDL();
        }else if(ServerConfig.batchqueuesortpolicy.equalsIgnoreCase("Urgency")) {
            return removeMaxUrgency(); //Homogeneous Only
        }else{
            System.out.println("unrecognize batchqueue policy");
            return removeEDL();
        }
    }
}