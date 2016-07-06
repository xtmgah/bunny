package org.rabix.backend.local;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.ProtocolType;
import org.rabix.bindings.filemapper.FileMapper;
import org.rabix.bindings.filemapper.FileMappingException;
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
import org.rabix.bindings.protocol.draft3.bean.resource.requirement.Draft3CreateFileRequirement;
import org.rabix.bindings.protocol.draft3.bean.resource.requirement.Draft3CreateFileRequirement.Draft3FileRequirement;
import org.rabix.bindings.protocol.draft3.bean.resource.requirement.Draft3ResourceRequirement;
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
import org.rabix.executor.config.FileConfig;
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
      File appFile = new File(URIHelper.extractBase(appPath));
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
      String executionDirPath = commandLine.getOptionValue("basedir");
      if (executionDirPath != null) {
        File executionDir = new File(executionDirPath);
        if (!executionDir.exists() || !executionDir.isDirectory()) {
          logger.info("Execution directory {} doesn't exist or is not a directory", executionDirPath);
          System.exit(10);
        } else {
          configOverrides.put("backend.execution.directory", executionDir.getCanonicalPath());
        }
      } else {
        configOverrides.put("backend.execution.directory", new File(".").getCanonicalPath());
      }
      if (commandLine.hasOption("no-container")) {
        configOverrides.put("backend.docker.enabled", false);
      }
      
      Path inputsDir = Paths.get(Paths.get(inputsFile.getCanonicalPath()).getParent().toAbsolutePath().toString());
      Path workDir = Paths.get((String) configOverrides.get("backend.execution.directory")).toAbsolutePath();
      configOverrides.put("conformance.inputs.directory", inputsDir.toString());
      configOverrides.put("conformance.outputs.directory", workDir.toString());
      
      final ConfigModule configModule = new ConfigModule(configDir, configOverrides);
      Injector injector = Guice.createInjector(new SimpleFTPModule(), new EngineModule(),
          new ExecutorModule(configModule), new AbstractModule() {
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

      Configuration configuration = configModule.provideConfig();
      Boolean conformance = configuration.getString(FileConfig.BUNNY_CONFORMANCE) != null;
      if(conformance) {
        BindingsFactory.setProtocol(configuration.getString(FileConfig.BUNNY_CONFORMANCE));
      }

      String appUrl = URIHelper.createURI(URIHelper.FILE_URI_SCHEME, appPath);
      String inputsText = readFile(inputsFile.getAbsolutePath(), Charset.defaultCharset());
      Map<String, Object> inputs = JSONHelper.readMap(JSONHelper.transformToJSON(inputsText));
      
      if (conformance && commandLine.hasOption("t")) {
        workDir = Paths.get("").toAbsolutePath();
        Path pathRelative = workDir.relativize(inputsDir);
        Bindings bindings = BindingsFactory.create(appUrl);
        System.out.println(JSONHelper.writeObject(createConformanceTestResults(appUrl, inputs, bindings, pathRelative.toString())));
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

          while (!Job.isFinished(rootJob)) {
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
  private static Map<String, Object> createConformanceTestResults(String appURI, Map<String, Object> inputs, Bindings bindings, String inputsDir) throws BindingException {
    ProtocolType protocolType = bindings.getProtocolType();
    Job job = null;
    switch (protocolType) {
    case DRAFT2:
      String draft2ResolvedApp = Draft2DocumentResolver.resolve(appURI);
      Draft2JobApp draft2App = BeanSerializer.deserialize(draft2ResolvedApp, Draft2JobApp.class);
      
      if (!draft2App.isCommandLineTool()) {
        logger.error("The application is not a valid command line tool.");
        System.exit(10);
      }

      Draft2CommandLineTool draft2CommandLineTool = BeanSerializer.deserialize(draft2ResolvedApp, Draft2CommandLineTool.class);
      
      Map<String, Object> draft2AllocatedResources = (Map<String, Object>) inputs.get("allocatedResources");
      Integer draft2Cpu = draft2AllocatedResources != null ? (Integer) draft2AllocatedResources.get("cpu") : null;
      Integer draft2Mem = draft2AllocatedResources != null ? (Integer) draft2AllocatedResources.get("mem") : null;
      job = new Job(appURI, inputs);
      job = bindings.mapInputFilePaths(job, new BackendCommandLine().new ConformanceFileMapper(inputsDir));
      inputs = job.getInputs();
      inputs.put("allocatedResources", draft2AllocatedResources);
      Draft2Job draft2Job = new Draft2Job(draft2CommandLineTool, inputs);
      draft2Job.setResources(new Draft2Resources(false, draft2Cpu, draft2Mem));

      Draft2CommandLineBuilder draft2CommandLineBuilder = new Draft2CommandLineBuilder();
      List<Object> draft2CommandLineParts = draft2CommandLineBuilder.buildCommandLineParts(draft2Job);
      String draft2Stdin;
      try {
        draft2Stdin = draft2CommandLineTool.getStdin(draft2Job);
        String draft2Stdout = draft2CommandLineTool.getStdout(draft2Job);

        Draft2CreateFileRequirement draft2CreateFileRequirement = draft2CommandLineTool.getCreateFileRequirement();
        Map<Object, Object> draft2CreatedFiles = null;
        if (draft2CreateFileRequirement != null) {
          draft2CreatedFiles = new HashMap<>();
          for (Draft2FileRequirement draft2FileRequirement : draft2CreateFileRequirement.getFileRequirements()) {
            draft2CreatedFiles.put(draft2FileRequirement.getFilename(draft2Job), draft2FileRequirement.getContent(draft2Job));
          }
        }
        Map<String, Object> draft2Result = new HashMap<>();
        draft2Result.put("args", commandLineToString(draft2CommandLineParts));
        draft2Result.put("stdin", draft2Stdin);
        draft2Result.put("stdout", draft2Stdout);
        draft2Result.put("createfiles", draft2CreatedFiles);
        return draft2Result;
      } catch (Draft2ExpressionException e) {
        throw new BindingException(e);
      }
    case DRAFT3:
      String draft3ResolvedApp = Draft3DocumentResolver.resolve(appURI);
      Draft3JobApp draft3App = BeanSerializer.deserialize(draft3ResolvedApp, Draft3JobApp.class);

      if (!draft3App.isCommandLineTool()) {
        logger.error("The application is not a valid command line tool.");
        System.exit(10);
      }

      Draft3CommandLineTool draft3CommandLineTool = BeanSerializer.deserialize(draft3ResolvedApp, Draft3CommandLineTool.class);
      Draft3ResourceRequirement resourceRequirement = draft3CommandLineTool.getResourceRequirement();
      
      job = new Job(appURI, inputs);
      job = bindings.mapInputFilePaths(job, new BackendCommandLine().new ConformanceFileMapper(inputsDir));
      inputs = job.getInputs();
      Draft3Job draft3Job = new Draft3Job(draft3CommandLineTool, (Map<String, Object>) inputs);
      try {
        if(resourceRequirement != null) {
          draft3Job.setRuntime(resourceRequirement.build(draft3Job));
        }
      } catch (Draft3ExpressionException e1) {
        throw new BindingException(e1);
      }
      Draft3CommandLineBuilder draft3CommandLineBuilder = new Draft3CommandLineBuilder();
      List<Object> draft3CommandLineParts = draft3CommandLineBuilder.buildCommandLineParts(draft3Job);
      String draft3Stdin;
      try {
        draft3Stdin = draft3CommandLineTool.getStdin(draft3Job);
        String draft3Stdout = draft3CommandLineTool.getStdout(draft3Job);

        Draft3CreateFileRequirement draft3CreateFileRequirement = draft3CommandLineTool.getCreateFileRequirement();
        Map<Object, Object> draft3CreatedFiles = null;
        if (draft3CreateFileRequirement != null) {
          draft3CreatedFiles = new HashMap<>();
          for (Draft3FileRequirement draft3FileRequirement : draft3CreateFileRequirement.getFileRequirements()) {
            draft3CreatedFiles.put(draft3FileRequirement.getFilename(draft3Job), draft3FileRequirement.getContent(draft3Job));
          }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("args", commandLineToString(draft3CommandLineParts));
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
    options.addOption("b", "basedir", true, "execution directory");
    options.addOption("c", "configuration-dir", true, "configuration directory");
    options.addOption("t", "conformance-test", false, "conformance test");
    options.addOption(null, "no-container", false, "don't use containers");
    options.addOption(null, "tmpdir-prefix", true, "doesn't do anything");
    options.addOption(null, "tmp-outdir-prefix", true, "doesn't do anything");
    options.addOption("o", "outdir", true, "");
    options.addOption(null, "quiet", false, "quiet");
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

  private static List<String> commandLineToString(List<Object> commandLineParts) {
    List<String> commandLineString = new ArrayList<String>();
    for (Object part : commandLineParts) {
      commandLineString.add(part.toString());
    }
    return commandLineString;
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
    File config = new File(
        new File(BackendCommandLine.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile()
            .getParentFile() + "/config");

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
  
  public class ConformanceFileMapper implements FileMapper {
    
    private String inputsDir;
    
    public ConformanceFileMapper(String inputsDir) {
      this.inputsDir = inputsDir;
    }
    
    @Override
    public String map(String filePath) throws FileMappingException {
      return new File(inputsDir, filePath).getPath();
    }
  }

}
