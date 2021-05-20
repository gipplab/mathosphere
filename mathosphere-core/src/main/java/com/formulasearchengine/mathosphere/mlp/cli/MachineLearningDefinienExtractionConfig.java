package com.formulasearchengine.mathosphere.mlp.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Leo on 16.01.2017.
 */
public class MachineLearningDefinienExtractionConfig extends FlinkMlpCommandConfig {
  @Parameter(names = {"--samplePercent"}, description = "how much of the training data should be used for training.")
  protected List<Double> percent = Arrays.asList(100d);
  @Parameter(names = {"--svmCost"}, description = "Cost value for the svm.")
  protected List<Double> svmCost = Arrays.asList(2d);
  @Parameter(names = {"--svmGamma"}, description = "Gamma value for the svm.")
  protected List<Double> svmGamma = Arrays.asList(0.022097087d);
  @Parameter(names = {"--writeSvmModel"}, description = "Writes the models from the cross evaluation to the output directory.")
  protected boolean writeSvmModel;
  @Parameter(names = {"--instances"}, description = "File location of the instances.arff file to use for the testing and training. " +
    "Full support only for files that have been written by --writeInstances of the same version of this executable.")
  protected String instancesFile;

  public boolean isWriteInstances() {
    return writeInstances;
  }

  public boolean isCoarseSearch() {
    return coarseSearch;
  }

  public boolean isFineSearch() {
    return fineSearch;
  }

  @Parameter(names = {"--writeInstances"}, description = "Writes the data to train the svm to the output directory. Overwrites instances.arff in the output directory.")
  protected boolean writeInstances;
  @Parameter(names = {"--coarseParameterSearch"}, description = "Searches for parameters in a coarse grid of cost and gamma values.")
  protected boolean coarseSearch;
  @Parameter(names = {"--fineParameterSearch"}, description = "Searches for parameters in a fine grid of cost and gamma values.")
  protected boolean fineSearch;
  @Parameter(names = {"--leaveOneOutEvaluation"}, description = "Perform a leave one out evaluation of the models performance.")
  protected boolean leaveOneOutEvaluation = false;

  @Parameter(names = {"--goldFile"}, description = "Location of the gold data file.")
  protected String goldFile;

  public String getDependencyParserModel() {
    return dependencyParserModel;
  }

  public boolean isLeaveOneOutEvaluation() {
    return leaveOneOutEvaluation;
  }

  public String getGoldFile() {
    return goldFile;
  }

  public List<Double> getPercent() {
    return percent;
  }

  public void setPercent(List<Double> percent) {
    this.percent = percent;
  }

  public List<Double> getSvmCost() {
    return svmCost;
  }

  public void setSvmCost(List<Double> svmCost) {
    this.svmCost = svmCost;
  }

  public List<Double> getSvmGamma() {
    return svmGamma;
  }

  public void setSvmGamma(List<Double> svmGamma) {
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
    test.dataset = "c:/tmp/mlp/input/eval_dataset_sample.xml";
    test.outputdir = "c:/tmp/mlp/output/corase";
    test.goldFile = "C:/tmp/mlp/input/gold_with_alias.json";
    test.setUseTeXIdentifiers(true);
    //test.texvcinfoUrl = "http://localhost:10044/texvcinfo";
    test.parallelism = 1;
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

  public String getInstancesFile() {
    return instancesFile;
  }

  public void setInstancesFile(String instancesFile) {
    this.instancesFile = instancesFile;
  }
}
