package testpackage;

import Repository.VideoRepository;
import Scheduler.AdmissionControl;
import Scheduler.GOPTaskScheduler;
import Scheduler.ServerConfig;
import Stream.Settings;
import Stream.*;
import static java.lang.Thread.sleep;

public class RequestGenerator {
    static String videoList[]={"bbb_trailer","ff_trailer_part1","ff_trailer_part3"};
    public static void OneRandomRequest(GOPTaskScheduler GTS){
        //random a resolution
        int randomRes=(int)(Math.random()*7)+1;
        int x=randomRes*80;
        int y=randomRes*60;
        int videoChoice=(int)(Math.random()* ServerConfig.videoList.size());
        //Settings setting=new Settings(videoList[videoChoice],x+"",y+"");
        String setting=x+"x"+y;
        //setting.settingIdentifier=randomRes;

        // create Stream from Video, there are 3 constructor for Stream, two for making from only certain segment (not all)
        Stream ST=new Stream(VideoRepository.videos.get(videoChoice),"Resolution",setting); //admission control can work in constructor, or later?
        //Admission Control assign Priority of each segments
        AdmissionControl.AssignStreamPriority(ST);
        /* //print out priorities
        for(StreamGOP stg:ST.streamGOPs){
            System.out.println(stg.getPriority());
        }
        */
        //Scheduler
        GTS.addStream(ST);
        System.out.println("test2");
    }
    //simple static RandomRequest Generator
    public static void nRandomRequest(GOPTaskScheduler GTS,int Request_Numbers, int interval,int n){
        //interval =-1 for random delay

        int round = 1;
        do {
            for (int i = 0; i < Request_Numbers; i++) {
                OneRandomRequest(GTS);
            }
            round++;
            if(interval>0&&round<n) {
                try {
                    sleep(interval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } while(round<n);
    }
}
