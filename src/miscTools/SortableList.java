package miscTools;

import Scheduler.GOPTaskScheduler;
import TimeEstimatorpkg.TimeEstimator;

import java.util.LinkedList;
import java.util.ListIterator;

//this List can be used as priority queue by take removeHighestPrio, instead of removeFirst.

public class SortableList extends LinkedList<Streampkg.StreamGOP> {
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
        long earliestvalue=earliest.deadLine-TimeEstimator.getHistoricProcessTimeLong(GOPTaskScheduler.VMinterfaces.get(0).VM_class,GOPTaskScheduler.VMinterfaces.get(0).port, earliest,1);
        ListIterator<Streampkg.StreamGOP> it = listIterator(1);
        while (it.hasNext()) {
            Streampkg.StreamGOP t = it.next();
            long checkvalue=t.deadLine- TimeEstimator.getHistoricProcessTimeLong(GOPTaskScheduler.VMinterfaces.get(0).VM_class,GOPTaskScheduler.VMinterfaces.get(0).port, t,1);
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
}