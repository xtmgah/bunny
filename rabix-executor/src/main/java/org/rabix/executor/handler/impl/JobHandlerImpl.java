package org.rabix.executor.handler.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.filemapper.FileMapper;
import org.rabix.bindings.filemapper.FileMappingException;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.requirement.DockerContainerRequirement;
import org.rabix.bindings.model.requirement.FileRequirement;
import org.rabix.bindings.model.requirement.FileRequirement.SingleFileRequirement;
import org.rabix.bindings.model.requirement.FileRequirement.SingleInputFileRequirement;
import org.rabix.bindings.model.requirement.FileRequirement.SingleTextFileRequirement;
import org.rabix.bindings.model.requirement.LocalContainerRequirement;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.common.helper.ChecksumHelper;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;
import org.rabix.executor.ExecutorException;
import org.rabix.executor.config.FileConfig;
import org.rabix.executor.config.StorageConfig;
import org.rabix.executor.config.StorageConfig.BackendStore;
import org.rabix.executor.container.ContainerException;
import org.rabix.executor.container.ContainerHandler;
import org.rabix.executor.container.ContainerHandlerFactory;
import org.rabix.executor.container.impl.CompletedContainerHandler;
import org.rabix.executor.handler.JobHandler;
import org.rabix.executor.model.JobData;
import org.rabix.executor.service.DownloadFileService;
import org.rabix.executor.service.JobDataService;
import org.rabix.ftp.SimpleFTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.assistedinject.Assisted;

public class JobHandlerImpl implements JobHandler {

  private static final String ERROR_LOG = "job.err.log";
  private final String KEY_CHECKSUM = "checksum";

  private static final Logger logger = LoggerFactory.getLogger(JobHandlerImpl.class);

  private final File workingDir;

  private final DownloadFileService downloadFileService;
  private final JobDataService jobDataService;

  private final SimpleFTPClient ftpClient;
  private final boolean enableHash;
  private final HashAlgorithm hashAlgorithm;

  private Job job;
  private Configuration configuration;
  private ContainerHandler containerHandler;

  @Inject
  public JobHandlerImpl(@Assisted Job job, JobDataService jobDataService, DownloadFileService downloadFileService, Configuration configuration, SimpleFTPClient ftpClient) {
    this.job = job;
    this.configuration = configuration;
    this.downloadFileService = downloadFileService;
    this.jobDataService = jobDataService;
    this.workingDir = StorageConfig.getWorkingDir(job, configuration);
    this.ftpClient = ftpClient;
    this.enableHash = FileConfig.calculateFileChecksum(configuration);
    this.hashAlgorithm = FileConfig.checksumAlgorithm(configuration);
  }

  @Override
  public void start() throws ExecutorException {
    logger.info("Start command line tool for id={}", job.getId());
    try {
      Bindings bindings = BindingsFactory.create(job);
      downloadFileService.download(job, bindings.getInputFiles(job));

      List<Requirement> combinedRequirements = new ArrayList<>();
      combinedRequirements.addAll(bindings.getHints(job));
      combinedRequirements.addAll(bindings.getRequirements(job));

      createFileRequirements(combinedRequirements);

      job = bindings.mapInputFilePaths(job, new InputFileMapper());
      job = bindings.preprocess(job, workingDir);

      if (bindings.canExecute(job)) {
        containerHandler = new CompletedContainerHandler();
      } else {
        Requirement containerRequirement = getRequirement(combinedRequirements, DockerContainerRequirement.class);
        if (containerRequirement == null || !StorageConfig.isDockerSupported(configuration)) {
          containerRequirement = new LocalContainerRequirement();
        }
        containerHandler = ContainerHandlerFactory.create(job, containerRequirement, configuration);
      }
      containerHandler.start();
    } catch (Exception e) {
      String message = String.format("Execution failed for %s. %s", job.getId(), e.getMessage());
      throw new ExecutorException(message, e);
    }
  }

