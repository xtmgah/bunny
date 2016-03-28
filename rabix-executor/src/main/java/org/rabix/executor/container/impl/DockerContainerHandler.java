package org.rabix.executor.container.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.requirement.DockerContainerRequirement;
import org.rabix.bindings.model.requirement.EnvironmentVariableRequirement;
import org.rabix.executor.config.StorageConfig;
import org.rabix.executor.container.ContainerException;
import org.rabix.executor.container.ContainerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.LogsParam;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.LogMessage;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ContainerState;
import com.spotify.docker.client.messages.HostConfig;

/**
 * Docker based implementation of {@link ContainerHandler} 
 */
public class DockerContainerHandler implements ContainerHandler {

  private static final Logger logger = LoggerFactory.getLogger(DockerContainerHandler.class);

  public static final String DIRECTORY_MAP_MODE = "rw";
  public static final String COMMAND_FILE = "cmd.log";

  private String containerId;
  private DockerClient dockerClient;

  private final Job job;
  private final DockerContainerRequirement dockerResource;

  private final File workingDir;
  private final Configuration configuration;

  public DockerContainerHandler(Job job, DockerContainerRequirement dockerResource, Configuration configuration) {
    this.job = job;
    this.dockerResource = dockerResource;
    this.configuration = configuration;
    this.workingDir = StorageConfig.getWorkingDir(job, configuration);
    this.dockerClient = createDockerClient(configuration);
  }
  
  private DockerClient createDockerClient(Configuration configuration) {
    String dockerURI = StorageConfig.getDockerURI(configuration);
    
    return DefaultDockerClient.builder()
        .uri(URI.create(dockerURI.replaceAll("^unix:///", "unix://localhost/")))
        .connectTimeoutMillis(TimeUnit.MINUTES.toMillis(1))
        .readTimeoutMillis(TimeUnit.MINUTES.toMillis(1))
        .build();
  }

  @Override
  public void start() throws ContainerException {
    String dockerPull = dockerResource.getDockerPull();

    try {
      try {
        dockerClient.pull(dockerPull);
      } catch (Exception e) {
        logger.error("Failed to pull " + dockerPull, e);
        throw new ContainerException("Failed to pull " + dockerPull, e);
      }

      Set<String> volumes = new HashSet<>();
      String physicalPath = StorageConfig.getLocalExecutionDirectory(configuration);
      volumes.add(physicalPath);

      ContainerConfig.Builder builder = ContainerConfig.builder();
      builder.image(dockerPull);
      
      HostConfig.Builder hostConfigBuilder = HostConfig.builder();
      hostConfigBuilder.binds(physicalPath + ":" + physicalPath + ":" + DIRECTORY_MAP_MODE);
      HostConfig hostConfig = hostConfigBuilder.build();
      builder.hostConfig(hostConfig);

      Bindings bindings = BindingsFactory.create(job);
      String commandLine = bindings.buildCommandLine(job);

      File commandLineFile = new File(workingDir, COMMAND_FILE);
      FileUtils.writeStringToFile(commandLineFile, commandLine);
      builder.workingDir(workingDir.getAbsolutePath()).volumes(volumes).cmd("sh", "-c", commandLine);

      EnvironmentVariableRequirement environmentVariableResource = bindings.getEnvironmentVariableRequirement(job);
      if (environmentVariableResource != null) {
        builder.env(environmentVariableResource.getVariables());
      }
      ContainerCreation creation = null;
      try {
        creation = dockerClient.createContainer(builder.build());
      } catch (DockerException | InterruptedException e) {
        logger.error("Failed to create Docker container.", e);
        throw new ContainerException("Failed to create Docker container.");
      }
      containerId = creation.id();
      try {
        dockerClient.startContainer(containerId);
      } catch (DockerException | InterruptedException e) {
        logger.error("Failed to start Docker container " + containerId, e);
        throw new ContainerException("Failed to start Docker container " + containerId);
      }
      logger.info("Docker container {} has started.", containerId);
    } catch (IOException e) {
      logger.error("Failed to create cmd.log file.", e);
      throw new ContainerException("Failed to create cmd.log file.");
    } catch (BindingException e) {
      logger.error("Failed to start container.", e);
      throw new ContainerException("Failed to start container.", e);
    }
  }

