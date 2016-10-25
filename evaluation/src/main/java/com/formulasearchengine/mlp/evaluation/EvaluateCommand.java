package com.formulasearchengine.mlp.evaluation;


import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.io.Serializable;

@Parameters(commandDescription = "Evaluates the given file against the mlp goldstandard")
public class EvaluateCommand implements Serializable {

  @Parameter(names = {"-in"},
    description =
      "Input csv file to evaluate. Should be in the format \"qId,title,identifier,definition\\n.\" formatted according to  " +
        "\nExample: " +
        "\n1,matched,\"W\",\"van der waerden number\"")
  private String in = "";

  public String getGold() {
    return gold;
  }

  @Parameter(names = {"-gold"},
    description = "The gold standard file")
  private String gold = "";

  public EvaluateCommand() {
  }

  public String getIn() {
    return in;
  }

}
