package com.formulasearchengine.mathosphere.mlp;

import com.formulasearchengine.mathosphere.mlp.cli.EvalCommandConfig;
import com.formulasearchengine.mathosphere.mlp.cli.FlinkMlpCommandConfig;
import com.formulasearchengine.mathosphere.mlp.cli.MachineLearningDefinienExtractionConfig;
import com.formulasearchengine.mathosphere.mlp.contracts.JsonSerializerMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.TextAnnotatorMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.TextExtractorMapper;
import com.formulasearchengine.mathosphere.mlp.ml.WekaLearner;
import com.formulasearchengine.mathosphere.mlp.pojos.ParsedWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.WikiDocumentOutput;
import com.formulasearchengine.mathosphere.mlp.text.SimpleFeatureExtractor;
import com.formulasearchengine.mlp.evaluation.Evaluator;
import com.formulasearchengine.mlp.evaluation.pojo.GoldEntry;
import com.google.common.base.*;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.core.fs.FileSystem.WriteMode;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

public class MachineLearningRelationFinder {

  public static void main(String[] args) throws Exception {
    /*MachineLearningDefinienExtractionConfig config = MachineLearningDefinienExtractionConfig.from(args);
    config.setMultiThreadedCrossEvaluation(true);
    find(config);*/
    generateCoraseParameterGrid();
  }

  public static void test() throws Exception {
    find(MachineLearningDefinienExtractionConfig.test());
  }

  public static DataSet<WikiDocumentOutput> find(MachineLearningDefinienExtractionConfig config) throws Exception {
    ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
    DataSource<String> source = readWikiDump(config, env);
    DataSet<ParsedWikiDocument> documents = source.flatMap(new TextExtractorMapper())
      .map(new TextAnnotatorMapper(config));
    Logger.getRootLogger().setLevel(Level.ERROR);
    ArrayList<GoldEntry> gold = (new Evaluator()).readGoldEntries(new File("C:\\tmp\\mlp\\input\\gold.json"));
    DataSet<WikiDocumentOutput> instances = documents.map(new SimpleFeatureExtractor(config, gold));
    DataSet<Object> a = instances.reduceGroup(new WekaLearner(config));
    a.map(new JsonSerializerMapper<>())
      .writeAsText(config.getOutputDir() + "\\tmp", WriteMode.OVERWRITE);
    env.execute();
    return instances;
  }

  public static DataSource<String> readWikiDump(MachineLearningDefinienExtractionConfig config, ExecutionEnvironment env) {
    return FlinkMlpRelationFinder.readWikiDump(config, env);
  }

  private static void generateSatuationData() throws Exception {
    MachineLearningDefinienExtractionConfig config = MachineLearningDefinienExtractionConfig.test();
    //config.setPercent(new double[]{10, 20, 30, 40, 50, 60, 70, 80, 90, 100});
    config.setMultiThreadedCrossEvaluation(true);
    find(config);
  }

  private static void generateCoraseParameterGrid() throws Exception {
    MachineLearningDefinienExtractionConfig config = MachineLearningDefinienExtractionConfig.test();
    config.setSvmCost(WekaLearner.C_corase);
    config.setSvmGamma(WekaLearner.Y_corase);
    find(config);
  }

  private static void generateFineParameterGrid() throws Exception {
    MachineLearningDefinienExtractionConfig config = MachineLearningDefinienExtractionConfig.testfine();
    config.setSvmCost(WekaLearner.C_fine);
    config.setSvmGamma(WekaLearner.Y_fine);
    find(config);
  }


}
