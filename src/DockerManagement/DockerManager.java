package DockerManagement;

import com.spotify.docker.client.*;
import com.spotify.docker.client.messages.*;

import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pi on 3/15/19.
 */
public class DockerManager {

    public static String imageName = "testimage";

    private static DockerClient docker = null;

    public static List<Container> containers = null;

    private static DockerClient CreateDockerClient(){
        return new DefaultDockerClient("unix:///var/run/docker.sock");
        //return new DefaultDockerClient("tcp://localhost:2375");
        //return DefaultDockerClient.builder().uri(URI.create("https://localhost:80")).build();
    }


    public static String CreateContainers(int instanceNum)  {
        String createdIP="";
        if(docker == null)
            docker = CreateDockerClient();

        String IP;
/*
        final DockerClient docker = DefaultDockerClient.builder()
                .uri(URI.create("https://boot2docker:2376"))
                .dockerCertificates(new DockerCertificates(Paths.get("/home/pi/.docker/config.json")))
                .build();
*/
        //  final List<Container> containers = docker.listContainers();
        try {
            containers = docker.listContainers(DockerClient.ListContainersParam.allContainers());

            final List<Image> images = docker.listImages();

            // final ContainerCreation creation = docker.createContainer(config, name);

            //final String id = creation.id();

            final String[] ports = {"5601"};

            final Map<String, List<PortBinding>> portBindings = new HashMap<String, List<PortBinding>>();
            for ( String port : ports ) {
                List<PortBinding> hostPorts = new ArrayList<PortBinding>();
                hostPorts.add( PortBinding.of( "", 9000 ) );
                portBindings.put( port + "/tcp", hostPorts );
            }

            final HostConfig hostConfig = HostConfig.builder()
                    .binds("/mnt/container:/home/shared")
                    .portBindings(portBindings)
                    .build();
            //         HostConfig.builder().binds("/var/www/html/2019WebDemo:~/").build();

            final String[] command = {"/bin/bash"};
            final ContainerConfig containerConfig = ContainerConfig.builder()
                    .image(imageName)
                    .attachStderr(Boolean.TRUE)
                    .attachStdin(Boolean.TRUE)
                    .tty(Boolean.TRUE)
                    .hostConfig(hostConfig)
                    .exposedPorts( "5601/tcp" )
                    .cmd(command)
                    .build();

            for (int i=0;i<instanceNum;i++){
                final ContainerCreation containerCreation = docker.createContainer(containerConfig);
                docker.startContainer(containerCreation.id());
                createdIP+=docker.inspectContainer(containers.get(i).id()).networkSettings().ipAddress()+",";
            }

            containers = docker.listContainers(DockerClient.ListContainersParam.allContainers());

            for(int i=0;i<containers.size();i++){

                System.out.println(" id: " + containers.get(i).id()+ " ports: " +  containers.get(i).portsAsString() + " IP: "+docker.inspectContainer(containers.get(i).id()).networkSettings().ipAddress());
            }
        }catch(Exception e){
            System.out.print("Docker fail");
        }
        return createdIP;
    }

    //stop all containers?
    public static void KillAllContainers() throws DockerException, InterruptedException {
        if(docker == null)
            docker = CreateDockerClient();

        containers = docker.listContainers(DockerClient.ListContainersParam.allContainers());

        for (int i=0;i<containers.size();i++){
            docker.killContainer(containers.get(i).id());
        }
    }

    //permanently remove all containers
    public static void RemoveAllContainers() throws DockerException, InterruptedException {
        if(docker == null)
            docker = CreateDockerClient();

        containers = docker.listContainers(DockerClient.ListContainersParam.allContainers());

        for (int i=0;i<containers.size();i++){
            docker.stopContainer(containers.get(i).id(), 0);
            docker.removeContainer(containers.get(i).id());
        }
    }

    public static void ExtractFiles(){

    }

    public static void GetCurrentContainers() throws DockerException, InterruptedException {
        if(docker == null)
            docker = CreateDockerClient();

        containers = docker.listContainers(DockerClient.ListContainersParam.allContainers());
    }

}

