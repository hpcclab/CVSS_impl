package SessionPkg;

import ProtoMessage.TaskRequest;
import Repository.RepositoryGOP;
import Repository.Video;
import mainPackage.CVSE;


//current implementation make owner ID=0 and session ID=0 for all request, this must change later.
public class videoSession extends Session{
    public Video video; // for reference of which video we are talking about
    public int segmentStart;
    public int segmentEnd;
    public long presentationTime;
    public long reqArrival;

    public videoSession(){
        super();
        video =new Video();
        if(CVSE.config.run_mode.equalsIgnoreCase("sim")){

        }else {
            presentationTime = System.currentTimeMillis() + 1000; //thisTime+Constant for now, should really be scheduleTime
        }
    }
    public videoSession(int vid, String command, String settings, long deadline, long arrivalTime){
        this(vid,command,settings,0,CVSE.VR.videos.get(vid).repositoryGOPs.size(),deadline,arrivalTime);
        System.out.println("resgopsize="+CVSE.VR.videos.get(vid).repositoryGOPs.size());

    }
    public videoSession(int vid, String command, String settings, int startSegment, long deadline, long arrivalTime){
        this(vid,command,settings,startSegment,CVSE.VR.videos.get(vid).repositoryGOPs.size(),deadline,arrivalTime);
    }

    public videoSession(int vid, String command, String settings, int startSegment, int endSegment, long deadline, long arrivalTime) {
        super(0, "", "/", 0);
        video = CVSE.VR.videos.get(vid);
        segmentEnd = endSegment;
        segmentStart = startSegment;
        reqArrival=arrivalTime;
        if (deadline == 0) { //ST==0, did not specified a preliminary deadline
            //normally dont fall in this case anyway in sim mode
            if (CVSE.config.run_mode.equalsIgnoreCase("sim")) {

                presentationTime = CVSE.GTS.maxElapsedTime + 2000; //add a prelinary value
            } else {
                presentationTime = System.currentTimeMillis() + 4000; //thisTime+Constant for now, should really be scheduleTime
            }
        } else {
            presentationTime = deadline;
        }


        //generate operator/parameter
//        String Owner="";
//        TaskRequest.Operation.ParamOptions.Builder ParamBuilder=TaskRequest.Operation.ParamOptions.newBuilder();
//        TaskRequest.Operation.Builder CmdBuilder = TaskRequest.Operation.newBuilder();
//        setOperation(
//                CmdBuilder.setCmd(command)
//                .addParameter(0,ParamBuilder.setSubparameter(settings)
//                        .setOwner(Owner)
//                        .setRequestDL(presentationTime)
//                        .build())
//                .build()
//        );
        //now, generate all transcoding requests,
        TaskRequest.ServiceRequest.Builder SerBuilder = TaskRequest.ServiceRequest.newBuilder();
        for(int i=startSegment;i<endSegment;i++){
            //System.out.println("create segment number "+i);
          ////   code below build wrong thing, we should keep requests permutable first until dispatch
//            RepositoryGOP agop=video.repositoryGOPs.get(i);
//            TaskRequest.Operation Op=CmdBuilder.setCmd(command)
//                    .addParameter(0,ParamBuilder.setSubparameter(settings)
//                            .setOwner(Owner)
//                            .setRequestDL(2*i+presentationTime)
//                            .build())
//                    .build();
//            AssociatedRequests.add(
//                    SerBuilder.setDataSource(agop.getPath())
//                            .setGlobalDeadline(2*i+presentationTime)
//                            .setDataTag("")
//                            .setArrival(0) //no info yet...
//                            //.setEstMean(0)
//                            //.setEstSD(0)
//                            .addOPlist(Op)
//                    .build()
//            );

        //build the permutable stuff
            JOperation anOPE=new JOperation(command,settings,owner,2*i+presentationTime); //single parameter, single command
            TranscodingRequest aTR=new TranscodingRequest(""+video.id+"_"+i,"SimReq",reqArrival,0,anOPE);
            aTR.updateGlobalDL();
            //update     EstMean,EstSD ?
            AssociatedRequests.add(aTR);
//   public TranscodingRequest(String source,String Tag,long arrival,int priority)

        }
    }

}