  @Override
  public void stop() throws ContainerException {
    try {
      dockerClient.stopContainer(containerId, 0);
    } catch (Exception e) {
      logger.error("Docker container " + containerId + " failed to stop", e);
      throw new ContainerException("Docker container " + containerId + " failed to stop");
    }
  }

  @JsonIgnore
  public boolean isStarted() throws ContainerException {
    ContainerInfo containerInfo;
    try {
      containerInfo = dockerClient.inspectContainer(containerId);
      ContainerState containerState = containerInfo.state();
      Date startedDate = containerState.startedAt();
      return startedDate != null;
    } catch (Exception e) {
      logger.error("Failed to query docker. Container ID: " + containerId, e);
      throw new ContainerException("Failed to query docker. Container ID: " + containerId);
    }
  }

  @Override
  @JsonIgnore
  public boolean isRunning() throws ContainerException {
    ContainerInfo containerInfo;
    try {
      containerInfo = dockerClient.inspectContainer(containerId);
      ContainerState containerState = containerInfo.state();
      return containerState.running();
    } catch (Exception e) {
      logger.error("Failed to query docker. Container ID: " + containerId, e);
      throw new ContainerException("Failed to query docker. Container ID: " + containerId);
    }
  }

  @Override
  @JsonIgnore
  public int getProcessExitStatus() throws ContainerException {
    ContainerInfo containerInfo;
    try {
      containerInfo = dockerClient.inspectContainer(containerId);
      ContainerState containerState = containerInfo.state();
      return containerState.exitCode();
    } catch (Exception e) {
      logger.error("Failed to query docker. Container ID: " + containerId, e);
      throw new ContainerException("Failed to query docker. Container ID: " + containerId);
    }
  }

  /**
   * Does after processing (dumps standard error log for now)
   */
  @Override
  public void dumpContainerLogs(final File logFile) throws ContainerException {
    logger.debug("Saving standard error files for id={}", job.getId());

    if (logFile != null) {
      try {
        dumpLog(containerId, logFile);
      } catch (Exception e) {
        logger.error("Docker container " + containerId + " failed to create log file", e);
        throw new ContainerException("Docker container " + containerId + " failed to create log file");
      }
    }
  }
  
  /**
   * Helper method for dumping error logs from Docker to file 
   */
  public void dumpLog(String containerId, File logFile) throws DockerException, InterruptedException {
    LogStream errorStream = null;
    
    FileChannel fileChannel = null;
    FileOutputStream fileOutputStream = null;
    try {
      if (logFile.exists()) {
        logFile.delete();
      }
      logFile.createNewFile();
      
      fileOutputStream = new FileOutputStream(logFile);
      fileChannel = fileOutputStream.getChannel();

      errorStream = dockerClient.logs(containerId, LogsParam.stderr());
      while (errorStream.hasNext()) {
        LogMessage message = errorStream.next();
        ByteBuffer buffer = message.content();
        fileChannel.write(buffer);
      }
    } catch (FileNotFoundException e) {
      throw new DockerException("File " + logFile + " not found");
    } catch (IOException e) {
      throw new DockerException(e);
    } finally {
      if (errorStream != null) {
        errorStream.close();
      }
      if (fileChannel != null) {
        try {
          fileChannel.close();
        } catch (IOException e) {
          logger.error("Failed to close file channel", e);
        }
      }
      if (fileOutputStream != null) {
        try {
          fileOutputStream.close();
        } catch (IOException e) {
          logger.error("Failed to close file output stream", e);
        }
      }
    }
  }

}
