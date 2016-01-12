package com.formulasearchengine.mathosphere.mlp.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.io.Serializable;

@Parameters(commandDescription = "Applies the MLP evaluation to an evaluation dataset")
public class EvalCommandConfig extends FlinkMlpCommandConfig implements Serializable {


  @Parameter(names = {"--queries"}, description = "query file")
  private String queries;

  @Parameter(names = {"--nd"}, description = "namespace discovery file")
  private String ndFile;

  public String getQueries() {
    return queries;
  }

  public String getNdFile() {
    return ndFile;
  }
}
