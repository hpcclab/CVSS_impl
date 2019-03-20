package ResourceManagement;

import Scheduler.GOPTaskScheduler;
import Scheduler.GOPTaskScheduler_mergable;
import Scheduler.ServerConfig;
import Simulator.RequestGenerator;

import java.io.FileWriter;
import java.io.PrintWriter;

import static java.lang.Thread.sleep;

public class DataUpdate {


    public static void printstat(){
        //print stat!
        long avgActualSpentTime=0;
        long totalWorkDone=0,ntotalWorkDone=0;
        long totaldeadlinemiss=0, ntotaldeadlinemiss=0;
        if(RequestGenerator.finished){
            //file output
            try {
                String prefix=(ServerConfig.taskmerge)?"merge_":"unmerge";
                prefix+= (!ServerConfig.batchqueuesortpolicy.equalsIgnoreCase("None"))?"_Sort":"_Unsort";
                prefix+= (ServerConfig.consideratemerge)?"":"always_merge";
                prefix+= (!ServerConfig.batchqueuesortpolicy.equalsIgnoreCase("None"))? ServerConfig.batchqueuesortpolicy:"";
                prefix+="_";
                FileWriter F1 = new FileWriter("./resultstat/full/" + prefix+ServerConfig.profileRequestsBenhmark);
                FileWriter F2 = new FileWriter("./resultstat/numbers/" + prefix+ServerConfig.profileRequestsBenhmark);
                PrintWriter Fullwriter = new PrintWriter(F1);
                PrintWriter numberwriter = new PrintWriter(F2);
                //to screen
                Fullwriter.println("File" + ServerConfig.profileRequestsBenhmark);
                if (!ServerConfig.batchqueuesortpolicy.equalsIgnoreCase("None")) {
                    Fullwriter.println("Stat for Queuesort=" + ServerConfig.batchqueuesortpolicy  + " mergable=" + ServerConfig.taskmerge);
                } else {
                    Fullwriter.println("Stat for Queuesort=" + ServerConfig.batchqueuesortpolicy  + " mergable=" + ServerConfig.taskmerge);
                }
                for (int i = 0; i < GOPTaskScheduler.machineInterfaces.size(); i++) {
                    MachineInterface vmi = GOPTaskScheduler.machineInterfaces.get(i);
                    Fullwriter.println("Machine " + i + " time elapsed:" + vmi.elapsedTime + " time actually spent:" + vmi.actualSpentTime);
                    Fullwriter.println("completed: " + vmi.total_taskdone + "(" + vmi.total_itemdone + ") requests, missed " + vmi.total_itemmiss + "(" + vmi.total_taskmiss + ")");


                    avgActualSpentTime += vmi.actualSpentTime;
                    totalWorkDone += vmi.total_itemdone;
                    ntotalWorkDone += vmi.total_taskdone;
                    totaldeadlinemiss += vmi.total_itemmiss;
                    ntotaldeadlinemiss += vmi.total_taskmiss;
                }
                Fullwriter.println("total completed: " + totalWorkDone + "(" + ntotalWorkDone + ") missed " + totaldeadlinemiss + "(" + ntotaldeadlinemiss + ") type A merged:" + GOPTaskScheduler_mergable.mrg.merged_tasklvl_count);
                Fullwriter.println("avgspentTime " + avgActualSpentTime / ServerConfig.maxVM);
                numberwriter.println(totalWorkDone+" , "+ntotalWorkDone+" , "+totaldeadlinemiss+" , "+ntotaldeadlinemiss+" , "+avgActualSpentTime / ServerConfig.maxVM);

                Fullwriter.close();
                numberwriter.close();
                F1.close();
                F2.close();
                System.out.println("Benchmark finished");
                System.out.println("Probe count=" + GOPTaskScheduler_mergable.mrg.probecounter); //check how it works after refactor
                sleep(200);
                System.exit(0);
            }catch(Exception e){
                System.out.println("printstat bug:"+e);
            }
        }else {
            //to screen
            System.out.println("File" + ServerConfig.profileRequestsBenhmark);
            if (!ServerConfig.batchqueuesortpolicy.equalsIgnoreCase("None")) {
                System.out.println("Stat for Queuesort=" + ServerConfig.batchqueuesortpolicy + " mergable=" + ServerConfig.taskmerge);
            } else {
                System.out.println("Stat for Queuesort=" + ServerConfig.batchqueuesortpolicy + " mergable=" + ServerConfig.taskmerge);
            }
            for (int i = 0; i < GOPTaskScheduler.machineInterfaces.size(); i++) {
                MachineInterface vmi = GOPTaskScheduler.machineInterfaces.get(i);
                System.out.println("Machine " + i + " time elapsed:" + vmi.elapsedTime + " time actually spent:" + vmi.actualSpentTime);
                System.out.println("completed: " + vmi.total_taskdone + "(" + vmi.total_itemdone + ") requests, missed " + vmi.total_itemmiss + "(" + vmi.total_taskmiss + ")");
                avgActualSpentTime += vmi.actualSpentTime;
                totalWorkDone += vmi.total_itemdone;
                ntotalWorkDone += vmi.total_taskdone;
                totaldeadlinemiss += vmi.total_itemmiss;
                ntotaldeadlinemiss += vmi.total_taskmiss;
            }
            System.out.println("total completed: " + totalWorkDone + "(" + ntotalWorkDone + ") missed " + totaldeadlinemiss + "(" + ntotaldeadlinemiss + ") type A merged:" + GOPTaskScheduler_mergable.mrg.merged_tasklvl_count);
            System.out.println("avgspentTime " + avgActualSpentTime / ServerConfig.maxVM);
        }
    }
}
