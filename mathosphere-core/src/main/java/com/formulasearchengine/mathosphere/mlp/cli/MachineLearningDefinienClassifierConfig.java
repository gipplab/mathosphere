package com.formulasearchengine.mathosphere.mlp.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.io.InputStream;

/**
 * Created by Leo on 09.02.2017.
 */
public class MachineLearningDefinienClassifierConfig extends FlinkMlpCommandConfig {

  public String getSvmModel() {
    return svmModel;
  }

  @Parameter(names = {"--svmModel"}, description = "File location of the svm model to use.")
  protected String svmModel;

  @Parameter(names = {"--dependencyParserModel"}, description = "Location of the model for the dependency parser.")
  protected String dependencyParserModel = "edu/stanford/nlp/models/parser/nndep/english_UD.gz";

  public static MachineLearningDefinienClassifierConfig test() {
    MachineLearningDefinienClassifierConfig test = new MachineLearningDefinienClassifierConfig();
    test.dataset = "c:/tmp/mlp/input/eval_dataset.xml";
    test.outputdir = "c:/tmp/mlp/output/corase";
    test.setUseTeXIdentifiers(true);
    test.texvcinfoUrl = "http://localhost:10044/texvcinfo";
    test.parallelism = 1;
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
}
