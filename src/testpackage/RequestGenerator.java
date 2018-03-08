package testpackage;

import Repository.VideoRepository;
import Scheduler.AdmissionControl;
import Scheduler.GOPTaskScheduler;
import Scheduler.ServerConfig;
import Stream.Settings;
import Stream.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

import static java.lang.Thread.sleep;

class requestprofile implements Comparable<requestprofile> {
    public int videoChoice;
    public String command;
    public String setting;
    public long appearTime;
    public long deadline;
//recipe is at Video level, each presentation time of each GOPS are in repository video
    public requestprofile(int videoChoice, String command, String setting, long appearTime, long deadline) {
        this.videoChoice = videoChoice;
        this.command = command;
        this.setting = setting;
        this.appearTime = appearTime;
        this.deadline = deadline;
    }

    @Override
    public int compareTo(requestprofile requestprofile) {
        if (requestprofile != null) {
            if (this.appearTime > requestprofile.appearTime) {
                return 1;
            } else if (this.appearTime < requestprofile.appearTime) {
                return -1;
            }
            return 0;
        }
        return -1;
    }
}

public class RequestGenerator {
   // static String videonameList[]={"bbb_trailer","ff_trailer_part1","ff_trailer_part3"}; //not using



    //only capable of generating Resolution Request
    public static void OneRandomRequest(GOPTaskScheduler GTS){
        //random a resolution
        int randomRes=(int)(Math.random()*7)+1;
        int x=randomRes*80;
        int y=randomRes*60;
        int videoChoice=(int)(Math.random()* ServerConfig.videoList.size());
        //Settings setting=new Settings(videoList[videoChoice],x+"",y+"");
        String setting=x+"x"+y;
        long deadline=0; //did not specified deadline
        //setting.settingIdentifier=randomRes;
        OneSpecificRequest(GTS,videoChoice,"Resolution",setting,deadline);
    }

