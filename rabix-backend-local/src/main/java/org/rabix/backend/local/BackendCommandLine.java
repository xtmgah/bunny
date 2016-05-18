package org.rabix.backend.local;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.ProtocolType;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.bindings.protocol.draft2.Draft2CommandLineBuilder;
import org.rabix.bindings.protocol.draft2.bean.Draft2CommandLineTool;
import org.rabix.bindings.protocol.draft2.bean.Draft2Job;
import org.rabix.bindings.protocol.draft2.bean.Draft2JobApp;
import org.rabix.bindings.protocol.draft2.bean.Draft2Resources;
import org.rabix.bindings.protocol.draft2.bean.resource.requirement.Draft2CreateFileRequirement;
import org.rabix.bindings.protocol.draft2.bean.resource.requirement.Draft2CreateFileRequirement.Draft2FileRequirement;
import org.rabix.bindings.protocol.draft2.expression.Draft2ExpressionException;
import org.rabix.bindings.protocol.draft2.resolver.Draft2DocumentResolver;
import org.rabix.bindings.protocol.draft3.Draft3CommandLineBuilder;
import org.rabix.bindings.protocol.draft3.bean.Draft3CommandLineTool;
import org.rabix.bindings.protocol.draft3.bean.Draft3Job;
import org.rabix.bindings.protocol.draft3.bean.Draft3JobApp;
import org.rabix.bindings.protocol.draft3.bean.Draft3Resources;
import org.rabix.bindings.protocol.draft3.bean.resource.requirement.Draft3CreateFileRequirement;
import org.rabix.bindings.protocol.draft3.bean.resource.requirement.Draft3CreateFileRequirement.Draft3FileRequirement;
import org.rabix.bindings.protocol.draft3.expression.Draft3ExpressionException;
import org.rabix.bindings.protocol.draft3.resolver.Draft3DocumentResolver;
import org.rabix.common.config.ConfigModule;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;
import org.rabix.engine.EngineModule;
import org.rabix.engine.rest.api.BackendHTTPService;
import org.rabix.engine.rest.api.JobHTTPService;
import org.rabix.engine.rest.api.impl.BackendHTTPServiceImpl;
import org.rabix.engine.rest.api.impl.JobHTTPServiceImpl;
import org.rabix.engine.rest.backend.BackendDispatcher;
import org.rabix.engine.rest.db.BackendDB;
import org.rabix.engine.rest.db.JobDB;
import org.rabix.engine.rest.service.BackendService;
import org.rabix.engine.rest.service.JobService;
import org.rabix.engine.rest.service.impl.BackendServiceImpl;
import org.rabix.engine.rest.service.impl.JobServiceImpl;
import org.rabix.executor.ExecutorModule;
import org.rabix.executor.service.ExecutorService;
import org.rabix.ftp.SimpleFTPModule;
import org.rabix.transport.backend.impl.BackendLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;

/**
 * Local command line executor
 */
public class BackendCommandLine {

  private static final Logger logger = LoggerFactory.getLogger(BackendCommandLine.class);
  private static String configDir = "/.bunny/config";

