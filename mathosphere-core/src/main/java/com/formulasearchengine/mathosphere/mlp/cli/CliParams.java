package com.formulasearchengine.mathosphere.mlp.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameters;
import com.formulasearchengine.mathosphere.mathpd.cli.FlinkPdCommandConfig;

import java.io.Serializable;

public class CliParams implements Serializable {

  public static final String COUNT = "count";
  public static final String LIST = "list";
  public static final String EXTRACT = "extract";
  public static final String MLP = "mlp";
  public static final String EVAL = "eval";
  public static final String ML = "ml";
  public static final String PD = "pd";
  public static final String HELP = "help";
  private EvalCommandConfig evalCommand;

  @Parameters(commandDescription = "Prints this help message")
  static private class HelpCommand {
  }

  private JCommander jc;

  private CountCommandConfig countCommand;
  private ListCommandConfig listCommand;
  private MlpCommandConfig extractCommand;
  private MachineLearningDefinienExtractionConfig mlCommand;
  private FlinkMlpCommandConfig mlpCommand;
  private FlinkPdCommandConfig pdCommand;

  private String command;

  private CliParams() {
  }

  public static CliParams from(String[] args) {
    JCommander jc = new JCommander();

    CliParams params = new CliParams();
    params.countCommand = new CountCommandConfig();
    params.listCommand = new ListCommandConfig();
    params.mlpCommand = new FlinkMlpCommandConfig();
    params.extractCommand = new MlpCommandConfig();
    params.mlCommand = new MachineLearningDefinienExtractionConfig();
    params.evalCommand = new EvalCommandConfig();
    params.pdCommand = new FlinkPdCommandConfig();

    jc.addCommand(COUNT, params.countCommand);
    jc.addCommand(LIST, params.listCommand);
    jc.addCommand(EXTRACT, params.extractCommand);
    jc.addCommand(MLP, params.mlpCommand);
    jc.addCommand(EVAL, params.evalCommand);
    jc.addCommand(ML, params.mlCommand);
    jc.addCommand(PD, params.pdCommand);
    jc.addCommand(HELP, new HelpCommand());

    jc.parse(args);

    params.command = jc.getParsedCommand();
    params.jc = jc;
    return params;
  }

  public void printHelp() {
    jc.usage();
  }

  public String getCommand() {
    return command;
  }

  public CountCommandConfig getCount() {
    return countCommand;
  }

  public ListCommandConfig getListCommandConfig() {
    return listCommand;
  }

  public FlinkMlpCommandConfig getMlpCommandConfig() {
    return mlpCommand;
  }

  public MlpCommandConfig getExtractCommandConfig() {
    return extractCommand;
  }

  public EvalCommandConfig getEvalCommandConfig() {
    return evalCommand;
  }

  public MachineLearningDefinienExtractionConfig getMachineLearningCommand() {
    return mlCommand;
  }

  public FlinkPdCommandConfig getPdCommandConfig() {
    return pdCommand;
  }
}