    public static void OneSpecificRequest(GOPTaskScheduler GTS,int videoChoice,String command,String setting,long deadline){
        Stream ST=new Stream(VideoRepository.videos.get(videoChoice),command,setting,deadline); //admission control can work in constructor, or later?
        AdmissionControl.AssignStreamPriority(ST);
        GTS.addStream(ST);
        //System.out.println("test2");
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
    private static ArrayList<requestprofile> rqe_arr=new ArrayList<>();

    //read all data profile to rqe
    public static void ReadProfileRequests(String filename){
        File F=new File("BenchmarkInput/"+filename);
        Scanner scanner= null;
        try {
            scanner = new Scanner(F);
            while(scanner.hasNext()){
                String aline[]=scanner.nextLine().split("\\s+");
                if(aline.length==5){
                    requestprofile aRequest=new requestprofile(Integer.parseInt(aline[0]),aline[1],aline[2],Long.parseLong(aline[3]),Long.parseLong(aline[4]));
                    rqe_arr.add(aRequest);
                }else{
                    System.out.println("invalid format found");
                }
            }
        } catch (Exception e) {
            System.out.println("read benchmarkProfileFail"+e);
        }
    }
    static boolean finished=false;
    static int currentIndex=0;
    //a once call to push out data that past their startTime
    public static void contProfileRequestsGen(GOPTaskScheduler GTS){
        if(currentIndex<rqe_arr.size()) {
            while (rqe_arr.get(currentIndex).appearTime < GOPTaskScheduler.maxElapsedTime) {
                requestprofile arqe = rqe_arr.get(currentIndex);
                currentIndex++;
                OneSpecificRequest(GTS, arqe.videoChoice, arqe.command, arqe.setting, arqe.deadline);
                if (currentIndex >= rqe_arr.size()) {
                    finished=true;
                    break;
                }
            }
        }
    }

    public static requestprofile[] modifyrqe(requestprofile[] original_rqe){
        //set first few requests to start from Time 0
        int maxchange=Math.min(original_rqe.length,5);
        for(int i=0;i<maxchange;i++){
            original_rqe[i].appearTime=0;
        }
        //set 5% of the request to match type A
        int cloneindex=original_rqe.length/5;
        for(int i= cloneindex+1;i<cloneindex+original_rqe.length/20;i++){
            original_rqe[i].command=original_rqe[cloneindex].command;
            original_rqe[i].setting=original_rqe[cloneindex].setting;
            original_rqe[i].videoChoice=original_rqe[cloneindex].videoChoice;
        }
        //set 10% of the request to match type B
        cloneindex=original_rqe.length/3; //this is where problem arise, mismatch setting
        for(int i= cloneindex+1;i<cloneindex+original_rqe.length/10;i++){
            original_rqe[i].command=original_rqe[cloneindex].command;
            original_rqe[i].videoChoice=original_rqe[cloneindex].videoChoice;
        }
        //set 20% of the request to match type C
        cloneindex=original_rqe.length/2;
        for(int i= cloneindex+1;i<cloneindex+original_rqe.length/5;i++){
            original_rqe[i].videoChoice=original_rqe[cloneindex].videoChoice;
        }

        return original_rqe;
    }

    //generator function, save to file
    public static void generateDistributedRandomRequests(String filename,long seed,int totalVideos,int totalRequest,long timeSpan,int avgslack,double sdslack) throws IOException {
        //random into array, modify, sort array, write to file
        Random r =new Random(seed);
        int i=0;
        FileWriter F = new FileWriter("BenchmarkInput/"+filename+".txt");
        PrintWriter writer = new PrintWriter(F);
        requestprofile rqe[]=new requestprofile[totalRequest];
        String commandList[]={"Bitrate","Resolution","to1000","Codec"};
        //random
        for(i=0;i<totalRequest;i++) {
            //int videoChoice, String command, String setting,long appearTime, long deadline
            int randomSetting=r.nextInt(4);
            int randomCommand=r.nextInt(4);
            String setting ="";
            setting = "settingChoice_"+randomSetting;
            long appear=r.nextLong()%timeSpan;
            if(appear<0){
                appear*=-1;
            }
            long deadline=(long)(r.nextGaussian()*sdslack)+avgslack;
            deadline+=appear;
            rqe[i]=new requestprofile(r.nextInt(totalVideos),commandList[randomCommand],setting,appear,deadline);
        }
        //modify
        requestprofile modded_rqe[]=modifyrqe(rqe);
        //sort
        Arrays.sort(modded_rqe);

        // write to file
        for(i=0;i<totalRequest;i++){
            writer.println(modded_rqe[i].videoChoice+" "+modded_rqe[i].command+" "+modded_rqe[i].setting+" "+modded_rqe[i].appearTime+" "+modded_rqe[i].deadline);
        }
        writer.close();
    }
    //enforce least duplicate or match, unless neccessery
    public static void generateProfiledRandomRequests(String filename,long seed,int totalVideos,int totalRequest,long timeSpan,int avgslack,double sdslack) throws IOException {
        //random into array, modify, sort array, write to file
        Random r =new Random(seed);
        int i=0;
        FileWriter F = new FileWriter("BenchmarkInput/"+filename+".txt");
        PrintWriter writer = new PrintWriter(F);
        requestprofile rqe[]=new requestprofile[totalRequest];
        String commandList[]={"Framerate","Resolution","Bitrate","Codec"};
        int randomDone=0;
        int fold=0; //each fold means there is at least one type C matchable, every 4 fold then a type A matched
        int positionMatchup[]=new int[totalVideos];
        //randomly make it not match at all
        while(totalRequest-randomDone>0 ){
            int randomtodo=Math.min(totalVideos,totalRequest-randomDone); //random upto totalVideos
            //created index 0-totalVideos in order before shuffle
            for(int p=0;p<randomtodo;p++){
                positionMatchup[p]=p;
            }
            //shuffle, swap s with random index
            for(int s=0;s<randomtodo;s++){
                int tmp=positionMatchup[s];
                int swappair=Math.abs(r.nextInt()%randomtodo);
                positionMatchup[s]=positionMatchup[swappair];
                positionMatchup[swappair]=tmp;
            }
            //create the request
            for(int q=0;q<randomtodo;q++) {
                // video choice is in the positionMatchup
                String acmd=commandList[(positionMatchup[q]+fold)%4];// ensure least command overlap as possible
                long appear=Math.abs(r.nextLong()%timeSpan);
                long deadline=(long)(r.nextGaussian()*sdslack)+avgslack;
                deadline+=appear;
                //System.out.println("rqe[]="+(randomDone+q)+" positionMatup[]="+q);
                rqe[randomDone+q]=new requestprofile(positionMatchup[q],acmd,"TBD",appear,deadline); //setting ToBeDetermined
            }

            fold++;
            randomDone+=randomtodo;
        }

        //modify
        //requestprofile modded_rqe[]=modifyrqe(rqe);
        //sort
        Arrays.sort(rqe);

        // write to file
        for(i=0;i<totalRequest;i++){
            writer.println(rqe[i].videoChoice+" "+rqe[i].command+" "+rqe[i].setting+" "+rqe[i].appearTime+" "+rqe[i].deadline);
        }
        writer.close();

        writer.close();
    }

}
