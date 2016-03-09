package org.rabix.executor.handler.commandline;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.filemapper.FileMapper;
import org.rabix.bindings.filemapper.FileMappingException;
import org.rabix.bindings.model.Executable;
import org.rabix.bindings.model.requirement.FileRequirement;
import org.rabix.bindings.model.requirement.FileRequirement.SingleFileRequirement;
import org.rabix.bindings.model.requirement.FileRequirement.SingleInputFileRequirement;
import org.rabix.bindings.model.requirement.FileRequirement.SingleTextFileRequirement;
import org.rabix.bindings.model.requirement.LocalContainerRequirement;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.executor.ExecutorException;
import org.rabix.executor.config.FileConfig;
import org.rabix.executor.config.StorageConfig;
import org.rabix.executor.config.StorageConfig.BackendStore;
import org.rabix.executor.container.ContainerException;
import org.rabix.executor.container.ContainerHandler;
import org.rabix.executor.container.ContainerHandlerFactory;
import org.rabix.executor.handler.ExecutableHandler;
import org.rabix.executor.model.ExecutableData;
import org.rabix.executor.service.DownloadFileService;
import org.rabix.executor.service.ExecutableDataService;
import org.rabix.ftp.SimpleFTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.assistedinject.Assisted;

public class CommandLineExecutableHandler implements ExecutableHandler {

  private static final String ERROR_LOG = "job.err.log";
  
  private static final Logger logger = LoggerFactory.getLogger(CommandLineExecutableHandler.class);

  private final File workingDir;
  
  private final DownloadFileService downloadFileService;
  private final ExecutableDataService executableDataService;

  private final SimpleFTPClient ftpClient;
  
  private Executable executable;
  private Configuration configuration;
  private ContainerHandler containerHandler;

  @Inject
  public CommandLineExecutableHandler(@Assisted Executable executable, ExecutableDataService executableDataService, DownloadFileService downloadFileService, FileConfig fileConfig, Configuration configuration, SimpleFTPClient ftpClient) {
    this.executable = executable;
    this.configuration = configuration;
    this.downloadFileService = downloadFileService;
    this.executableDataService = executableDataService;
    this.workingDir = StorageConfig.getWorkingDir(executable, configuration);
    this.ftpClient = ftpClient;
  }

  @Override
  public void start() throws ExecutorException {
    logger.info("Start command line tool for id={}", executable.getId());
    try {
      Bindings bindings = BindingsFactory.create(executable);
      downloadFileService.download(executable, bindings.getInputFiles(executable));
      createFileRequirements(bindings);
      
      executable = bindings.mapInputFilePaths(executable, new FileMapper() {
        @Override
        public String map(String filePath) throws FileMappingException {
          BackendStore backendStore = StorageConfig.getBackendStore(configuration);
          switch (backendStore) {
            case FTP:
              logger.info("Map FTP path {} to physical path.", filePath);
              return new File(new File(StorageConfig.getLocalExecutionDirectory(configuration)), filePath).getAbsolutePath();
            case LOCAL:
              if (!filePath.startsWith(File.separator)) {
                return new File(new File(StorageConfig.getLocalExecutionDirectory(configuration)), filePath).getAbsolutePath();
              }
              return filePath;
            default:
              throw new FileMappingException("BackendStore " + backendStore + " is not supported.");
          }
          
        }
      });
      executable = bindings.preprocess(executable, workingDir);
      
      Requirement containerRequirement = bindings.getDockerRequirement(executable);
      if (containerRequirement == null || !StorageConfig.isDockerSupported(configuration)) {
        containerRequirement = new LocalContainerRequirement();
      }
      containerHandler = ContainerHandlerFactory.create(executable, containerRequirement, configuration);
      containerHandler.start();
    } catch (Exception e) {
      String message = String.format("Execution failed for %s. %s", executable.getId(), e.getMessage());
      throw new ExecutorException(message, e);
    }
  }

  private void createFileRequirements(Bindings bindings) throws ExecutorException {
    try {
      FileRequirement fileRequirementResource = bindings.getFileRequirement(executable);
      if (fileRequirementResource == null) {
        return;
      }

      List<SingleFileRequirement> fileRequirements = fileRequirementResource.getFileRequirements();
      if (fileRequirements == null) {
        return;
      }
      for (SingleFileRequirement fileRequirement : fileRequirements) {
        logger.info("Process file requirement {}", fileRequirement);

        File destinationFile = new File(workingDir, fileRequirement.getFilename());
        if (fileRequirement instanceof SingleTextFileRequirement) {
          FileUtils.writeStringToFile(destinationFile, ((SingleTextFileRequirement) fileRequirement).getContent());
          continue;
        }
        if (fileRequirement instanceof SingleInputFileRequirement) {
          String path = ((SingleInputFileRequirement) fileRequirement).getContent().getPath();
          File file = new File(path);
          if (!file.exists()) {
            continue;
          }
          if (file.isFile()) {
            FileUtils.copyFile(file, destinationFile);
          } else {
            FileUtils.copyDirectory(file, destinationFile);
          }
        }
      }
    } catch (IOException | BindingException e) {
      logger.error("Failed to process file requirements.", e);
      throw new ExecutorException("Failed to process file requirements.");
    }
  }

