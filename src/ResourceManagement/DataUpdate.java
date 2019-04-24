package ResourceManagement;

import Scheduler.GOPTaskScheduler_mergable;
import Scheduler.SystemConfig;
import mainPackage.CVSE;

import java.io.FileWriter;
import java.io.PrintWriter;

import static java.lang.Thread.sleep;

public class DataUpdate {
    public DataUpdate(){
    }

    public void printstat(){
        //print stat!
        long avgActualSpentTime=0;
        long totalWorkDone=0,ntotalWorkDone=0;
        long totaldeadlinemiss=0, ntotaldeadlinemiss=0;
        if(CVSE.RG.finished){
            //file output
            try {
                String prefix = (CVSE.config.taskmerge) ? "merge_" : "unmerge";
                prefix += (!CVSE.config.batchqueuesortpolicy.equalsIgnoreCase("None")) ? "_Sort" : "_Unsort";
                prefix += (CVSE.config.consideratemerge) ? "" : "always_merge";
                prefix += (!CVSE.config.batchqueuesortpolicy.equalsIgnoreCase("None")) ? CVSE.config.batchqueuesortpolicy : "";
                prefix += "_";
                FileWriter F1 = new FileWriter("./resultstat/full/" + prefix + CVSE.config.profileRequestsBenchmark);
                FileWriter F2 = new FileWriter("./resultstat/numbers/" + prefix + CVSE.config.profileRequestsBenchmark);
                PrintWriter Fullwriter = new PrintWriter(F1);
                PrintWriter numberwriter = new PrintWriter(F2);
                //to screen
                Fullwriter.println("File" + CVSE.config.profileRequestsBenchmark);
                if (!CVSE.config.batchqueuesortpolicy.equalsIgnoreCase("None")) {
                    Fullwriter.println("Stat for Queuesort=" + CVSE.config.batchqueuesortpolicy + " mergable=" + CVSE.config.taskmerge);
                } else {
                    Fullwriter.println("Stat for Queuesort=" + CVSE.config.batchqueuesortpolicy + " mergable=" + CVSE.config.taskmerge);
                }
                for (int i = 0; i < CVSE.GTS.machineInterfaces.size(); i++) {
                    MachineInterface vmi = CVSE.GTS.machineInterfaces.get(i);
                    Fullwriter.println("Machine " + i + " time elapsed:" + vmi.elapsedTime + " time actually spent processing:" + vmi.actualSpentTime);
                    Fullwriter.println("completed: " + vmi.total_taskdone + "(" + vmi.total_itemdone + ") requests, missed " + vmi.total_taskmiss + "(" + vmi.total_itemmiss + ")");


                    avgActualSpentTime += vmi.actualSpentTime;
                    totalWorkDone += vmi.total_itemdone;
                    ntotalWorkDone += vmi.total_taskdone;
                    totaldeadlinemiss += vmi.total_itemmiss;
                    ntotaldeadlinemiss += vmi.total_taskmiss;
                }
                Fullwriter.println("total completed: " + totalWorkDone + "(" + ntotalWorkDone + ") missed " + totaldeadlinemiss + "(" + ntotaldeadlinemiss + ")" );
                if (CVSE.GTS instanceof GOPTaskScheduler_mergable) {
                    GOPTaskScheduler_mergable GTS = (GOPTaskScheduler_mergable) CVSE.GTS;
                    System.out.println("type A merged:" + GTS.MRG.merged_tasklvl_count);
                }

                Fullwriter.println("avgspentTime " + avgActualSpentTime / CVSE.config.maxCR);
                numberwriter.println(totalWorkDone + " , " + ntotalWorkDone + " , " + totaldeadlinemiss + " , " + ntotaldeadlinemiss + " , " + avgActualSpentTime / CVSE.config.maxCR);

                Fullwriter.close();
                numberwriter.close();
                F1.close();
                F2.close();
                System.out.println("Benchmark finished");
                if (CVSE.GTS instanceof GOPTaskScheduler_mergable){
                    GOPTaskScheduler_mergable GTS= (GOPTaskScheduler_mergable) CVSE.GTS;
                    System.out.println("Probe count=" + GTS.MRG.probecounter); //check how it works after refactor
                }
                sleep(200);
                System.exit(0);
            }catch(Exception e){
                System.out.println("printstat bug:"+e);
            }
        }else {
            //to screen
            System.out.println("File" + CVSE.config.profileRequestsBenchmark);
            if (!CVSE.config.batchqueuesortpolicy.equalsIgnoreCase("None")) {
                System.out.println("Stat for Queuesort=" + CVSE.config.batchqueuesortpolicy + " mergable=" + CVSE.config.taskmerge);
            } else {
                System.out.println("Stat for Queuesort=" + CVSE.config.batchqueuesortpolicy + " mergable=" + CVSE.config.taskmerge);
            }
            for (int i = 0; i < CVSE.GTS.machineInterfaces.size(); i++) {
                MachineInterface vmi = CVSE.GTS.machineInterfaces.get(i);
                System.out.println("Machine " + i + " time elapsed:" + vmi.elapsedTime + " time actually spent:" + vmi.actualSpentTime);
                System.out.println("completed: " + vmi.total_taskdone + "(" + vmi.total_itemdone + ") requests, missed " + vmi.total_itemmiss + "(" + vmi.total_taskmiss + ")");
                avgActualSpentTime += vmi.actualSpentTime;
                totalWorkDone += vmi.total_itemdone;
                ntotalWorkDone += vmi.total_taskdone;
                totaldeadlinemiss += vmi.total_itemmiss;
                ntotaldeadlinemiss += vmi.total_taskmiss;
            }
            System.out.println("total completed: " + totalWorkDone + "(" + ntotalWorkDone + ") missed " + totaldeadlinemiss + "(" + ntotaldeadlinemiss + ")");
            if (CVSE.GTS instanceof GOPTaskScheduler_mergable){
                GOPTaskScheduler_mergable GTS= (GOPTaskScheduler_mergable) CVSE.GTS;
                System.out.println("type A merged:" + GTS.MRG.merged_tasklvl_count);
            }
            System.out.println("avgspentTime " + avgActualSpentTime / CVSE.config.maxCR);
        }
    }
}
