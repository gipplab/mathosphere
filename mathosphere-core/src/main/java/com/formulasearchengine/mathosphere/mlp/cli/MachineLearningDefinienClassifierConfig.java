package com.formulasearchengine.mathosphere.mlp.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.io.InputStream;

/**
 * Created by Leo on 09.02.2017.
 */
public class MachineLearningDefinienClassifierConfig extends EvalCommandConfig {

  @Parameter(names = {"--evaluate"}, description = "Weather or not to evaluate against the gold standard.")
  protected boolean evaluate = false;

  public String getSvmModel() {
    return svmModel;
  }

  @Parameter(names = {"--stringFilter"}, description = "File location of the stringToWordVector filter to use.")
  protected String stringToWordVectorFilter;

  @Parameter(names = {"--svmModel"}, description = "File location of the svm model to use.")
  protected String svmModel;

  @Parameter(names = {"--dependencyParserModel"}, description = "Location of the model for the dependency parser.")
  protected String dependencyParserModel = "edu/stanford/nlp/models/parser/nndep/english_UD.gz";

  public static MachineLearningDefinienClassifierConfig test() {
    MachineLearningDefinienClassifierConfig test = new MachineLearningDefinienClassifierConfig();
    test.dataset = "c:/tmp/mlp/input/eval_dataset.xml";
    test.outputdir = "c:/tmp/mlp/output/";
    test.setUseTeXIdentifiers(true);
    test.texvcinfoUrl = "http://localhost:10044/texvcinfo";
    test.parallelism = 1;
    test.setSvmModel("C:\\Develop\\mathosphere4\\mathosphere-core\\target\\svm_model__c_1.0_gamma_0.018581361.model");
    test.stringToWordVectorFilter = "C:\\Develop\\mathosphere4\\mathosphere-core\\target\\string_filter__c_1.0_gamma_0.018581361.model";
    return test;
  }


  public static MachineLearningDefinienClassifierConfig from(String[] args) {
    if (args.length == 0) {
      return test();
    }

    MachineLearningDefinienClassifierConfig config = new MachineLearningDefinienClassifierConfig();
    JCommander commander = new JCommander();
    commander.addObject(config);
    commander.parse(args);

    return config;
  }

  public String dependencyParserModel() {
    return dependencyParserModel;
  }

  public void setSvmModel(String svmModel) {
    this.svmModel = svmModel;
  }

  public String getStringToWordVectorFilter() {
    return stringToWordVectorFilter;
  }

  public boolean isEvaluate() {
    return evaluate;
  }
}
