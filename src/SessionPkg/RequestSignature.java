package SessionPkg;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

//for merger
public class RequestSignature{
    private final String command;
    private final String param;
    private final String data;
    //constructor with StreamGOP and intended level of matching
    //command list will only contain ONE command and param, no merge
    public RequestSignature(TranscodingRequest original, int level){
        String thecmd="";
        String theparam="";
        int i=0;
        if(original.operation.size()!=1){
            command=param=data="";
            System.out.println("Error, Operation count !=1");
            return;
        }
        if(original.operation.get(0).Parameter.size()!=1){
            command=param=data="";
            System.out.println("Error, parameter count !=1");
            return;
        }
//////////////////
        //retrieve thecmd and theparam
        thecmd=original.operation.get(0).cmd;
        theparam=original.operation.get(0).Parameter.get(0).subParameter;
        //////////
        if(level==3) { //Type C
            data=original.DataSource; //match video segment
            command="";
            param="";
        }else if(level==2){ //Type B
            data=original.DataSource;
            command=thecmd; //match command
            param="";
        }else{ // Type A
            data=original.DataSource;
            command=thecmd;
            param=theparam; //match resolution too

        }

    }
    public int hashCode() {
        //System.out.println("building hashcode cmd="+command+" param="+param+" path="+Path);
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
                // if deriving: appendSuper(super.hashCode()).

                        append(command).
                        append(param).
                        append(data).
                        toHashCode();
    }

    //return true if it's match, design to compare to request constructed with the SAME level
    public boolean equals(Object obj){
        if (!(obj instanceof RequestSignature)) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        RequestSignature X=(RequestSignature)obj;
        return new EqualsBuilder().
                append(command,X.command).
                append(param,X.param).
                append(data,X.data).
                isEquals();
    }
}