  @Override
  public Executable postprocess(boolean isTerminal) throws ExecutorException {
    logger.debug("postprocess(id={})", executable.getId());
    try {
      containerHandler.dumpContainerLogs(new File(workingDir, ERROR_LOG));

      if (!isSuccessful()) {
        upload(workingDir);
        return executable;
      }
      
      Bindings bindings = BindingsFactory.create(executable);
      executable = bindings.populateOutputs(executable, workingDir);
      executable = bindings.mapOutputFilePaths(executable, new FileMapper() {
        @Override
        public String map(String filePath) throws FileMappingException {
          logger.info("Map absolute physical path {} to relative physical path.", filePath);
          return filePath.substring(StorageConfig.getLocalExecutionDirectory(configuration).length() + 1);
        }
      });
      upload(workingDir);
      
      ExecutableData executableData = executableDataService.find(executable.getId(), executable.getContext().getId());
      executableData.setResult(executable.getOutputs());
      executableDataService.save(executableData, executable.getContext().getId());
      
      logger.debug("Command line tool {} returned result {}.", executable.getId(), executable.getOutputs());
      return executable;
    } catch (ContainerException e) {
      logger.error("Failed to query container.", e);
      throw new ExecutorException("Failed to query container.", e);
    } catch (BindingException e) {
      logger.error("Could not collect outputs.", e);
      throw new ExecutorException("Could not collect outputs.", e);
    } catch (IOException e) {
      logger.error("Could not upload outputs.", e);
      throw new ExecutorException("Could not upload outputs.", e);
    }
  }
  
  private void upload(File workingDir) throws IOException {
    if (!StorageConfig.getBackendStore(configuration).equals(BackendStore.FTP)) {
      return;
    }
    for(File file : workingDir.listFiles()) {
      String remotePath = file.getAbsolutePath().substring(StorageConfig.getLocalExecutionDirectory(configuration).length());
      ftpClient.upload(file, remotePath);
    }
  }

  public void stop() throws ExecutorException {
    logger.debug("stop(id={})", executable.getId());
    if (containerHandler == null) {
      logger.debug("Container hasn't started yet.");
      return;
    }
    try {
      containerHandler.stop();
    } catch (ContainerException e) {
      logger.error("Failed to stop execution", e);
      throw new ExecutorException("Failed to stop execution.", e);
    }
  }

  public boolean isStarted() throws ExecutorException {
    logger.debug("isStarted()");
    if (containerHandler == null) {
      logger.debug("Container hasn't started yet.");
      return false;
    }
    try {
      return containerHandler.isStarted();
    } catch (ContainerException e) {
      logger.error("Failed to query container for status", e);
      throw new ExecutorException("Failed to query container for status.", e);
    }
  }

  public boolean isRunning() throws ExecutorException {
    logger.debug("isRunning()");
    if (containerHandler == null) {
      logger.debug("Container hasn't started yet.");
      return false;
    }
    try {
      return containerHandler.isRunning();
    } catch (ContainerException e) {
      logger.error("Couldn't check if container is running or not", e);
      throw new ExecutorException("Couldn't check if container is running or not.", e);
    }
  }

  @Override
  public int getExitStatus() throws ExecutorException {
    logger.debug("getExitStatus()");
    try {
      return containerHandler.getProcessExitStatus();
    } catch (ContainerException e) {
      logger.error("Couldn't get process exit value", e);
      throw new ExecutorException("Couldn't get process exit value.", e);
    }
  }
  
  @Override
  public boolean isSuccessful() throws ExecutorException {
    logger.debug("isSuccessful()");
    int processExitStatus = getExitStatus();
    return isSuccessful(processExitStatus);
  }

  @Override
  public boolean isSuccessful(int processExitCode) throws ExecutorException {
    try {
      Bindings bindings = BindingsFactory.create(executable);
      return bindings.isSuccessfull(executable, processExitCode);
    } catch (BindingException e) {
      logger.error("Failed to create Bindings", e);
      throw new ExecutorException(e);
    }
  }

}
