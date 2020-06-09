
package SessionPkg;

import ProtoMessage.TaskRequest;
import ProtoMessage.TaskRequest.*;

import java.util.ArrayList;

class JOperation{
    class JParamOptions{
         String subParameter; //just split strings if there are many subparameters
         String owner;
         long requestDL;
        public JParamOptions(String s,String o,long dl){
            subParameter=s;
            owner=o;
            requestDL=dl;
        }
        public JParamOptions(){

        }
    }
    String cmd;
    ArrayList<JParamOptions> Parameter=new ArrayList<>();
    public JOperation(String c,String s,String o,long dl){ //set up for single parameter
        cmd=c;
        Parameter.add(new JParamOptions(s,o,dl));
    }
    public JOperation(){

    }
}

//wrapper of TaskRequest datastructure.
public class TranscodingRequest implements Comparable<TranscodingRequest>
{

    public String DataSource;
    public long GlobalDeadline;
    public String DataTag;
    public long Arrival=0;
    public double EstMean=0;
    public double EstSD=0;
    public int Priority=0;
    public long TaskId=0;
    public ArrayList<JOperation> operation=new ArrayList<>();
    public int requestcount; //local param

    public TranscodingRequest(String source,String Tag,long arrival,int priority,JOperation anoperation){ //quick constructor
    DataSource=source;
    DataTag=Tag;
    Arrival=arrival;
    Priority=priority;
    operation.add(anoperation);
    }
    public void updateGlobalDL(){
        long max=0;
        int i=0;
        for (JOperation op : operation){
            for(JOperation.JParamOptions anoption: op.Parameter){
                if(anoption.requestDL>max){
                    max=anoption.requestDL;
                    i++;
                }
            }
        }
        GlobalDeadline=max;
        requestcount=i;
    }
    public ArrayList<String> listallCMD(){
        ArrayList<String> tmplist=new ArrayList<>();
        for (JOperation op : operation) {
                tmplist.add(op.cmd);
        }
        return tmplist;
    }
    public ArrayList<String> listparamsofCMD(String whichcmd){
        ArrayList<String> tmplist=new ArrayList<>();
        for (JOperation op : operation) {
            if(op.cmd.equalsIgnoreCase(whichcmd)) {
                for (JOperation.JParamOptions anoption : op.Parameter) {
                    tmplist.add(anoption.subParameter);
                }
            }
        }
        return tmplist;
    }
    public ArrayList<String> listallCMDOP(){
        ArrayList<String> tmplist=new ArrayList<>();
        for (JOperation op : operation) {
            for (JOperation.JParamOptions anoption : op.Parameter) {
                tmplist.add(op.cmd+":"+anoption.subParameter);
            }
        }
        return tmplist;
    }
    public long getdeadlineof(String whichcmd,String whichparam) {
        for (JOperation op : operation) {
            if (op.cmd.equalsIgnoreCase(whichcmd)) {
                for (JOperation.JParamOptions anoption : op.Parameter) {
                    if (anoption.subParameter.equalsIgnoreCase(whichparam)) {
                        return anoption.requestDL;
                    }
                }
            }
        }
        return -1;
    }
    //convert back from protobuf message
    public TranscodingRequest(ServiceRequest Tr){
        DataSource=Tr.getDataSource();
        GlobalDeadline=Tr.getGlobalDeadline();
        DataTag=Tr.getDataTag();
        Arrival=Tr.getArrival();
        EstMean=Tr.getEstMean();
        EstSD=Tr.getEstSD();
        Priority=Tr.getPriority();
        TaskId=Tr.getTaskID();
        for(int i=0;i<Tr.getOPlistCount();i++){
            JOperation tmpO=new JOperation();
            for(int j=0;j<Tr.getOPlist(i).getParameterCount();j++){
                JOperation.JParamOptions tmpP=tmpO.new JParamOptions(Tr.getOPlist(i).getParameter(i).getSubparameter(),
                        Tr.getOPlist(i).getParameter(i).getOwner(),
                        Tr.getOPlist(i).getParameter(i).getRequestDL()); //= tmpO.new JOperation.JParamOptions();
//                tmpP.subParameter=Tr.getOPlist(i).getParameter(i).getSubparameter();
//                tmpP.owner=Tr.getOPlist(i).getParameter(i).getOwner();
//                tmpP.requestDL=Tr.getOPlist(i).getParameter(i).getRequestDL();
                tmpO.Parameter.add(tmpP);
            }
            tmpO.cmd=Tr.getOPlist(i).getCmd();
            operation.add(tmpO);
        }
    }
        //sort by deadline
    public int compareTo(TranscodingRequest T){
        return (int)(GlobalDeadline-T.GlobalDeadline);
    }
    //build the request to send

    public ServiceRequest buildRequest(){
        TaskRequest.Operation.ParamOptions.Builder ParamBuilder=TaskRequest.Operation.ParamOptions.newBuilder();
        TaskRequest.Operation.Builder CmdBuilder = TaskRequest.Operation.newBuilder();
        TaskRequest.ServiceRequest.Builder SerBuilder=TaskRequest.ServiceRequest.newBuilder();

        ArrayList<TaskRequest.Operation> oplist=new ArrayList<>();
        for (JOperation i:operation) {
            ArrayList<TaskRequest.Operation.ParamOptions> paramlist=new ArrayList<>();
            for(JOperation.JParamOptions j:i.Parameter){
                paramlist.add(ParamBuilder
                        .setSubparameter(j.subParameter)
                        .setOwner(j.owner)
                        .setRequestDL(j.requestDL)
                        .build());
            }
            oplist.add(CmdBuilder
                    .setCmd(i.cmd)
                    .addAllParameter(paramlist)
                    .build());
        }
        return SerBuilder
                .setDataSource(DataSource)
                .setGlobalDeadline(GlobalDeadline)
                .setDataTag(DataTag)
                .setArrival(Arrival)
                .setEstMean(EstMean)
                .setEstSD(EstSD)
                .setPriority(Priority)
                .setTaskID(TaskId)
                .addAllOPlist(oplist).build();
    }


}