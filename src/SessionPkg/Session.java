package SessionPkg;

import ProtoMessage.TaskRequest;
import mainPackage.CVSE;

import java.util.ArrayList;
//First level of what is the video transcoding, never transmit away from SVSE,
//unlike prior version, we nolonger have a ref link back to
public class Session {
    public long sessionid; //keep video number in case of video transcoding
    public String owner;
    private int status;
    public String outputPath;
    //ArrayList<TaskRequest.ServiceRequest> AssociatedRequests=new ArrayList<TaskRequest.ServiceRequest>(); //might be useful to track request associate with this request
    public ArrayList<TranscodingRequest> AssociatedRequests=new ArrayList<TranscodingRequest>();
    public TaskRequest.Operation getOperation() {
        return operation;
    }

    public void setOperation(TaskRequest.Operation operation) {
        this.operation = operation;
    }

    TaskRequest.Operation operation;

    public Session(){ // empty one
        sessionid=0;
        owner="";
        status=-1;
        outputPath="";
        operation=null;
    }
    public Session(int ses_id,String Owner,String path){ // empty status
        this(ses_id,Owner,path,0);
    }
    public Session(int ses_id,String Owner,String path,int statusNum){ // all filled
        sessionid=ses_id;
        owner=Owner;
        status=statusNum;
        outputPath=path;
    }
    public int GetStatus(){
        return status;
    }

}