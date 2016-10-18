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


  @Parameter(names = {"--namespace"}, description = "incorporate namespace data")
  private Boolean namespace = false;

  @Parameter(names = {"--ref"}, description = "relevance judgements folder")
  private String relevanceFolder;

  @Parameter(names = {"--level"}, description = "relevance level (1 partially relevant, 2 relevant)")
  private int level = 2;

  public String getQueries() {
    return queries;
  }

  public String getNdFile() {
    return ndFile;
  }

  public Boolean getNamespace() {
    return namespace;
  }


  public String getRelevanceFolder() {
    return relevanceFolder;
  }

  public int getLevel() {
    return level;
  }
}
