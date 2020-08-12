package TimeEstimatorpkg;

import ResourceManagement.MachineInterface;
import SessionPkg.TranscodingRequest;
import mainPackage.CVSE;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by pi on 5/21/17.
 */


public class TimeEstimator {

    HashMap<String,HashMap<String,histStat>> detailedTable=new HashMap<>(); //two layers, one for machine then one for the rest of parameters
    HashMap<String,Double> mergeTable=new HashMap<>(); //merge time saving
    public TimeEstimator(){
    }
    public String hashkey( String operation, String param, String Datasource){ //machine type, operation, parameter, video (data) can set as Generic
        return operation+'_'+param+'_'+Datasource;
    }
    public void updateTable(String VMclass, HashMap<String,histStat> runtime_report) {

    }

    //design decision, should this return per segment or per action?
    //select do it all in here!
    //get port data just because fixedBandwidth machine store Bandwidth in their port

    public histStat getHistoricProcessTime(MachineInterface VM, TranscodingRequest segment){
        System.out.println("check historical time of "+segment.DataSource+" on VM type "+ VM.VM_class);
//        try {
//            Thread.sleep(20);
//        }catch (Exception e){
//            System.out.println("sleep bug");
//        }
        //for fixedBandwidth case
        if(VM.VM_class.equalsIgnoreCase("fixedBandwidth")) {
//            long bandwidth = VM.port;
//            System.out.println("bandwidth=" + bandwidth * 8);
//            System.out.println("request HistoricProcessTime of a fixedBandwidth VM, data is not reliable at the moment, use with caution-- only return transferTIme, no transmission delay");
//            //System.out.println("segment size=" + segment.fileSize);
//            return new histStat(segment.fileSize / bandwidth + 1, 1);
            System.out.println("Warning time estimation for fixedbandwidth is disabled");
            return new histStat(1000, 1);
        }
        //for other case
        //poll machine type
        HashMap<String, histStat> polled1 = detailedTable.get(VM.VM_class);
        if (polled1 != null) { //have the machine type data
            //if(CVSE.config.timeEstimatorMode.equalsIgnoreCase("profiled")) {
                return searchHistoricProcessTime(polled1, segment,"profiled");
            //}
        }else{
            System.out.println("\nNo historic data for this machine type!: " + VM.VM_class);
            return new histStat(-1, -1); //set at arbitary value
        }
    }

    public histStat searchHistoricProcessTime(HashMap<String,histStat> table,TranscodingRequest segment,String searchMode){
        //SDcoefficient=1 is Worst case, -1 is BestCase,
        //System.out.println("found machine type, now search the record");

        long ESTTime = 0;
        double SD = 0;
        ArrayList<String> cmdlist=new ArrayList<String>();
        double sumsave=0; //sum saving, then average later
        //System.out.println(segment.listallCMD());
        for (String cmd : segment.listallCMD()) { //all cmd
            //System.out.println("cmd="+cmd);
            //System.out.println(segment.listparamsofCMD(cmd));
            if(cmd.equalsIgnoreCase("Custom")) { //customized command, no history
                ESTTime+=2000; //use prelim value
                SD+=50; //use prelim value
            }else{
                for (String param : segment.listparamsofCMD(cmd) ) { //all param
                    //actually ignore everything in TranscodingRequest at this moment
                    String pollstr;
                    //System.out.println("tablekeyset="+table.keySet());

                    if (searchMode.equalsIgnoreCase("profiled")) {
                        //System.out.println("Time Estimator for cmd="+cmd+" param="+param+" segment="+segment.segment+" vname="+segment.videoname);
                        pollstr = hashkey(cmd, param, segment.DataSource);
                    } else if (searchMode.equalsIgnoreCase("operation")) { //operation based
                        pollstr = cmd;
                    } else {
                        pollstr = cmd + param; //operation+param based
                    }
                    //System.out.println("pollstr="+pollstr);
                    histStat polled2 = table.get(pollstr);
                    //System.out.println("keyset=" + polled1.keySet());

                    if (polled2 != null) {
                        ESTTime += polled2.mean;
                        SD += polled2.SD;
                        cmdlist.add(cmd+'+'+param);
                    } else {
                        System.out.println("No historic data for this cmd!:" + cmd + " param:" + param + " Video Sessionid=" +segment.DataSource + " pollstr=" + pollstr);
                        ESTTime+=99999;
                    }
                }
            }
        }
        if(segment.requestcount>1){ //mere than one request merged together, so we save sometime from merging
            double mergesaving=mergeSaving(segment,cmdlist);
            ESTTime*=1-mergesaving;
            SD*=1-mergesaving;
        }
        System.out.println("Normal Estimation mean="+ESTTime+" SD="+SD);
        return new histStat(ESTTime, SD);
    }
    //can be replaced with better solution later if there are more merge data
    public double mergeSaving(TranscodingRequest segment,ArrayList<String> cmdlist){
        //now calculate the saving from all the merge
        double saving=0;
        if(segment.requestcount>2){ //complex merge...
            // WHAT TO DO WITH COMPLEX MERGING... ,estimating it?
            //if(segment.cmdSet.containsKey("CODEC")){ //saving around 15%
            //    saving=0.15;
            //}else{
                saving=0.32;
           // }
        }else if(segment.requestcount==2){ //simple two operation merge
            String mergecmd=cmdlist.get(0)+"+"+cmdlist.get(1);
            if(mergeTable.containsKey(mergecmd)) {
                double savingPercent = mergeTable.get(mergecmd);
                saving= savingPercent*0.01; //convert from percentage saving
            }else{
                System.out.println("Warning, saving ratio not found");
            }
        } // else{} //no merge, then don't deduct transcoding time
        return saving;
    }

