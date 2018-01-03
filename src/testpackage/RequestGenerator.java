package testpackage;

import Repository.VideoRepository;
import Scheduler.AdmissionControl;
import Scheduler.GOPTaskScheduler;
import Stream.Settings;
import Stream.*;

public class RequestGenerator {
    static String videoList[]={"bbb_trailer","ff_trailer_part1","ff_trailer_part3"};
    public static void OneRandomRequest(GOPTaskScheduler GTS){
        //random a resolution
        int randomRes=(int)(Math.random()*7)+1;
        int x=randomRes*60;
        int y=randomRes*80;
        int videoChoice=(int)(Math.random()*3);
        Settings setting=new Settings(videoList[videoChoice],x+"",y+"");

        // create Stream from Video, there are 3 constructor for Stream, two for making from only certain segment (not all)
        Stream ST=new Stream(VideoRepository.videos.get(0),setting); //admission control can work in constructor, or later?

        //Admission Control assign Priority of each segments
        AdmissionControl.AssignStreamPriority(ST);
        /* //print out priorities
        for(StreamGOP stg:ST.streamGOPs){
            System.out.println(stg.getPriority());
        }
        */
        //Scheduler
        GTS.addStream(ST);
    }
    public static void nRandomRequest(GOPTaskScheduler GTS,int Request_Numbers, int interval){
        //interval =-1 for random delay,
        for(int i=0;i< Request_Numbers;i++){
            OneRandomRequest(GTS);
        }
    }
}