  public static void main(String[] commandLineArguments) {
    final CommandLineParser commandLineParser = new DefaultParser();
    final Options posixOptions = createOptions();

    CommandLine commandLine;
    try {
      commandLine = commandLineParser.parse(posixOptions, commandLineArguments);
      if (commandLine.hasOption("h")) {
        printUsageAndExit(posixOptions);
      }
      if (!checkCommandLine(commandLine)) {
        printUsageAndExit(posixOptions);
      }

      String appPath = commandLine.getArgList().get(0);
      File appFile = new File(appPath);
      if (!appFile.exists()) {
        logger.info("Application file {} does not exist.", appFile.getCanonicalPath());
        printUsageAndExit(posixOptions);
      }

      String inputsPath = commandLine.getArgList().get(1);
      File inputsFile = new File(inputsPath);
      if (!inputsFile.exists()) {
        logger.info("Inputs file {} does not exist.", inputsFile.getCanonicalPath());
        printUsageAndExit(posixOptions);
      }

      File configDir = getConfigDir(commandLine, posixOptions);

      if (!configDir.exists() || !configDir.isDirectory()) {
        logger.info("Config directory {} doesn't exist or is not a directory", configDir.getCanonicalPath());
        printUsageAndExit(posixOptions);
      }

      Map<String, Object> configOverrides = new HashMap<>();
      String executionDirPath = commandLine.getOptionValue("execution-dir");
      if (executionDirPath != null) {
        File executionDir = new File(executionDirPath);
        if (!executionDir.exists() || !executionDir.isDirectory()) {
          logger.info("Execution directory {} doesn't exist or is not a directory", executionDirPath);
          System.exit(10);
        } else {
          configOverrides.put("backend.execution.directory", executionDir.getCanonicalPath());
        }
      } else {
        String workingDir = null;
        try {
          workingDir = inputsFile.getParentFile().getCanonicalPath();
        } catch (Exception e) {
          workingDir = new File(".").getCanonicalPath();
        }
        configOverrides.put("backend.execution.directory", workingDir);
      }

      ConfigModule configModule = new ConfigModule(configDir, configOverrides);
      Injector injector = Guice.createInjector(
          new SimpleFTPModule(), 
          new EngineModule(),
          new ExecutorModule(configModule), 
          new AbstractModule() {
            @Override
            protected void configure() {
              bind(JobDB.class).in(Scopes.SINGLETON);
              bind(BackendDB.class).in(Scopes.SINGLETON);
              bind(JobService.class).to(JobServiceImpl.class).in(Scopes.SINGLETON);
              bind(BackendService.class).to(BackendServiceImpl.class).in(Scopes.SINGLETON);
              bind(BackendDispatcher.class).in(Scopes.SINGLETON);
              bind(JobHTTPService.class).to(JobHTTPServiceImpl.class);
              bind(BackendHTTPService.class).to(BackendHTTPServiceImpl.class).in(Scopes.SINGLETON);
            }
          });

      String appUrl = URIHelper.createURI(URIHelper.FILE_URI_SCHEME, appPath);
      String inputsText = readFile(inputsFile.getAbsolutePath(), Charset.defaultCharset());
      Map<String, Object> inputs = JSONHelper.readMap(JSONHelper.transformToJSON(inputsText));
      
      if (commandLine.hasOption("t")) {
        Bindings bindings = BindingsFactory.create(appUrl);
        System.out.println(JSONHelper.writeObject(createConformanceTestResults(appUrl, inputs, bindings.getProtocolType())));
        System.exit(0);
      }

      final JobService jobService = injector.getInstance(JobService.class);
      final BackendService backendService = injector.getInstance(BackendService.class);
      final ExecutorService executorService = injector.getInstance(ExecutorService.class);
      
      BackendLocal backendLocal = new BackendLocal();
      backendLocal = backendService.create(backendLocal);
      executorService.initialize(backendLocal);
      
      final Job job = jobService.create(new Job(appUrl, inputs));
      
      Thread checker = new Thread(new Runnable() {
        @Override
        public void run() {
          Job rootJob = jobService.get(job.getId());
          
          while(!Job.isFinished(rootJob)) {
            try {
              Thread.sleep(1000);
              rootJob = jobService.get(job.getId());
            } catch (InterruptedException e) {
              logger.error("Failed to wait for root Job to finish", e);
              throw new RuntimeException(e);
            }
          }
          if (rootJob.getStatus().equals(JobStatus.COMPLETED)) {
            try {
              logger.info(JSONHelper.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootJob.getOutputs()));
              System.exit(0);
            } catch (JsonProcessingException e) {
              logger.error("Failed to write outputs to standard out", e);
              System.exit(10);
            }
          } else {
            System.exit(10);
          }
        }
      });
      checker.start();
      checker.join();

    } catch (ParseException e) {
      logger.error("Encountered exception while parsing using PosixParser.", e);
    } catch (Exception e) {
      logger.error("Encountered exception while reading a input file.", e);
    }
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> createConformanceTestResults(String appURI, Map<String, Object> inputs, ProtocolType protocolType) throws BindingException {
    switch (protocolType) {
    case DRAFT2:
      Draft2DocumentResolver draft2DocumentResolver = new Draft2DocumentResolver();
      
      String draft2ResolvedApp = draft2DocumentResolver.resolve(appURI);
      Draft2JobApp draft2App = BeanSerializer.deserialize(draft2ResolvedApp, Draft2JobApp.class);
      
      if (!draft2App.isCommandLineTool()) {
        logger.error("The application is not a valid command line tool.");
        System.exit(10);
      }
      
      Draft2CommandLineTool draft2CommandLineTool = BeanSerializer.deserialize(draft2ResolvedApp, Draft2CommandLineTool.class);
      Draft2Job draft2Job = new Draft2Job(draft2CommandLineTool, (Map<String, Object>) inputs);
      Map<String, Object> draft2AllocatedResources = (Map<String, Object>) inputs.get("allocatedResources");
      Integer draft2Cpu = draft2AllocatedResources != null ? (Integer) draft2AllocatedResources.get("cpu") : null;
      Integer draft2Mem = draft2AllocatedResources != null ? (Integer) draft2AllocatedResources.get("mem") : null;
      draft2Job.setResources(new Draft2Resources(false, draft2Cpu, draft2Mem));

      Draft2CommandLineBuilder draft2CommandLineBuilder = new Draft2CommandLineBuilder();
      List<Object> draft2CommandLineParts = draft2CommandLineBuilder.buildCommandLineParts(draft2Job);
      String draft2Stdin;
      try {
        draft2Stdin = draft2CommandLineTool.getStdin(draft2Job);
        String draft2Stdout = draft2CommandLineTool.getStdout(draft2Job);

        Draft2CreateFileRequirement draft2CreateFileRequirement = draft2CommandLineTool.getCreateFileRequirement();
        Map<Object, Object> draft2CreatedFiles = new HashMap<>();
        if (draft2CreateFileRequirement != null) {
          for (Draft2FileRequirement draft2FileRequirement : draft2CreateFileRequirement.getFileRequirements()) {
            draft2CreatedFiles.put(draft2FileRequirement.getFilename(draft2Job), draft2FileRequirement.getContent(draft2Job));
          }
        }
        Map<String, Object> draft2Result = new HashMap<>();
        draft2Result.put("args", draft2CommandLineParts);
        draft2Result.put("stdin", draft2Stdin);
        draft2Result.put("stdout", draft2Stdout);
        draft2Result.put("createfiles", draft2CreatedFiles);
        return draft2Result;
      } catch (Draft2ExpressionException e) {
        throw new BindingException(e);
      }
    case DRAFT3:
      Draft3DocumentResolver draft3DocumentResolver = new Draft3DocumentResolver();

      String draft3ResolvedApp = draft3DocumentResolver.resolve(appURI);
      Draft3JobApp draft3App = BeanSerializer.deserialize(draft3ResolvedApp, Draft3JobApp.class);

      if (!draft3App.isCommandLineTool()) {
        logger.error("The application is not a valid command line tool.");
        System.exit(10);
      }

      Draft3CommandLineTool draft3CommandLineTool = BeanSerializer.deserialize(draft3ResolvedApp, Draft3CommandLineTool.class);
      Draft3Job draft3Job = new Draft3Job(draft3CommandLineTool, (Map<String, Object>) inputs);
      Map<String, Object> draft3AllocatedResources = (Map<String, Object>) inputs.get("allocatedResources");
      Integer draft3Cpu = draft3AllocatedResources != null ? (Integer) draft3AllocatedResources.get("cpu") : null;
      Integer draft3Mem = draft3AllocatedResources != null ? (Integer) draft3AllocatedResources.get("mem") : null;
      draft3Job.setResources(new Draft3Resources(false, draft3Cpu, draft3Mem));

      Draft3CommandLineBuilder draft3CommandLineBuilder = new Draft3CommandLineBuilder();
      List<Object> draft3CommandLineParts = draft3CommandLineBuilder.buildCommandLineParts(draft3Job);
      String draft3Stdin;
      try {
        draft3Stdin = draft3CommandLineTool.getStdin(draft3Job);
        String draft3Stdout = draft3CommandLineTool.getStdout(draft3Job);

        Draft3CreateFileRequirement draft3CreateFileRequirement = draft3CommandLineTool.getCreateFileRequirement();
        Map<Object, Object> draft3CreatedFiles = new HashMap<>();
        if (draft3CreateFileRequirement != null) {
          for (Draft3FileRequirement draft3FileRequirement : draft3CreateFileRequirement.getFileRequirements()) {
            draft3CreatedFiles.put(draft3FileRequirement.getFilename(draft3Job), draft3FileRequirement.getContent(draft3Job));
          }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("args", draft3CommandLineParts);
        result.put("stdin", draft3Stdin);
        result.put("stdout", draft3Stdout);
        result.put("createfiles", draft3CreatedFiles);
        return result;
      } catch (Draft3ExpressionException e) {
        throw new BindingException(e);
      }
    default:
      break;
    }
    return null;
  }
  
  /**
   * Reads content from a file
   */
  static String readFile(String path, Charset encoding) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, encoding);
  }

  /**
   * Create command line options
   */
  private static Options createOptions() {
    Options options = new Options();
    options.addOption("v", "verbose", false, "verbose");
    options.addOption("e", "execution-dir", true, "execution directory");
    options.addOption("l", "log-iterations-dir", true, "log engine tables directory");
    options.addOption("c", "configuration-dir", true, "configuration directory");
    options.addOption("t", "conformance-test", false, "conformance test");
    options.addOption("h", "help", false, "help");
    return options;
  }

  /**
   * Check for missing options
   */
  private static boolean checkCommandLine(CommandLine commandLine) {
    if (commandLine.getArgList().size() != 2) {
      logger.info("Invalid number of arguments");
      return false;
    }
    return true;
  }

  /**
   * Prints command line usage
   */
  private static void printUsageAndExit(Options options) {
    new HelpFormatter().printHelp("rabix [OPTION]... <tool> <job>", options);
    System.exit(0);
  }

  private static File getConfigDir(CommandLine commandLine, Options options) throws IOException {
    String configPath = commandLine.getOptionValue("configuration-dir");
    if (configPath != null) {
      File config = new File(configPath);
      if (config.exists() && config.isDirectory()) {
        return config;
      } else {
        logger.debug("Configuration directory {} doesn't exist or is not a directory.", configPath);
      }
    }
    File config = new File(new File(BackendCommandLine.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile().getParentFile() + "/config");
    
    logger.debug("Config path: " + config.getCanonicalPath());
    if (config.exists() && config.isDirectory()) {
      logger.debug("Configuration directory found localy.");
      return config;
    }
    String homeDir = System.getProperty("user.home");

    config = new File(homeDir, configDir);
    if (!config.exists() || !config.isDirectory()) {
      logger.info("Config directory doesn't exist or is not a directory");
      printUsageAndExit(options);
    }
    return config;
  }

}
