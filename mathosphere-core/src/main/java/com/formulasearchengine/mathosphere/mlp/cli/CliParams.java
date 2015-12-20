package com.formulasearchengine.mathosphere.mlp.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameters;

import java.io.Serializable;

public class CliParams implements Serializable {

  @Parameters(commandDescription = "Prints this help message")
  static private class HelpCommand {
  }

  private JCommander jc;

  private CountCommandConfig countCommand;
  private ListCommandConfig listCommand;
  private MlpCommandConfig extractCommand;
  private FlinkMlpCommandConfig mlpCommand;

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

    jc.addCommand("count", params.countCommand);
    jc.addCommand("list", params.listCommand);
    jc.addCommand("extract", params.extractCommand);
    jc.addCommand("mlp", params.mlpCommand);
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
}