    public long getHistoricProcessTimeLong(MachineInterface VM,TranscodingRequest segment,double SDco) {
    histStat rS=getHistoricProcessTime(VM,segment);
    return rS.mean+ (long)(rS.SD*SDco);
    }
    //function called at the beginning of running to populate data
    public void populate(String VMclass){

        if(VMclass.equalsIgnoreCase("fixedBandwidth")){
            System.out.println("fixed bandwidth machine, no need to populate TimeEstimator table");
            //doNothing
        }else {
            //System.out.println("populate table"+VMclass);
            File F = new File("profile" + File.separator + VMclass + ".txt");
            Scanner scanner = null;
            try {
                scanner = new Scanner(F);
            } catch (Exception e) {
                System.out.println("Time estimator error"+e);
            }

            HashMap<String, histStat> X = new HashMap<>();
            while (scanner.hasNext()) {

                long mean;
                double SD;
                String command;
                String setting;
                String vsegment;
                String fullline = scanner.nextLine();
                //System.out.println(fullline);
                String[] line = fullline.split(",");
                if (line.length == 5) {
                    command = line[0];
                    setting = line[1];
                    vsegment= line[2];
                    mean = (long)Double.parseDouble(line[3]);
                    SD = Double.parseDouble(line[4])* CVSE.config.sdmultiplier; //SD multiplier can be changed to be different from 1.0
                    histStat S = new histStat(mean, SD);
                    X.put(command +"_"+setting+"_"+vsegment, S);
                    //System.out.println(line[1]);
                } else {
                    System.out.println("profile not correctly formatted");
                }
            }
            //F.close
            //System.out.println("read a profile");
            detailedTable.put(VMclass, X);
            populateMergeData(VMclass); //eventually maybe per VM basis, now it is global average time saving estimation
        }
    }
    public void populateMergeData(String VMclass){
        //currently ignore all the VMclass, use a normalized merge table
        File F = new File("profile"+File.separator+"MergeInfo.txt");
        Scanner scanner = null;
        try {
            scanner = new Scanner(F);
        } catch (Exception e) {
            System.out.println("Time merge saving profile error"+e);
        }
        HashMap<String, histStat> X = new HashMap<>();
        while (scanner.hasNext()) {
            String[] line=scanner.nextLine().split(",");
            if(line.length==3) {
                mergeTable.put(line[0]+'+'+line[1], Double.parseDouble(line[2]));
                mergeTable.put(line[1]+'+'+line[0], Double.parseDouble(line[2]));
            }else{
                System.out.println("invalid merge record");
            }
        }
    }
    public void SetSegmentProcessingTime(TranscodingRequest segment)
    {
        // set deadline Time to System.currentTime() +getHistoricProcessTime(segment)+ constant ?
        // don't use System.nanoTime Across the machine
    }

}