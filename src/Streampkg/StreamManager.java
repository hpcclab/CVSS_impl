package Streampkg;

import Scheduler.AdmissionControl;
import Scheduler.GOPTaskScheduler;
import Scheduler.ServerConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static Singletons.GTSSingleton.GTS;
import static Singletons.VRSingleton.VR;

/**
 * Created by vaughanv21 on 2/1/2019.
 */
public class StreamManager {

    public void InitializeStream(int num){

    }

    public void RemoveProcessedStreams() throws IOException {
        File[] directories = new File("streams").listFiles(File::isDirectory);

        for (int i=0;i<directories.length;i++){
            removeFolder(directories[i].getPath());
        }
    }

    private void removeFolder(String name) throws IOException {
        Path directory = Paths.get(name);
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                Files.delete(file); // this will work because it's always a File
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir); //this will work because Files in the directory are already deleted
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public boolean CreateDirectory(Settings userRequest){
        File dir = new File(userRequest.outputDir());
        if (dir.exists()){
            return false;
        }

        String absPath = "/home/pi/Documents/VHPCC/workspace/CVSS_impl";

        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));

        String[] command = {"bash", ServerConfig.path + "bash/createDir.sh", userRequest.outputDir(),ServerConfig.path, userRequest.videoname};

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT); //debug,make output from bash to screen
            pb.redirectError(ProcessBuilder.Redirect.INHERIT); //debug,make output from bash to screen

            pb.start();
            Process p = pb.start();
            p.waitFor();
        } catch (Exception e){

        }
        return true;
    }

    public void InitializeStream(int videoIndex, Settings userRequest,  GOPTaskScheduler GTS){
        System.out.println("before stream");
        // create Stream from Video, there are 3 constructor for Stream, two for making from only certain segment (not all)

        //Settings newRequest = new Settings(userRequest.videoname, userRequest.resHeight, userRequest.resWidth);

        Stream ST=new Stream(VR.videos.get(videoIndex),userRequest); //admission control can work in constructor, or later?

        CreateDirectory(userRequest);

        //Admission Control assign Priority of each segments
        AdmissionControl.AssignStreamPriority(ST);
        for(StreamGOP x:ST.streamGOPs){
            System.out.println(x.getPriority());
        }
        //Scheduler
        System.out.println("test1");
        GTS.addStream(ST);
        System.out.println("test2");
        System.out.println("after stream");
    }



}
