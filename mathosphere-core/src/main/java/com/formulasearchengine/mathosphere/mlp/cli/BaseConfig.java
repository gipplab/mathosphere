package com.formulasearchengine.mathosphere.mlp.cli;


import com.beust.jcommander.Parameter;

public class BaseConfig {
  private static final String DEFAULT_POS_MODEL =
      "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";
  @Parameter(names = {"-pos", "--posModel"}, description = "POS model to use")
  private String model = DEFAULT_POS_MODEL;

  @Parameter(names = {"-l", "--language"}, description = "Language of the input")
  private String language = "en";

  @Parameter(names = {"-a", "--alpha"})
  private double alpha = 1.0;

  @Parameter(names = {"-b", "--beta"})
  private double beta = 1.0;

  @Parameter(names = {"-g", "--gamma"})
  private double gamma = 0.1;

  @Parameter(names = {"-t", "--threshold"})
  private double threshold = 0.4;

  @Parameter(names = {"-w", "--wikiDataList"})
  private String wikiDataFile = null;

  @Parameter(names = {"--tex"})
  private boolean useTeXIdentifiers = true;

  public BaseConfig() {
  }

  public BaseConfig(String model, String language, double alpha, double beta, double gamma,
                    double threshold, boolean useTeXIdentifiers) {
    this.model = model;
    this.language = language;
    this.alpha = alpha;
    this.beta = beta;
    this.gamma = gamma;
    this.threshold = threshold;
    this.useTeXIdentifiers = useTeXIdentifiers;
  }

  public String getModel() {
    return model;
  }

  public double getAlpha() {
    return alpha;
  }

  public double getBeta() {
    return beta;
  }

  public double getGamma() {
    return gamma;
  }

  public double getThreshold() {
    return threshold;
  }

  public String getLanguage() {
    return language;
  }

  public boolean getUseTeXIdentifiers() {
    return useTeXIdentifiers;
  }

  public void setUseTeXIdentifiers(boolean useTeXIdentifiers) {
    this.useTeXIdentifiers = useTeXIdentifiers;
  }

  public String getWikiDataFile() {
    return wikiDataFile;
  }

}
