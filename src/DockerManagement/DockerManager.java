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

    public static void CreateDockerClient() throws DockerCertificateException, DockerException, InterruptedException {

        String imageName = "testimage";

       final DockerClient docker = new DefaultDockerClient("unix:///var/run/docker.sock");

/*
        final DockerClient docker = DefaultDockerClient.builder()
                .uri(URI.create("https://boot2docker:2376"))
                .dockerCertificates(new DockerCertificates(Paths.get("/home/pi/.docker/config.json")))
                .build();
*/
      //  final List<Container> containers = docker.listContainers();

        final List<Container> containers = docker.listContainers(DockerClient.ListContainersParam.allContainers());

        final List<Image> images = docker.listImages();

        final ContainerConfig config = ContainerConfig.builder()
                .image(imageName)
                .build();

        final String name = "random_container_name2";

       // final ContainerCreation creation = docker.createContainer(config, name);

        //final String id = creation.id();

        final String[] command = {"sh", "-c", "test.sh"};
        final ContainerConfig containerConfig = ContainerConfig.builder()
                .image(imageName)
                .cmd(command)
                // Probably other configuration
                .build();
        final ContainerCreation containerCreation = docker.createContainer(containerConfig);
        docker.startContainer(containerCreation.id());

        /*

        docker.pull(imageName);

        /*

        final String[] ports = {"80", "22"};
        final Map<String, List<PortBinding>> portBindings = new HashMap<>();
        for (String port : ports) {
            List<PortBinding> hostPorts = new ArrayList<>();
            hostPorts.add(PortBinding.of("0.0.0.0", port));
            portBindings.put(port, hostPorts);
        }

// Bind container port 443 to an automatically allocated available host port.
        List<PortBinding> randomPort = new ArrayList<>();
        randomPort.add(PortBinding.randomPort("0.0.0.0"));
        portBindings.put("443", randomPort);

        final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

// Create container with exposed ports
        final ContainerConfig containerConfig = ContainerConfig.builder()
                .hostConfig(hostConfig)
                .image(imageName).exposedPorts(ports)
                .cmd("sh", "-c", "while :; do sleep 1; done")
                .build();

        final ContainerCreation creation = docker.createContainer(containerConfig);
        final String id = creation.id();

// Inspect container
        final ContainerInfo info = docker.inspectContainer(id);

// Start container
        docker.startContainer(id);

// Exec command inside running container with attached STDOUT and STDERR
        final String[] command = {"sh", "-c", "ls"};
        final ExecCreation execCreation = docker.execCreate(
                id, command, DockerClient.ExecCreateParam.attachStdout(),
                DockerClient.ExecCreateParam.attachStderr());
        final LogStream output = docker.execStart(execCreation.id());
        final String execOutput = output.readFully();

// Kill container
        docker.killContainer(id);

// Remove container
        docker.removeContainer(id);

// Close the docker client
        docker.close();

//*/

    }
}
