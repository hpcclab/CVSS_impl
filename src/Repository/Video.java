package Repository;

import Repository.RepositoryGOP;
import Scheduler.ServerConfig;
import com.amazonaws.services.opsworkscm.model.Server;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by pi on 5/21/17.
 */
public class Video {

    public ArrayList<RepositoryGOP> repositoryGOPs= new ArrayList<RepositoryGOP>();;

    private int totalSegments = 0;
    public String name;

    public Video(){
    }
    public Video(String path){
        if(ServerConfig.videoRepository_mode.equalsIgnoreCase("list.txt")){
            //list mode
            try {
                Scanner scanner=new Scanner(new File(path+"list.txt"));
                String[] sp=scanner.nextLine().split(" ");
                if(sp[0].equalsIgnoreCase("g")){ //auto generate mode
                    //System.out.println("g mode");
                    int n=Integer.parseInt(sp[1]);
                    for(int i=0;i<n;i++){
                        String GOPname=i+".ts";
                        RepositoryGOP repositoryGop = new RepositoryGOP(path+GOPname);
                        this.addGOP(repositoryGop);
                    }
                }else if(sp[0].equalsIgnoreCase("l")){ //list mode
                    while(scanner.hasNext()){ // didn't test
                        String GOPname=scanner.nextLine();
                        RepositoryGOP repositoryGop = new RepositoryGOP(path+GOPname);
                        this.addGOP(repositoryGop);
                    }
                }else{
                    System.out.println("uncrecognize a mode in list.txt");
                }
            } catch (FileNotFoundException e) {
                System.out.println("videorepository can not find list.txt or read fail");
                //e.printStackTrace();
            }


        }else{
            //scan mode
            File[]  files= new File(path).listFiles();
            if (files != null) {
                //System.out.println(files.length);
                for (int i = 0; i < files.length; i++) {
                    //System.out.println(i+" "+files[i].getName() +" "+files[i].getPath()); //DEBUG
                    if (!files[i].isDirectory()) {
                        String fileName = files[i].getName();
                        //check if extension is not m3u8
                        String extension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
                        if (!extension.equalsIgnoreCase("m3u8") && extension.equalsIgnoreCase("txt")) {
                            RepositoryGOP repositoryGop = new RepositoryGOP(files[i].getPath());
                            this.addGOP(repositoryGop);
                        }
                    }
                }
            }
        }
    }
    public int getTotalSegments()
    {
        return totalSegments;
    }

    public void setTotalSegments(int totalSegments)
    {
        if(totalSegments >= 0)
        {
            this.totalSegments = totalSegments;
        }
        else
        {
            this.totalSegments = -1;
        }

    }

    public void addGOP(RepositoryGOP repositoryGop){
        repositoryGOPs.add(repositoryGop);
        totalSegments++;
    }


}