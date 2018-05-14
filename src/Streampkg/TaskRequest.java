package Streampkg;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class TaskRequest{ //fill every thing for lvl1 mapping constructor, skip resolution for lvl2 mapping, ...
    private final String command;
    private final String param;
    private final String Path;
    //constructor with StreamGOP and intended level of matching
    //command list will only contain ONE command and param, no merge
    public TaskRequest(StreamGOP original,int level){
        String thecmd="";
        String theparam="";
        int i=0;
        for(String cmd : original.cmdSet.keySet()){
            thecmd=cmd;
            theparam=original.cmdSet.get(thecmd).get(0);
            i++;
            if(i>1){
                System.out.println("why do we have merged command here?");
            }
        }
        if(level==3) { //Type C
            Path=original.getPath(); //match video segment
            command="";
            param="";
        }else if(level==2){ //Type B
            Path=original.getPath();
            command=thecmd; //match command
            param="";
        }else{ // Type A
            Path=original.getPath();
            command=thecmd;
            param=theparam; //match resolution too

        }

    }
    public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
                // if deriving: appendSuper(super.hashCode()).
                        append(command).
                        append(param).
                        append(Path).
                        toHashCode();
    }

    //return true if it's match, design to compare to request constructed with the SAME level
    public boolean equals(Object obj){
        if (!(obj instanceof TaskRequest)) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        TaskRequest X=(TaskRequest)obj;
        return new EqualsBuilder().
                append(command,X.command).
                append(param,X.param).
                append(Path,X.Path).
                isEquals();
    }
}