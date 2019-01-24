package Simulator;

import Repository.VideoRepository;
import Scheduler.AdmissionControl;
import Scheduler.GOPTaskScheduler;
import Scheduler.ServerConfig;
import Streampkg.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

import static java.lang.Thread.sleep;



public class RequestGenerator {
   // static String videonameList[]={"bbb_trailer","ff_trailer_part1","ff_trailer_part3"}; //not using



    //only capable of generating Resolution Request
    public static void OneRandomRequest(GOPTaskScheduler GTS){
        //random a resolution
        int randomRes=(int)(Math.random()*7)+1;
        int x=randomRes*80;
        int y=randomRes*60;
        int videoChoice=(int)(Math.random()* VideoRepository.videos.size());
        //Settings setting=new Settings(videoList[videoChoice],x+"",y+"");
        String setting=x+"x"+y;
        long deadline=0; //did not specified deadline
        //setting.settingIdentifier=randomRes;
        OneSpecificRequest(GTS,videoChoice,"Resolution",setting,deadline,0);
    }

    public static void OneSpecificRequest(GOPTaskScheduler GTS, int videoChoice, String command, String setting, long deadline, long arrival){
        Stream ST=new Stream(VideoRepository.videos.get(videoChoice),command,setting,deadline,arrival); //admission control can work in constructor, or later?
        AdmissionControl.AssignStreamPriority(ST);
        GTS.addStream(ST);
        //System.out.println("test2");
    }
    //simple static RandomRequest Generator
    public static void nRandomRequest(GOPTaskScheduler GTS, int Request_Numbers, int interval, int n){
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
    public static boolean finished=false;
    static int currentIndex=0;
    //a once call to push out data that past their startTime
    public static void contProfileRequestsGen(GOPTaskScheduler GTS){
        if(currentIndex<rqe_arr.size()) {
            while (rqe_arr.get(currentIndex).appearTime <= GOPTaskScheduler.maxElapsedTime) {
                requestprofile arqe = rqe_arr.get(currentIndex);
                currentIndex++;
                OneSpecificRequest(GTS, arqe.videoChoice, arqe.command, arqe.setting, arqe.deadline,arqe.appearTime);
                if (currentIndex >= rqe_arr.size()) {
                    finished=true;
                    break;
                }
                System.out.println("currentIndex="+currentIndex+" rqe_arr size="+rqe_arr.size());
            }
        }
    }
    public static long nextappearTime(){
        if(currentIndex<rqe_arr.size()) {
            return rqe_arr.get(currentIndex).appearTime;
        }
        return -1;
    }
    ///////////////// before are creation of the profile /////////////////


    public static requestprofile[] modifyrqeb4sort(requestprofile[] original_rqe,int videos,long segmentcounts){
        //set first few requests to start from Time 0, start off with some load right away
        int maxchange=Math.min(original_rqe.length,10);
        for(int i=0;i<maxchange;i++){
            original_rqe[i].appearTime=0;
        }

        return original_rqe;
    }
    //modify the rqe after sorting by appearance time done, so
    public static requestprofile[] modifyrqeaftersort(requestprofile[] original_rqe,Random r,int videos,int requestcount,long segmentcounts) {

        double TypeArate=0.05,TypeCrate=0.2;
        int cmdspace=4;
        int cloneindex=original_rqe.length/4; //so it doesn't start immediately
        double requestspace=(videos*1.0)*cmdspace; //108 videos, 4 cmd so request space =424 ?
        double duplicated=(requestcount*1.0)/requestspace; // 130 /424 = 0% typeA natural match
        System.out.println("duplicated="+duplicated+" typeArate="+TypeArate);
        System.out.println("cloneindex="+cloneindex);
        //set 20% of the request to match type C
        duplicated*=cmdspace; //type C don't care cmd, so bring it back
        /*
        if(duplicated<1+TypeCrate) {
            System.out.println("type C match injection");
            double togo;
            if(duplicated>1) {
                togo = TypeCrate - duplicated + 1;
            }else{
            */
        double togo = TypeCrate;
        //}
        int altered = 0;
        while (altered < togo * segmentcounts) {
            //copy previous few, can duplicate
            int pminus=Math.abs(r.nextInt(3))+1;
            original_rqe[cloneindex].videoChoice = original_rqe[cloneindex - pminus].videoChoice;
            altered += 2*VideoRepository.videos.get(original_rqe[cloneindex].videoChoice).getTotalSegments();
            cloneindex += 3;  //so not too often happened, rather than cloneindex+=2
        }
        System.out.println("cloneindex="+cloneindex);
            /*
        }else{
            System.out.println("Already have too many type C match");
        }
        */


        /*if(duplicated<1+TypeArate) { //need to add more match to type A
            System.out.println("type A match injection");

            double togo;
            if(duplicated>1) {
                togo = TypeArate - duplicated + 1;
            }else{
            */
        togo = TypeArate;
            //}
            altered = 0;
            while (altered < togo * segmentcounts) {
                //copy previous few
                int pminus=Math.abs(r.nextInt(4))+1;
                original_rqe[cloneindex].command = original_rqe[cloneindex - pminus].command;
                original_rqe[cloneindex].setting = original_rqe[cloneindex - pminus].setting;
                original_rqe[cloneindex].videoChoice = original_rqe[cloneindex - pminus].videoChoice;
                altered += 2*VideoRepository.videos.get(original_rqe[cloneindex].videoChoice).getTotalSegments();
                cloneindex += 3; //so not too often happened, rather than cloneindex+=2
            }
        System.out.println("cloneindex="+cloneindex);
            /*
        }else{
            System.out.println("Already have too many type A match");
        }
        */
        /* //we don't have type B matching at the moment
        //set 10% of the request to match type B
        cloneindex=original_rqe.length/2; //this is where problem arise, mismatch setting
        for(int i= cloneindex+1;i<cloneindex+original_rqe.length/10;i+=2){
            original_rqe[i].command=original_rqe[i-1].command;
            original_rqe[i].videoChoice=original_rqe[i-1].videoChoice;
        }
        */

        return original_rqe;

    }
    //enforce least duplicate or match, unless neccessery, then modify to add matching
    public static void generateProfiledRandomRequests(String filename,long seed,int totalVideos,int videofolds,int totalRequest,long timeSpan,int avgslack,double sdslack) throws IOException {
        //random into array, modify, sort array, write to file
        Random r =new Random(seed);
        int i=0;
        FileWriter F = new FileWriter("BenchmarkInput/"+filename+".txt");
        PrintWriter writer = new PrintWriter(F);

        ArrayList<requestprofile> rqes=new ArrayList<>();
        String commandList[]={"Framerate","Resolution","Bitrate","Codec"};
        int fold=0; //each fold means there is at least one type C matchable, every 4 fold then a type A matched
        int positionMatchup[]=new int[totalVideos];
        long totalSegmentcount=0;
        //randomly make it not match at all
        while(totalSegmentcount-totalRequest<0 ){
            //created index 0-totalVideos and shuffle them
            positionMatchup=miscTools.utils.positionshuffle(r,totalVideos);

            //create the request
            for(int q=0;q<totalVideos;q++) {
                // video choice is in the positionMatchup
                String acmd=commandList[(positionMatchup[q]+fold)%4];// ensure least command overlap as possible
                long appear=Math.abs(r.nextLong()%timeSpan);
                long deadline=(long)(r.nextGaussian()*sdslack)+avgslack;
                deadline+=appear;
                //System.out.println("rqe[]="+(randomDone+q)+" positionMatup[]="+q);
                rqes.add(new requestprofile(totalVideos*fold+positionMatchup[q],acmd,"TBD",appear,deadline)); //setting ToBeDetermined
                totalSegmentcount+=VideoRepository.videos.get(totalVideos*fold+positionMatchup[q]).getTotalSegments();

                if(totalSegmentcount-totalRequest>=0){
                    break;
                }
            }
            fold++;
            fold%=videofolds;
        }
        int randomDone=rqes.size();
        requestprofile rqe[]=(requestprofile[])rqes.toArray(new requestprofile[rqes.size()]);
        //modify
        modifyrqeb4sort(rqe,randomDone,totalSegmentcount);
        //sort
        Arrays.sort(rqe);
        //modify again?
        modifyrqeaftersort(rqe,r,randomDone,totalRequest,totalSegmentcount);
        // write to file
        System.out.println("randomized "+ totalSegmentcount+" segments");
        for(i=0;i<randomDone;i++){
            writer.println(rqe[i].videoChoice+" "+rqe[i].command+" "+rqe[i].setting+" "+rqe[i].appearTime+" "+rqe[i].deadline);
        }
        writer.close();

    }

}
