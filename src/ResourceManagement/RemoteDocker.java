package ResourceManagement;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemoteDocker {


    private DockerClient docker = null;
    private String type,name;
    public List<Container> containers = null;
    boolean allowCold=false;
    public RemoteDocker(String ttype,String nname,String IP,String certPath,Boolean _allowCold){
        //System.out.println("IP="+IP+" certPath="+certPath);
        type=ttype;
        name=nname;
        docker=CreateDockerClient(IP,certPath);
        allowCold=_allowCold;
    }
//// to start docker manually which allow access: dockerd --tlsverify --tlscacert=ca.pem --tlscert=server-cert.pem --tlskey=server-key.pem \
//  -H=0.0.0.0:2376 -H unix:///var/run/docker.sock &

    private DockerClient CreateDockerClient(String IP,String certPath){
        //return new DefaultDockerClient("unix:///var/run/docker.sock"); //default local access
        //return new DefaultDockerClient("tcp://10.131.80.30:2375"); //TEST connection without cert.
        try{
        final DockerClient docker = DefaultDockerClient.builder() //remote with cert
                //.uri(URI.create("https://10.131.35.31:2376")) //VM1
                //.dockerCertificates(new DockerCertificates(Paths.get("/share_dir/cert/VM1")))

                .uri(URI.create("https://"+IP)) //VM01
                .dockerCertificates(new DockerCertificates(Paths.get(certPath)))
                .build();
        return docker;
        }catch (Exception e){
            System.out.println("Failed to create Docker Client: "+e);
            return null;
        }
        //return DefaultDockerClient.builder().uri(URI.create("https://localhost:80")).build();
    }

    public String CreateContainers(String[] ports,int nodeID,String imageName,String CMD)  { //
        String createdID="";
        if(docker == null) {
            System.out.println("docker Client wasn't created");
            return "Error docker==null";
        }

        String IP;

        try {
            //say it out the ip needed (legacy)
//            BufferedWriter writer = new BufferedWriter(new FileWriter("/mnt/container/portid"));
//            writer.write(givenPort);
//            writer.close();

            containers = docker.listContainers(DockerClient.ListContainersParam.allContainers());

            //final List<Image> images = docker.listImages();

            //support array, because it may need multiple ports
            System.out.println("givenport="+ports[0]);
            final Map<String, List<PortBinding>> portBindings = new HashMap<String, List<PortBinding>>();
            for ( String port : ports ) {
                List<PortBinding> hostPorts = new ArrayList<PortBinding>();
                hostPorts.add( PortBinding.of( "", port ) ); //host to container port
                portBindings.put( port, hostPorts ); //+ "/tcp" is optional?
            }

            final HostConfig hostConfig = HostConfig.builder()
                    .binds("/share_dir/SVSE:/share_dir/SVSE") //temporary
                    .portBindings(portBindings)
                    .build();

            final String[] command = {CMD,nodeID+""};
            final ContainerConfig containerConfig = ContainerConfig.builder()
                    .image(imageName)
                    .attachStderr(Boolean.TRUE)
                    .attachStdin(Boolean.TRUE)
                    .tty(Boolean.TRUE)
                    .cpuQuota(1l) // so that performance scaling is consistence
                    .hostConfig(hostConfig)
                    //.exposedPorts( givenPort) //container to host + "/tcp"
                    .cmd(command)
                    .build();

                final ContainerCreation containerCreation = docker.createContainer(containerConfig);
                docker.startContainer(containerCreation.id());
                //createdIP+=docker.inspectContainer(containerCreation.id()).networkSettings().ipAddress()+",";
                createdID=containerCreation.id();
                //createdIP="0.0.0.0"; //TESTTTT, not using the returned IP
                //byte[] ByteCode=givenPort.getBytes();
                //Files.write("/mnt/container/portid",ByteCode);



        }catch(Exception e){
            System.out.println("Docker fail: "+e);
        }
        return createdID;
    }

    //stop all containers?
    public void KillAllContainers() throws DockerException, InterruptedException {
        if(docker == null)
            return;

        containers = docker.listContainers(DockerClient.ListContainersParam.allContainers());

        for (int i=0;i<containers.size();i++){
            docker.killContainer(containers.get(i).id());
        }
    }

    //permanently remove all containers
    public void RemoveAllContainers() throws DockerException, InterruptedException {
        if(docker == null)
            return;

        containers = docker.listContainers(DockerClient.ListContainersParam.allContainers());

        for (int i=0;i<containers.size();i++){
            docker.stopContainer(containers.get(i).id(), 0);
            docker.removeContainer(containers.get(i).id());
        }
    }

    public void ExtractFiles(){

    }

    public void GetCurrentContainers() throws DockerException, InterruptedException {
        if(docker == null)
            return;

        containers = docker.listContainers(DockerClient.ListContainersParam.allContainers());
    }
    public void waitContainersStop(String whichone) throws DockerException, InterruptedException {
        docker.waitContainer(whichone);
    }
    public void removeCont(String whichone) throws DockerException, InterruptedException {
        docker.removeContainer(whichone);
    }
}

