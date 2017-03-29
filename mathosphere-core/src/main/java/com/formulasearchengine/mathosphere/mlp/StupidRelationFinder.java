package com.formulasearchengine.mathosphere.mlp;

import com.formulasearchengine.mathosphere.mlp.cli.MachineLearningDefinienExtractionConfig;
import com.formulasearchengine.mathosphere.mlp.contracts.JsonSerializerMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.StupidRelationScorer;
import com.formulasearchengine.mathosphere.mlp.contracts.TextAnnotatorMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.TextExtractorMapper;
import com.formulasearchengine.mathosphere.mlp.pojos.EvaluationResult;
import com.formulasearchengine.mathosphere.mlp.pojos.ParsedWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.WikiDocumentOutput;
import com.formulasearchengine.mathosphere.mlp.text.SimpleFeatureExtractorMapper;
import com.formulasearchengine.mlp.evaluation.Evaluator;
import com.formulasearchengine.mlp.evaluation.pojo.GoldEntry;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.core.fs.FileSystem.WriteMode;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;

public class StupidRelationFinder {

  public static void main(String[] args) throws Exception {
    MachineLearningDefinienExtractionConfig config = MachineLearningDefinienExtractionConfig.from(args);
    find(config);
  }

  public static void test() throws Exception {
    find(MachineLearningDefinienExtractionConfig.test());
  }

  public static void find(MachineLearningDefinienExtractionConfig config) throws Exception {
    ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
    env.setParallelism(config.getParallelism());
    DataSource<String> source = readWikiDump(config, env);
    DataSet<ParsedWikiDocument> documents = source.flatMap(new TextExtractorMapper())
      .map(new TextAnnotatorMapper(config));
    Logger.getRootLogger().setLevel(Level.ERROR);
    ArrayList<GoldEntry> gold = (new Evaluator()).readGoldEntries(new File(config.getGoldFile()));
    DataSet<WikiDocumentOutput> instances = documents.map(new SimpleFeatureExtractorMapper(config, gold));
    //get extraction results and rate all of them without selecting
    DataSet<EvaluationResult> result = instances.reduceGroup(new StupidRelationScorer(config));
    //write to kick off flink execution
    result.map(new JsonSerializerMapper<>())
      .writeAsText(config.getOutputDir() + "\\tmp", WriteMode.OVERWRITE);
    env.execute();
  }

  public static DataSource<String> readWikiDump(MachineLearningDefinienExtractionConfig config, ExecutionEnvironment env) {
    return FlinkMlpRelationFinder.readWikiDump(config, env);
  }
}
