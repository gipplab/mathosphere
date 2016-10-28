package com.formulasearchengine.mathosphere.mathpd.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.formulasearchengine.mathosphere.mlp.cli.BaseConfig;

import java.io.Serializable;

@Parameters(commandDescription = "Runs the MLP algorithm (on Flink)")
public class FlinkPdCommandConfig extends BaseConfig implements Serializable {

  @Parameter(names = {"-in", "--inputDir"}, description = "path to the directory with arxivdump")
  protected String dataset;

  @Parameter(names = {"-ref", "--referenceDir"}, description = "path to the directory with files to compare")
  protected String ref;

  @Parameter(names = {"-out", "--outputDir"}, description = "path to output directory")
  protected String outputdir;

  @Parameter(names = {"--treads"}, description = "how many parallel threads should be used")
  protected int parallelism = 0;

  public FlinkPdCommandConfig() {
  }

  public static FlinkPdCommandConfig test() {
    String dataset = "c:/tmp/mlp/input/";
    String outputdir = "c:/tmp/mlp/output/";
    String refdir = "c:/tmp/mlp/ref/";

    FlinkPdCommandConfig cfg = new FlinkPdCommandConfig(dataset, refdir, outputdir);
    cfg.setUseTeXIdentifiers(false);
    return cfg;
  }

  public static FlinkPdCommandConfig from(String[] args) {
    if (args.length == 0) {
      return test();
    }

    FlinkPdCommandConfig config = new FlinkPdCommandConfig();
    JCommander commander = new JCommander();
    commander.addObject(config);
    commander.parse(args);
    return config;
  }

  public FlinkPdCommandConfig(String dataset, String reference, String outputdir) {
    this.dataset = dataset;
    this.outputdir = outputdir;
    this.ref = reference;
  }

  public FlinkPdCommandConfig(String dataset, String outputdir, String model, String language, double alpha, double beta,
                              double gamma, double threshold, Boolean useTex) {
    super(model, language, alpha, beta, gamma, threshold, useTex);
    this.dataset = dataset;
    this.outputdir = outputdir;
  }

  public String getDataset() {
    return dataset;
  }

  public String getOutputDir() {
    return outputdir;
  }

  public int getParallelism() {
    return parallelism;
  }

  public String getRef() {
    return ref;
  }
}
