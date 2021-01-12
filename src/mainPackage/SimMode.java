package mainPackage;

import java.util.Scanner;

import static java.lang.Thread.sleep;

//now call from main of realmode test
public class SimMode {


    public static String trysleep(int time){
        try {
            sleep(time);
        }catch (Exception e) {
        return "Failed: " + e;
    }
        return "";
    }
    public static String Sim(String confFile, String opt) {

        Scanner scanner = new Scanner(System.in);
        //read config file

        int rqn = 1, interval, n;
        if (CVSE.config.profiledRequests) {
            // read benchmark input
            if (!opt.equalsIgnoreCase("config")&&!opt.equalsIgnoreCase("testconfig.txt")) {
                System.out.println("overwrite profileRequestBenhmark with " + opt);
                CVSE.config.profileRequestsBenchmark = opt;
            }
            while(System.currentTimeMillis()<CVSE.GTS.referenceTime){
                int sleeptime=(int)(CVSE.GTS.referenceTime-System.currentTimeMillis());
                System.out.println("sleeping for "+sleeptime+" before start");
                trysleep(sleeptime);
            }
            CVSE.RG.ReadProfileRequests(CVSE.config.profileRequestsBenchmark);
            //
            //sleep(3000);
            System.out.println("start sim");

            //CVSE.RG.contProfileRequestsGen(); //use Tick to call it

            while (!CVSE.RG.finished || !CVSE.GTS.emptyQueue()) {

                trysleep(300);
                CVSE.RG.contProfileRequestsGen();
                CVSE.GTS.taskScheduling();
                System.out.println("wait for sim to finish, RG="+CVSE.RG.finished+" queuelength="+CVSE.GTS.getBatchqueueLength());

                //CVSE.RG.contProfileRequestsGen(); //probably good idea to call here...
            }
            System.out.println("\nAll request have been released\n");
            int count=0;
            while (!CVSE.GTS.emptyQueue()) {
                System.out.println("wait for pending work to send");
                trysleep(300);
                if(count>3){
                    System.out.println("Freeze, should do something?");
                    //CVSE.GTS.
                }
                count++;
            }
            System.out.println("Batch queue emptied");
            //wait until get ack for all tasks
            while (CVSE.GTS.workcompleted<CVSE.GTS.worksubmitted) {
                System.out.println("wait for allTask tobe acked");
                trysleep(300);
            }
        } else {
            while (rqn != 0) {
                System.out.println("enter video request numbers to generate and their interval and how many times");
                rqn = scanner.nextInt();
                interval = scanner.nextInt();
                n = scanner.nextInt();
                //create a lot of request to test out
                CVSE.RG.nRandomRequest(rqn, interval, n);
            }
        }
        // Check point, enter any key to continue
        //System.out.println("enter any key to terminate the system");
        //scanner.next();
        //wind down process
        if(CVSE.RP ==null){
            System.out.println("RP is down");
        }else{
            if(CVSE.RP.DU==null){
                System.out.println("DU is down");
            }
        }
        CVSE.RP.DU.printstat();
        try {
            CVSE.RP.PurgeAllContainers();
        }catch(Exception e){
            System.out.println("close containers error");
        }
        //CVSE.RP.DU.graphplot();

        trysleep(2000); //if graphplot is not draw, don't turn it down too fast
        CVSE.GTS.close();
        CVSE.RP.closeAll();
        System.out.println("Done");
        System.exit(1); //make sure the program exit

        return "success";



    }

    //sandbox testing something strange, not really doing the program code
    public static String genbenchmarkTrace(int seed) {
        int[] sr;
        if (seed == 0) {
            //30
            //sr = new int[] {699,1911,16384,9999,555,687,9199,104857,212223,777,1920, 1080, 768, 1990, 4192, 262144, 800, 12345, 678, 521, 50, 167, 1, 251, 68, 6, 333, 1048575, 81, 7};
            //5
            sr = new int[] {699,1911,16384,9999,555};
            //1
            //sr = new int[]{699};
        } else {
            sr=new int[1];
            sr[0]=seed;
        }

        for (int j = 0; j < sr.length; j++) {
            for (int i = 1500; i <= 2700; i += 600) {
                //_CVSE.RG.generateProfiledRandomRequests("wcodec" + i + "r_180000_10000_3000_s" + sr[j], sr[j], 100, i, 180000, 10000, 3000);
                //use default avgslacktime value, 10000 for most operations, 8000 for codec
                CVSE.RG.generateProfiledRandomRequests("start0_" + i + "r_120000_10000_3000_s" + sr[j], sr[j], 100, i, 120000, 0, 3000);
            }
        }

        //CVSE.RP.DU.graphplot();

        return "done";
    }


}