  private void createFileRequirements(List<Requirement> requirements) throws ExecutorException, FileMappingException {
    try {
      FileRequirement fileRequirementResource = getRequirement(requirements, FileRequirement.class);
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
          String mappedPath = new InputFileMapper().map(path);
          File file = new File(mappedPath);
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
    } catch (IOException e) {
      logger.error("Failed to process file requirements.", e);
      throw new ExecutorException("Failed to process file requirements.");
    }
  }

  @SuppressWarnings("unchecked")
  private <T extends Requirement> T getRequirement(List<Requirement> requirements, Class<T> clazz) {
    for (Requirement requirement : requirements) {
      if (requirement.getClass().equals(clazz)) {
        return (T) requirement;
      }
    }
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Job postprocess(boolean isTerminal) throws ExecutorException {
    logger.debug("postprocess(id={})", job.getId());
    try {
      containerHandler.dumpContainerLogs(new File(workingDir, ERROR_LOG));

      if (!isSuccessful()) {
        upload(workingDir);
        return job;
      }

      Bindings bindings = BindingsFactory.create(job);
      job = bindings.postprocess(job, workingDir);

      if (enableHash) {
        Map<String, Object> outputs = job.getOutputs();
        Map<String, Object> outputsWithCheckSum = (Map<String, Object>) populateChecksum(outputs);
        job = Job.cloneWithOutputs(job, outputsWithCheckSum);
      }

      job = bindings.mapOutputFilePaths(job, new OutputFileMapper());
      upload(workingDir);

      JobData jobData = jobDataService.find(job.getId(), job.getContext().getId());
      jobData.setResult(job.getOutputs());
      jobDataService.save(jobData, job.getContext().getId());

      logger.debug("Command line tool {} returned result {}.", job.getId(), job.getOutputs());
      return job;
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
    for (File file : workingDir.listFiles()) {
      String remotePath = file.getAbsolutePath().substring(StorageConfig.getLocalExecutionDirectory(configuration).length());
      ftpClient.upload(file, remotePath);
    }
  }

  public void stop() throws ExecutorException {
    logger.debug("stop(id={})", job.getId());
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

  @SuppressWarnings("unchecked")
  private void calculateChecksum(Object file) {
    Map<String, Object> fileMap = (Map<String, Object>) file;
    File f = new File((String) fileMap.get("path"));
    String checksum = ChecksumHelper.checksum(f, hashAlgorithm);
    if (checksum != null) {
      fileMap.put(KEY_CHECKSUM, checksum);
    }
  }

  @SuppressWarnings("unchecked")
  public Object populateChecksum(Object outputs) {
    if (outputs instanceof Map) {
      String mapClass = (String) ((Map<String, Object>) outputs).get("class");
      if (mapClass != null && mapClass.equals("File")) {
        calculateChecksum(outputs);
        return outputs;
      } else {
        Map<String, Object> outputsMap = (Map<String, Object>) outputs;
        Map<String, Object> result = new HashMap<String, Object>();
        for (String output : outputsMap.keySet()) {
          Object value = outputsMap.get(output);
          result.put(output, populateChecksum(value));
        }
        return result;
      }
    } else if (outputs instanceof List) {
      List<Object> iter = (List<Object>) outputs;
      List<Object> resultList = new ArrayList<Object>();
      for (Object elem : iter) {
        resultList.add(populateChecksum(elem));
      }
      return resultList;
    } else {
      return outputs;
    }
  }

  @Override
  public boolean isSuccessful(int processExitCode) throws ExecutorException {
    try {
      Bindings bindings = BindingsFactory.create(job);
      return bindings.isSuccessful(job, processExitCode);
    } catch (BindingException e) {
      logger.error("Failed to create Bindings", e);
      throw new ExecutorException(e);
    }
  }

  private class InputFileMapper implements FileMapper {

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

  }

  private class OutputFileMapper implements FileMapper {

    @Override
    public String map(String filePath) throws FileMappingException {
      logger.info("Map absolute physical path {} to relative physical path.", filePath);
      return filePath.substring(StorageConfig.getLocalExecutionDirectory(configuration).length() + 1);
    }

  }

}
