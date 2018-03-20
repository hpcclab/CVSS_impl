package miscTools;

import Scheduler.GOPTaskScheduler;
import Scheduler.TimeEstimator;
import TranscodingVM.VMProvisioner;

import java.util.LinkedList;
import java.util.ListIterator;

//this List can be used as priority queue by take removeHighestPrio, instead of removeFirst.

public class SortableList extends LinkedList<Stream.StreamGOP> {
    public Stream.StreamGOP pollHighestPrio() {
        Stream.StreamGOP highest = peekFirst();
        ListIterator<Stream.StreamGOP> it = listIterator(1);
        while (it.hasNext()) {
            Stream.StreamGOP t = it.next();
            if (t.compareTo(highest)>0) {
                highest = t;
            }
        }
        return highest;
    }

    public Stream.StreamGOP pollEDL(){
        Stream.StreamGOP earliest = peekFirst();
        ListIterator<Stream.StreamGOP> it = listIterator(1);
        while (it.hasNext()) {
            Stream.StreamGOP t = it.next();
            if (t.deadLine<earliest.deadLine) {
                earliest = t;
            }
        }
        return earliest;
    }
    public Stream.StreamGOP pollMaxUrgency(){ //Homogeneous Only
        Stream.StreamGOP earliest = peekFirst();
        long earliestvalue=earliest.deadLine-TimeEstimator.getHistoricProcessTimeLong(GOPTaskScheduler.VMinterfaces.get(0).VM_class, earliest,1);
        ListIterator<Stream.StreamGOP> it = listIterator(1);
        while (it.hasNext()) {
            Stream.StreamGOP t = it.next();
            long checkvalue=t.deadLine- TimeEstimator.getHistoricProcessTimeLong(GOPTaskScheduler.VMinterfaces.get(0).VM_class, t,1);
            if ( checkvalue<earliestvalue) {
                earliest = t;
                earliestvalue=checkvalue;
            }
        }
        return earliest;
    }
    public Stream.StreamGOP removeHighestPrio() {
        Stream.StreamGOP highest = pollHighestPrio();
        removeFirstOccurrence(highest);
        return highest;
    }
    public Stream.StreamGOP removeEDL(){
        Stream.StreamGOP earliest = pollEDL();
        removeFirstOccurrence(earliest);
        return earliest;
     }
    public Stream.StreamGOP removeMaxUrgency(){ //Homogeneous Only
        Stream.StreamGOP earliest = pollMaxUrgency();
        removeFirstOccurrence(earliest);
        return earliest;
    }
}