package miscTools;

import SessionPkg.TranscodingRequest;
import mainPackage.CVSE;

import java.util.Collection;
import java.util.LinkedList;
import java.util.ListIterator;

//this List can be used as priority queue by take removeHighestPrio, instead of removeFirst.

public class TaskQueue extends LinkedList<TranscodingRequest> {
    public TaskQueue(Collection<? extends TranscodingRequest> collection) {
        super(collection);
    }
    public TaskQueue() {
        super();
    }

    public TranscodingRequest pollHighestPrio() {
        TranscodingRequest highest = peekFirst();
        ListIterator<TranscodingRequest> it = listIterator(1);
        while (it.hasNext()) {
            TranscodingRequest t = it.next();
            if (t.Priority> highest.Priority) {
                highest = t;
            }
        }
        return highest;
    }

    public TranscodingRequest pollEDL(){
        TranscodingRequest earliest = peekFirst();
        ListIterator<TranscodingRequest> it = listIterator(1);
        while (it.hasNext()) {
            TranscodingRequest t = it.next();
            if (t.GlobalDeadline<earliest.GlobalDeadline) {
                earliest = t;
            }
        }
        return earliest;
    }
    public TranscodingRequest pollMaxUrgency(){ //Homogeneous Only, or else, only use first machine's processing time
        TranscodingRequest earliest = peekFirst();
        long earliestvalue=earliest.GlobalDeadline-CVSE.TE.getHistoricProcessTimeLong(CVSE.GTS.machineInterfaces.get(0), earliest,2);
        ListIterator<TranscodingRequest> it = listIterator(1);
        while (it.hasNext()) {
            TranscodingRequest t = it.next();
            long checkvalue=t.GlobalDeadline- CVSE.TE.getHistoricProcessTimeLong(CVSE.GTS.machineInterfaces.get(0), t,2);
            if ( checkvalue<earliestvalue) {
                earliest = t;
                earliestvalue=checkvalue;
            }
        }
        return earliest;
    }
    public TranscodingRequest removeHighestPrio() {
        TranscodingRequest highest = pollHighestPrio();
        removeFirstOccurrence(highest);
        return highest;
    }
    public TranscodingRequest removeEDL(){
        TranscodingRequest earliest = pollEDL();
        removeFirstOccurrence(earliest);
        return earliest;
     }
    public TranscodingRequest removeMaxUrgency(){ //Homogeneous Only
        TranscodingRequest earliest = pollMaxUrgency();
        removeFirstOccurrence(earliest);
        return earliest;
    }
    public TranscodingRequest removeDefault() {
        if(CVSE.config.batchqueuesortpolicy.equalsIgnoreCase("None")) { //not sorting batch queue
            //X= Batchqueue.poll();
            return remove();
        }else if(CVSE.config.batchqueuesortpolicy.equalsIgnoreCase("Priority")) {
            return removeHighestPrio();
        }else if(CVSE.config.batchqueuesortpolicy.equalsIgnoreCase("Deadline")) {
            return removeEDL();
        }else if(CVSE.config.batchqueuesortpolicy.equalsIgnoreCase("Urgency")) { //maybe broken right now
            return removeMaxUrgency(); //Homogeneous Only
        }else{
            System.out.println("unrecognize batchqueue policy");
            return removeEDL();
        }
    }
}