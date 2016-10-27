package com.formulasearchengine.mathosphere.mlp.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameters;
import com.formulasearchengine.mathosphere.mathpd.cli.FlinkPdCommandConfig;

import java.io.Serializable;

public class CliParams implements Serializable {

  private EvalCommandConfig evalCommand;

  @Parameters(commandDescription = "Prints this help message")
  static private class HelpCommand {
  }

  private JCommander jc;

  private CountCommandConfig countCommand;
  private ListCommandConfig listCommand;
  private MlpCommandConfig extractCommand;
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
    params.evalCommand = new EvalCommandConfig();
    params.pdCommand = new FlinkPdCommandConfig();

    jc.addCommand("count", params.countCommand);
    jc.addCommand("list", params.listCommand);
    jc.addCommand("extract", params.extractCommand);
    jc.addCommand("mlp", params.mlpCommand);
    jc.addCommand("eval", params.evalCommand);
    jc.addCommand("pd", params.pdCommand);
    jc.addCommand("help", new HelpCommand());

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

  public FlinkPdCommandConfig getPdCommandConfig() {
    return pdCommand;
  }
}
