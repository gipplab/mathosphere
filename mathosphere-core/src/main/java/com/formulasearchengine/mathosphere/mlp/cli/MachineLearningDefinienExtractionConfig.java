package com.formulasearchengine.mathosphere.mlp.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

/**
 * Created by Leo on 16.01.2017.
 */
public class MachineLearningDefinienExtractionConfig extends FlinkMlpCommandConfig {
  @Parameter(names = {"--samplePercent"}, description = "how much of the training data should be used for training.")
  protected double[] percent = new double[]{100d};
  @Parameter(names = {"--multiThreadedCrossEvaluation"}, description = "If the cross evaluation should be done with 10 parallel threads.")
  protected boolean multiThreadedCrossEvaluation = false;
  @Parameter(names = {"--svmCost"}, description = "Cost value for the svm.")
  protected double[] svmCost = new double[]{0.074325445d};
  @Parameter(names = {"--svmGgamma"}, description = "Gamma value for the svm.")
  protected double[] svmGamma = new double[]{0.026278013d};
  @Parameter(names = {"--writeSvmModel"}, description = "Writes the models from the cross evaluation to the output directory.")
  protected boolean writeSvmModel;
  @Parameter(names = {"--dependencyParserModel"}, description = "Location of the model for the dependency parser.")
  protected String dependencyParserModel = "edu/stanford/nlp/models/parser/nndep/english_UD.gz";

  public double[] getPercent() {
    return percent;
  }

  public void setPercent(double[] percent) {
    this.percent = percent;
  }

  public boolean isMultiThreadedCrossEvaluation() {
    return multiThreadedCrossEvaluation;
  }

  public void setMultiThreadedCrossEvaluation(boolean multiThreadedCrossEvaluation) {
    this.multiThreadedCrossEvaluation = multiThreadedCrossEvaluation;
  }

  public double[] getSvmCost() {
    return svmCost;
  }

  public void setSvmCost(double[] svmCost) {
    this.svmCost = svmCost;
  }

  public double[] getSvmGamma() {
    return svmGamma;
  }

  public void setSvmGamma(double[] svmGamma) {
    this.svmGamma = svmGamma;
  }

  public boolean getWriteSvmModel() {
    return writeSvmModel;
  }

  public boolean isWriteSvmModel() {
    return writeSvmModel;
  }

  public void setWriteSvmModel(boolean writeSvmModel) {
    this.writeSvmModel = writeSvmModel;
  }

  public static MachineLearningDefinienExtractionConfig test() {
    MachineLearningDefinienExtractionConfig test = new MachineLearningDefinienExtractionConfig();
    test.dataset = "c:/tmp/mlp/input/eval_dataset.xml";
    test.outputdir = "c:/tmp/mlp/output/";
    test.setUseTeXIdentifiers(true);
    test.texvcinfoUrl = "http://localhost:10044/texvcinfo";
    return test;
  }

  public static MachineLearningDefinienExtractionConfig from(String[] args) {
    if (args.length == 0) {
      return test();
    }

    MachineLearningDefinienExtractionConfig config = new MachineLearningDefinienExtractionConfig();
    JCommander commander = new JCommander();
    commander.addObject(config);
    commander.parse(args);
    return config;
  }

  public String dependencyParserModel() {
    return dependencyParserModel;
  }

  public void dependencyParserModel(String dependencyParserLocation) {
    this.dependencyParserModel = dependencyParserModel;
  }
}
