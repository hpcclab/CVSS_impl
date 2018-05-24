package VMManagement;

import Streampkg.StreamGOP;

import static java.lang.Thread.sleep;

public abstract class VMinterface {


    public int id;
    public String VM_class;
    protected int status;
    public int estimatedQueueLength=0;
    public long estimatedExecutionTime=0;
    public long elapsedTime=0;
    public long actualSpentTime=0;
    public double deadLineMissRate;
    public long workdone,Nworkdone;
    public long deadlinemiss,Ndeadlinemiss;


    public VMinterface(){}

    public VMinterface(String vclass,int inid){
            this.VM_class=vclass;
            id=inid;
    }
    public boolean isWorking(){
        return status==1;
    }

    public abstract boolean sendJob(StreamGOP segment);
    public abstract double dataUpdate(boolean full);
    public abstract boolean sendShutdownmessage();
    public void close(){}

}