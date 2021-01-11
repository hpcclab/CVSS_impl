package Repository;

import ResourceManagement.MachineInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class ServiceRepository {
    public ArrayList<ServiceFunction> ServiceList=new ArrayList<>();
    //add all known operations to a machine interface

    public void populateAllFNtoMI(MachineInterface mi){
        for(ServiceFunction eachop: ServiceList){
            mi.addAvailableService(eachop);
        }
    }
    public void BroadcastFunctionToAllMI(ServiceFunction fn,ArrayList<MachineInterface> machineInterfaces){
        for (int i=0; i<machineInterfaces.size();i++){
            machineInterfaces.get(i).addAvailableService(fn);
        }
    }
    // legacy function, need rework
    public void readlistedOperations() {
        File listfile = new File("profile/operations.txt");
        if(!listfile.exists()){
            System.out.println("\n\nWarning profile/operations.txt does not exist \n\n");
        }else{
            try {
                Scanner scanner=new Scanner(listfile);
                while(scanner.hasNext()){
                    String line[]=scanner.nextLine().split(",");
                    if(line.length==2){
                        addLegacyOperation(line[0],line[1]);
                    }else{
                        System.out.println("ill formed line?");
                    }
                }
                scanner.close();
            } catch (FileNotFoundException e) {
                System.out.println("videorepository can not find list.txt or read fail");
                //e.printStackTrace();
            }
        }
        //operations as file
    }
    //String name,String batchscript
    // so, codec, bitrate, framerate, changing are all using the same FFmpegContainer.
    // batch file data is no longer used
    public void addLegacyOperation(String name,String batchfile){
        ServiceFunction the_Fn=new ServiceFunction(name,"FFmpegContainer","persistent");
        ServiceList.add(the_Fn);
    }
}
