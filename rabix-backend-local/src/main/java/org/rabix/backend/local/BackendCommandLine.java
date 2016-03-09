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
import org.rabix.bindings.model.Context;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.config.ConfigModule;
import org.rabix.common.helper.JSONHelper;
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
  
  public static void main(String[] commandLineArguments) {
    final CommandLineParser commandLineParser = new DefaultParser();
    final Options posixOptions = createOptions();

    CommandLine commandLine;
    try {
      commandLine = commandLineParser.parse(posixOptions, commandLineArguments);
      if (commandLine.hasOption("h")) {
        printUsageAndExit(posixOptions);
      }
      checkCommandLine(commandLine);

      String inputsPath = commandLine.getOptionValue("inputs");
      File inputsFile = new File(inputsPath);
      if (!inputsFile.exists()) {
        logger.info("Inputs file {} does not exist.", inputsFile.getCanonicalPath());
        System.exit(10);
      }
      
      String appPath = commandLine.getOptionValue("app");
      File appFile = new File(appPath);
      if (!appFile.exists()) {
        logger.info("Application file {} does not exist.", appFile.getCanonicalPath());
        System.exit(10);
      }
      
      File configDir = new File("config");
      if (!configDir.exists() || !configDir.isDirectory()) {
        logger.info("Config directory {} doesn't exist or is not a directory", configDir.getCanonicalPath());
        System.exit(10);
      }
      
      Map<String, Object> configOverrides = new HashMap<>();
      String executionDirPath = commandLine.getOptionValue("execution-dir");
      File executionDir = new File(executionDirPath);
      if (!executionDir.exists() || !executionDir.isDirectory()) {
        logger.info("Execution directory {} doesn't exist or is not a directory", executionDirPath);
        System.exit(10);
      } else {
        configOverrides.put("backend.execution.directory", executionDir.getCanonicalPath());
      }
      ConfigModule configModule = new ConfigModule(configDir, configOverrides);
      Injector injector = Guice.createInjector(new SimpleFTPModule(), new EngineModule(), new ExecutorModule(configModule));
      DAGNodeDB nodeDB = injector.getInstance(DAGNodeDB.class);
      EventProcessor eventProcessor = injector.getInstance(EventProcessor.class);
      JobService jobService = injector.getInstance(JobService.class);
      VariableService variableService = injector.getInstance(VariableService.class);
      LinkService linkService = injector.getInstance(LinkService.class);
      ExecutorService executorService = injector.getInstance(ExecutorService.class);
      ContextService contextService = injector.getInstance(ContextService.class);
      
      String appText = readFile(appFile.getAbsolutePath(), Charset.defaultCharset());
      String inputsText = readFile(inputsFile.getAbsolutePath(), Charset.defaultCharset());

      Bindings bindings = BindingsFactory.createFromAppText(appText);
      DAGNode node = bindings.translateToDAG(appText, inputsText);
      Object inputs = bindings.translateInputs(inputsText);

      Context context = new Context(Context.createUniqueID(), null);
      List<IterationCallback> callbacks = new ArrayList<>();
      
      String outputDirPath = commandLine.getOptionValue("log-iterations-dir");
      File outputDir = new File(outputDirPath);
      if (!outputDir.exists() || !outputDir.isDirectory()) {
        logger.info("Log iterations directory {} doesn't exist or is not a directory", outputDir.getCanonicalPath());
        System.exit(10);
      } else {
        callbacks.add(new CommandLinePrinter(outputDir, context.getId(), jobService, variableService, linkService, contextService, nodeDB));
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
    options.addOption("a", "app", true, "application file");
    options.addOption("i", "inputs", true, "inputs file");
    options.addOption("v", "verbose", false, "verbose");
    options.addOption("e", "execution-dir", true, "execution directory");
    options.addOption("l", "log-iterations-dir", true, "log engine tables directory");
    options.addOption("h", "help", false, "help");
    return options;
  }

  /**
   * Check for missing options 
   */
  private static void checkCommandLine(CommandLine commandLine) {
    if (!commandLine.hasOption("app")) {
      logger.info("missing application file");
      System.exit(10);
    }
    if (!commandLine.hasOption("inputs")) {
      logger.info("missing inputs file");
      System.exit(10);
    }
  }

  /**
   * Prints command line usage 
   */
  private static void printUsageAndExit(Options options) {
    new HelpFormatter().printHelp("rabix [OPTION]...", options);
    System.exit(0);
  }

  /**
   * Detects end of execution per root Job
   */
  private static class EndRootCallback implements IterationCallback {
    
    private Bindings bindings;
    
    private JobService jobService;
    private ContextService contextService;
    private VariableService variableService;

    public EndRootCallback(ContextService contextService, JobService jobService, VariableService variableService, Bindings bindings) {
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
