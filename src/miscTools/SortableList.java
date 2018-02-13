package miscTools;

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
    public Stream.StreamGOP removeHighestPrio() {
        Stream.StreamGOP highest = pollHighestPrio();
        removeFirstOccurrence(highest);
        return highest;
    }
}