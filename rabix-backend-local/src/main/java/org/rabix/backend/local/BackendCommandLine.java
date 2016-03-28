package org.rabix.backend.local;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
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
import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.ProtocolType;
import org.rabix.bindings.model.Context;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.bindings.protocol.draft2.Draft2CommandLineBuilder;
import org.rabix.bindings.protocol.draft2.bean.Draft2CommandLineTool;
import org.rabix.bindings.protocol.draft2.bean.Draft2Job;
import org.rabix.bindings.protocol.draft2.bean.Draft2Resources;
import org.rabix.bindings.protocol.draft2.bean.resource.requirement.Draft2CreateFileRequirement;
import org.rabix.bindings.protocol.draft2.bean.resource.requirement.Draft2CreateFileRequirement.Draft2FileRequirement;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.config.ConfigModule;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;
import org.rabix.engine.EngineModule;
import org.rabix.engine.db.DAGNodeDB;
import org.rabix.engine.event.impl.InitEvent;
import org.rabix.engine.model.ContextRecord;
import org.rabix.engine.model.ContextRecord.ContextStatus;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.VariableRecord;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.EventProcessor.IterationCallback;
import org.rabix.engine.service.ContextService;
import org.rabix.engine.service.JobService;
import org.rabix.engine.service.LinkService;
import org.rabix.engine.service.VariableService;
import org.rabix.executor.ExecutorModule;
import org.rabix.executor.service.ExecutorService;
import org.rabix.ftp.SimpleFTPModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Guice;
import com.google.inject.Injector;

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
      }

      ConfigModule configModule = new ConfigModule(configDir, configOverrides);
      Injector injector = Guice.createInjector(new SimpleFTPModule(), new EngineModule(),
          new ExecutorModule(configModule));
      DAGNodeDB nodeDB = injector.getInstance(DAGNodeDB.class);
      EventProcessor eventProcessor = injector.getInstance(EventProcessor.class);
      JobService jobService = injector.getInstance(JobService.class);
      VariableService variableService = injector.getInstance(VariableService.class);
      LinkService linkService = injector.getInstance(LinkService.class);
      ExecutorService executorService = injector.getInstance(ExecutorService.class);
      ContextService contextService = injector.getInstance(ContextService.class);

      Bindings bindings = BindingsFactory.create(ProtocolType.DRAFT2);

      String appText = bindings.loadAppFromFile(appFile);
      String inputsText = readFile(inputsFile.getAbsolutePath(), Charset.defaultCharset());

      DAGNode node = bindings.translateToDAG(appText, inputsText);
      Object inputs = bindings.translateInputs(inputsText);

      if (commandLine.hasOption("t")) {
        Draft2CommandLineTool draft2CommandLineTool = BeanSerializer.deserialize(appText, Draft2CommandLineTool.class);
        Draft2Job draft2Job = new Draft2Job(draft2CommandLineTool, (Map<String, Object>) inputs);

        Map<String, Object> allocatedResources = (Map<String, Object>) ((Map<String, Object>) inputs)
            .get("allocatedResources");
        Integer cpu = allocatedResources != null ? (Integer) allocatedResources.get("cpu") : null;
        Integer mem = allocatedResources != null ? (Integer) allocatedResources.get("mem") : null;
        draft2Job.setResources(new Draft2Resources(false, cpu, mem));

        Draft2CommandLineBuilder draft2CommandLineBuilder = new Draft2CommandLineBuilder();
        List<Object> commandLineParts = draft2CommandLineBuilder.buildCommandLineParts(draft2Job);
        String stdin = draft2CommandLineTool.getStdin(draft2Job);
        String stdout = draft2CommandLineTool.getStdout(draft2Job);
        
        Draft2CreateFileRequirement draft2CreateFileRequirement = draft2CommandLineTool.getCreateFileRequirement();
        Map<Object, Object> createdFiles = new HashMap<>();
        if (draft2CreateFileRequirement != null) {
          for (Draft2FileRequirement fileRequirement : draft2CreateFileRequirement.getFileRequirements()) {
            createdFiles.put(fileRequirement.getFilename(draft2Job), fileRequirement.getContent(draft2Job));
          }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("args", commandLineParts);
        result.put("stdin", stdin);
        result.put("stdout", stdout);
        result.put("createfiles", createdFiles);

        System.out.println(JSONHelper.writeObject(result));
        System.exit(0);
      }

      Context context = new Context(Context.createUniqueID(), null);
      List<IterationCallback> callbacks = new ArrayList<>();

      String outputDirPath = commandLine.getOptionValue("log-iterations-dir");
      if (outputDirPath != null) {
        File outputDir = new File(outputDirPath);
        if (!outputDir.exists() || !outputDir.isDirectory()) {
          logger.info("Log iterations directory {} doesn't exist or is not a directory", outputDir.getCanonicalPath());
          System.exit(10);
        } else {
          callbacks.add(new CommandLinePrinter(outputDir, context.getId(), jobService, variableService, linkService,
              contextService, nodeDB));
        }
      }
      callbacks.add(new LocalExecutableHandler(executorService, jobService, variableService, contextService, nodeDB));
      callbacks.add(new EndRootCallback(contextService, jobService, variableService, bindings));

      InitEvent initEvent = new InitEvent(context, node, inputs);
      eventProcessor.send(initEvent);
      eventProcessor.start(callbacks);
    } catch (ParseException e) {
      logger.error("Encountered exception while parsing using PosixParser.", e);
    } catch (Exception e) {
      logger.error("Encountered exception while reading a input file.");
    }
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
    File config = new File("config");
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

  /**
   * Detects end of execution per root Job
   */
  private static class EndRootCallback implements IterationCallback {

    private Bindings bindings;

    private JobService jobService;
    private ContextService contextService;
    private VariableService variableService;

    public EndRootCallback(ContextService contextService, JobService jobService, VariableService variableService,
        Bindings bindings) {
      this.jobService = jobService;
      this.contextService = contextService;
      this.variableService = variableService;
      this.bindings = bindings;
    }

    @Override
    public void call(EventProcessor eventProcessor, String contextId, int iteration) {
      ContextRecord context = contextService.find(contextId);
      if (context.getStatus().equals(ContextStatus.COMPLETED)) {
        JobRecord root = jobService.findRoot(contextId);

        List<VariableRecord> outputVariables = variableService.find(root.getId(), LinkPortType.OUTPUT, contextId);

        Object outputs = null;
        try {
          for (VariableRecord outputVariable : outputVariables) {
            outputs = bindings.addToOutputs(outputs, outputVariable.getPortId(), outputVariable.getValue());
          }
          logger.info(JSONHelper.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(outputs));
        } catch (BindingException | JsonProcessingException e) {
          logger.error("Failed to create outputs.", e);
        }
        System.exit(0);
      }
    }
  }

}
