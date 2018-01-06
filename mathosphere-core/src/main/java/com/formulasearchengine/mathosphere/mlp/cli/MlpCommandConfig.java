package com.formulasearchengine.mathosphere.mlp.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.io.Serializable;

@Parameters(commandDescription = "Applies the MLP algorithm to a single wiki article")
public class MlpCommandConfig extends BaseConfig implements Serializable {

  @Parameter(names = {"-in", "--inputFile"}, description = "path to the wiki article", required = true)
  private String input;

  @Parameter(names = {"-out", "--outputFile"}, description = "path to output file (if empty, print to stdout)")
  private String output;

  public String getInput() {
    return input;
  }

  public String getOutput() {
    return output;
  }

}
