package Simulator;

import Streampkg.Stream;
import mainPackage.CVSE;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

import static java.lang.Thread.sleep;



public class RequestGenerator {
    private Semaphore canGenTask;
    public boolean finished=false;
    // static String videonameList[]={"bbb_trailer","ff_trailer_part1","ff_trailer_part3"}; //not using
    public RequestGenerator(){
        canGenTask=new Semaphore(1);
    }

    public void OneRandomRequest(){
        //random a resolution example.
        //int randomRes=(int)(Math.random()*7)+1;
        //int x=randomRes*80;
        //int y=randomRes*60;
        //Settings setting=new Settings(videoList[videoChoice],x+"",y+"");
        //String setting=x+"x"+y;

        int videoChoice=(int)(Math.random()* ( CVSE.VR.videos.size()));
        int operation=(int)(Math.random()*(CVSE.GTS.possible_Operations.size()));
        String setting=""+(Math.random()*2); //random between 0 and 1 as setting identifier
        long deadline=0; //did not specified deadline
        //setting.settingIdentifier=randomRes;
        OneSpecificRequest(videoChoice,CVSE.GTS.possible_Operations.get(operation).operationname,setting,deadline,0);
    }

    public void OneSpecificRequest( int videoChoice, String command, String setting, long deadline, long arrival){
        System.out.print("create one specific request: ");
        Stream ST=new Stream(CVSE.VR.videos.get(videoChoice),command,setting,deadline,arrival); //admission control can work in constructor, or later?
        System.out.println(ST.video.name);
        CVSE.GTS.addStream(ST);
    }
    //simple static RandomRequest Generator
    public void nRandomRequest(int Request_Numbers, int interval, int n){
        //interval =-1 for random delay

        int round = 1;
        do {
            for (int i = 0; i < Request_Numbers; i++) {
                OneRandomRequest();
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
    public void ReadProfileRequests(String filename){
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
    int currentIndex=0;

    //Something bug?, with merging sometime doesn't finish...
    public void contProfileRequestsGen(){
        if(canGenTask.tryAcquire()) {
            System.out.println("Check task arrival");
            if (currentIndex < rqe_arr.size()) {
                while (rqe_arr.get(currentIndex).appearTime <= CVSE.GTS.maxElapsedTime) {
                    requestprofile arqe = rqe_arr.get(currentIndex);
                    currentIndex++;
                    OneSpecificRequest(arqe.videoChoice, arqe.command, arqe.setting, arqe.deadline, arqe.appearTime);
                    if (currentIndex >= rqe_arr.size()) {
                        System.out.println("sim finished");
                        CVSE.VMP.datacolEvent.stop();
                        finished = true;
                        break;
                    }
                    System.out.println("currentIndex=" + currentIndex + " rqe_arr size=" + rqe_arr.size());
                }
            }
            canGenTask.release();
        }else{
            System.out.println("RequestGenerator is still busy, not generating task");
        }
    }
    public long nextappearTime(){
        if(currentIndex<rqe_arr.size()) {
            return rqe_arr.get(currentIndex).appearTime;
        }
        return -1;
    }
    ///////////////// before are creation of the profile /////////////////


    public requestprofile[] modifyrqeb4sort(requestprofile[] original_rqe,int videos,long segmentcounts){
        //set first few requests to start from Time 0, start off with some load right away
        int maxchange=Math.min(original_rqe.length,10);
        for(int i=0;i<maxchange;i++){
            original_rqe[i].appearTime=0;
        }
        System.out.println("modify before sort, set few video's starting time to 0");
        return original_rqe;
    }
    private static void cloneA(requestprofile[] original_rqe,int oriindex,int offset){
        original_rqe[oriindex].setting = original_rqe[oriindex - offset].setting;
        cloneB(original_rqe,oriindex,offset);
    }
    private static void cloneB(requestprofile[] original_rqe,int oriindex,int offset){
        original_rqe[oriindex].command = original_rqe[oriindex - offset].command;
        cloneC(original_rqe,oriindex,offset);
    }
    private static void cloneC(requestprofile[] original_rqe,int oriindex,int offset){
        original_rqe[oriindex].videoChoice = original_rqe[oriindex - offset].videoChoice;
        //validation
        System.out.println("original (oriindex="+oriindex+":"+original_rqe[oriindex].videoChoice+" "+original_rqe[oriindex].command+" "+original_rqe[oriindex].setting);
        System.out.println("copy (offset="+offset+"):"+original_rqe[oriindex-offset].videoChoice+" "+original_rqe[oriindex-offset].command+" "+original_rqe[oriindex-offset].setting);
    }
    //modify the rqe after sorting by appearance time done, so
    public requestprofile[] modifyrqeaftersort(requestprofile[] original_rqe,Random r,int videos,int requestcount,long segmentcounts) {

        double TypeArate=0.05,TypeBrate=0.15,TypeCrate=0.2;
        int cmdspace=CVSE.GTS.possible_Operations.size();
        int paramspace=2;
        int cloneindex=original_rqe.length-1;
        double requestspace=(CVSE.VR.videos.size()*1.0)*cmdspace*paramspace; //100 videos, 3 cmd, 2 params so request space =600 ?
        double typeA_duplicated=Math.max(requestcount*1.0/requestspace-1,0.0);
        double typeB_duplicated=Math.max(requestcount*1.0*paramspace/requestspace-1,0.0);
        double typeC_duplicated=Math.max(requestcount*1.0*paramspace*cmdspace/requestspace-1,0.0);

        System.out.println("natural duplicatedA ="+typeA_duplicated+" duplicatedB="+typeB_duplicated+" duplicatedC="+typeC_duplicated);
        int altered;
        int clonecompleted=0;
        //start clone from the end instead of from the front, a b c type are more scattered now
        while((typeA_duplicated < TypeArate || typeB_duplicated < TypeBrate|| typeC_duplicated < TypeCrate)&&cloneindex >4) {
            if (typeA_duplicated < TypeArate && cloneindex >4) {
                //copy previous few
                int pminus = Math.abs(r.nextInt(4)) + 1; //pick 1-4 spots ago
                cloneA(original_rqe, cloneindex, pminus);
                altered = 2 * CVSE.VR.videos.get(original_rqe[cloneindex].videoChoice).getTotalSegments();
                cloneindex -= 3; //so not too often happened
                typeA_duplicated += altered * 1.0 / segmentcounts;
                //System.out.println(" altered="+altered+" segmentcount="+segmentcounts);
                //System.out.println("duplicatedA ="+typeA_duplicated+" duplicatedB="+typeB_duplicated+" duplicatedC="+typeC_duplicated);
            }
            if (typeB_duplicated < TypeBrate && cloneindex >4) {
                //copy previous few
                int pminus = Math.abs(r.nextInt(4)) + 1;
                cloneB(original_rqe, cloneindex, pminus);
                altered = 2 * CVSE.VR.videos.get(original_rqe[cloneindex].videoChoice).getTotalSegments();
                cloneindex -= 3;
                typeB_duplicated += altered * 1.0 / segmentcounts;

            }
            if (typeC_duplicated < TypeCrate && cloneindex >4) { //it's actually less likely we need to inject type c match
                //copy previous few
                int pminus = Math.abs(r.nextInt(4)) + 1;
                cloneC(original_rqe, cloneindex, pminus);
                altered = 2 * CVSE.VR.videos.get(original_rqe[cloneindex].videoChoice).getTotalSegments();
                cloneindex -= 3;
                typeC_duplicated += altered * 1.0 / segmentcounts;

            }
        }

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
    //simple linear random
    public long randTimeLinear(Random r,long timeSpan){
        return Math.abs(r.nextLong() % timeSpan);
    }
    //create high and low work load intensity pulse
    public long randTimeInterval(Random r,long timeSpan,int hiperiodlength,int lowperiodlength,double highperiodAmp){
        long pulselength=hiperiodlength+lowperiodlength;
        long pseudopulselength=lowperiodlength+ (long)(hiperiodlength*highperiodAmp);
        long pseudotimeSpan=timeSpan/pulselength*pseudopulselength;

        long rand=Math.abs(r.nextLong() % pseudotimeSpan);
        long flop=rand/pseudopulselength;
        long remain=rand%pseudopulselength;
        long transformedTime=flop*pulselength;
        if(remain<=lowperiodlength){ //end in low range
            return transformedTime+remain;
        }else{ // end in high range
            return transformedTime+lowperiodlength+ (long)((remain-lowperiodlength)/highperiodAmp);
        }
    }
    //enforce least duplicate or match, unless neccessery, then modify to add matching
    public void generateProfiledRandomRequests(String filename,long seed,int totalVideos,int totalRequest,long timeSpan,int avgslack,double sdslack){
        int highPeriod=20000,lowPeriod=20000;
        double highAmp=3.0;
        //random into array, modify, sort array, write to file
        Random r =new Random(seed);
        int parametertypes=2; //number of parameter types
        int i=0;
        try {
            FileWriter F = new FileWriter("BenchmarkInput/" + filename + ".txt");
            PrintWriter writer = new PrintWriter(F);

            ArrayList<requestprofile> rqes = new ArrayList<>();
            //String commandList[]={"Framerate","Resolution","Bitrate","Codec"};
            int fold = 0; //each fold means there is at least one type C matchable,every 2 fold then a type B matched
            // , every 4 fold then a type A matched
            int positionMatchup[] = new int[totalVideos];
            long totalSegmentcount = 0;
            //randomly make it not match at all
            while (totalSegmentcount - totalRequest < 0) {
                //created index 0-totalVideos and shuffle them
                positionMatchup = miscTools.utils.positionshuffle(r, totalVideos);

                //create the request
                for (int q = 0; q < totalVideos; q++) {
                    // video choice is in the positionMatchup
                    String acmd = CVSE.GTS.possible_Operations.get((positionMatchup[q] +2+ fold/parametertypes) % CVSE.GTS.possible_Operations.size()).operationname;// ensure least command overlap as possible
                    //long appear = randTimeLinear(r,timeSpan);
                    long appear =randTimeInterval(r,timeSpan,highPeriod,lowPeriod,highAmp);
                    int thisslacktime;
                    if(avgslack==0){ // if not set, use default value for now
                        if(acmd.equalsIgnoreCase("Codec")){
                            thisslacktime=80000;
                        }else{
                            thisslacktime=15000;
                        }
                    }else{ //if set, use slacktime that is set
                        thisslacktime=avgslack;
                    }
                    long deadline = (long) (r.nextGaussian() * sdslack) + thisslacktime;
                    deadline += appear;
                    int settingnum = (q + fold) % parametertypes;
                    //System.out.println("rqe[]="+(randomDone+q)+" positionMatup[]="+q);
                    rqes.add(new requestprofile(positionMatchup[q], acmd, "" + settingnum, appear, deadline)); //setting ToBeDetermined
                    //System.out.println("b");
                    totalSegmentcount += CVSE.VR.videos.get(positionMatchup[q]).getTotalSegments();
                    //System.out.println("c");
                    if (totalSegmentcount - totalRequest >= 0) {
                        break;
                    }
                }
                fold++;
            }
            int randomDone = rqes.size();
            requestprofile rqe[] = (requestprofile[]) rqes.toArray(new requestprofile[rqes.size()]);
        /*
        for(i=0;i<rqe.length;i++){
            System.out.println("array "+rqe[i].videoChoice);
        }
        */
            //modify
            modifyrqeb4sort(rqe, randomDone, totalSegmentcount); //make some few segment start at time 0
            //sort
            Arrays.sort(rqe);
            System.out.println("rqe length=" + rqe.length);
            //modify again?
            modifyrqeaftersort(rqe, r, randomDone, rqe.length, totalSegmentcount); //enforce some duplication
            // write to file
            System.out.println("randomized " + totalSegmentcount + " segments");
            for (i = 0; i < randomDone; i++) {
                writer.println(rqe[i].videoChoice + " " + rqe[i].command + " " + rqe[i].setting + " " + rqe[i].appearTime + " " + rqe[i].deadline);
            }
            //System.out.println("file wrote");
            writer.close();
        }catch (Exception e){
            System.out.println("Request generator error:"+e);
            e.printStackTrace();
        }
    }

}