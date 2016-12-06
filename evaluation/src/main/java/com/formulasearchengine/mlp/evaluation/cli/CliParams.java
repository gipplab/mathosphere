package com.formulasearchengine.mlp.evaluation.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameters;

import java.io.Serializable;

public class CliParams implements Serializable {

  public EvaluateCommand getEvaluateCommand() {
    return evaluateCommand;
  }

  @Parameters(commandDescription = "Prints this help message")
  static private class HelpCommand {
  }

  private EvaluateCommand evaluateCommand;

  private JCommander jc;

  private String command;

  private CliParams() {
  }

  public static CliParams from(String[] args) {
    JCommander jc = new JCommander();

    CliParams params = new CliParams();
    params.evaluateCommand = new EvaluateCommand();
    jc.addCommand("eval", params.evaluateCommand);
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
}
