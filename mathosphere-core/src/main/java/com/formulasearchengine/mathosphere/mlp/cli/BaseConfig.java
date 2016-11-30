package com.formulasearchengine.mathosphere.mlp.cli;


import com.beust.jcommander.Parameter;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

public class BaseConfig implements Serializable {
  protected static final String DEFAULT_POS_MODEL =
    "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";
  @Parameter(names = {"-pos", "--posModel"}, description = "POS model to use")
  protected String model = DEFAULT_POS_MODEL;

  @Parameter(names = {"-l", "--language"}, description = "Language of the input")
  protected String language = "en";

  @Parameter(names = {"-a", "--alpha"})
  protected double alpha = 1.0;

  @Parameter(names = {"-b", "--beta"})
  protected double beta = 1.0;

  @Parameter(names = {"-g", "--gamma"})
  protected double gamma = 0.1;

  @Parameter(names = {"-t", "--threshold"})
  protected double threshold = 0.4;

  @Parameter(names = {"-w", "--wikiDataList"})
  protected String wikiDataFile = null;

  @Parameter(names = {"--tex"})
  protected boolean useTeXIdentifiers = false;

  @Parameter(names = {"--texvcinfo"})
  protected String texvcinfoUrl = "http://api.formulasearchengine.com/v1/media/math/check/tex";

  @Parameter(names = {"--definitionMerging"}, description = "apply definition merging algorithm")
  protected Boolean definitionMerging = false;

  public Boolean getDefinitionMerging() {
    return definitionMerging;
  }

  public BaseConfig() {
    Properties prop = new Properties();
    String propFileName = "mathosphere.properties";

    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

    if (inputStream != null) {
      try {
        prop.load(inputStream);
        final String texvcinfo = prop.getProperty("texvcinfo");
        if (texvcinfo.length() > 0) {
          texvcinfoUrl = texvcinfo;
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
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

  public BaseConfig setWikiDataFile(String wikiDataFile) {
    this.wikiDataFile = wikiDataFile;
    return this;
  }

  public String getTexvcinfoUrl() {
    return texvcinfoUrl;
  }

  public void setModel(String model) {
    this.model = model;
  }
}
