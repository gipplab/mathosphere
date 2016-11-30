package com.formulasearchengine.mathosphere.mlp;

import com.formulasearchengine.mathosphere.mlp.cli.EvalCommandConfig;
import com.formulasearchengine.mathosphere.mlp.cli.FlinkMlpCommandConfig;
import com.formulasearchengine.mathosphere.mlp.contracts.JsonSerializerMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.TextAnnotatorMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.TextExtractorMapper;
import com.formulasearchengine.mathosphere.mlp.features.FeatureVector;
import com.formulasearchengine.mathosphere.mlp.pojos.MyWikiDocumentOutput;
import com.formulasearchengine.mathosphere.mlp.pojos.ParsedWikiDocument;
import com.formulasearchengine.mathosphere.mlp.text.FeatureExtractor;
import com.formulasearchengine.mlp.evaluation.Evaluator;
import com.formulasearchengine.mlp.evaluation.GoldEntry;
import com.formulasearchengine.mlp.evaluation.IdentifierDefinition;
import com.google.common.base.*;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.core.fs.FileSystem.WriteMode;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

public class MachineLearningRelationFinder {

  public static final double TRUE_POSITIVE = 1.0d;
  private static final double FALSE_POSITIVE = -1.0d;

  public static void main(String[] args) throws Exception {
  }

  public DataSet<FeatureVector> find(ExecutionEnvironment env) throws Exception {
    EvalCommandConfig config = EvalCommandConfig.test();

    DataSource<String> source = readWikiDump(config, env);
    DataSet<ParsedWikiDocument> documents = source.flatMap(new TextExtractorMapper())
      .map(new TextAnnotatorMapper(config));
    Logger.getRootLogger().setLevel(Level.ERROR);
    DataSet<MyWikiDocumentOutput> features = documents.map(new FeatureExtractor());
    features.map(new JsonSerializerMapper<>())
      .writeAsText(config.getOutputDir(), WriteMode.OVERWRITE);
    ArrayList<GoldEntry> gold = (new Evaluator()).readGoldEntries(new File("C:\\tmp\\mlp\\input\\gold.json"));
    //add gold information to extracted featureVectors
    DataSet<FeatureVector> labeledVectorDataSet = features.flatMap(new FlatMapFunction<MyWikiDocumentOutput, FeatureVector>() {
      @Override
      public void flatMap(MyWikiDocumentOutput value, Collector c) throws Exception {
        //isPresent() must succeed otherwise the gold standard was not complete
        GoldEntry goldEntry = gold.stream().filter(g -> g.getTitle().equals(value.getTitle().replaceAll(" ", "_"))).findFirst().get();
        List<IdentifierDefinition> identifierDefinitions = goldEntry.getDefinitions();
        for (FeatureVector fv : value.getFeatureVectors()) {
          for (IdentifierDefinition idd : identifierDefinitions) {
            if (fv.identifier.equals(idd.getIdentifier()) && fv.definition.replaceAll("\\[|\\]", "").trim().toLowerCase().equals(idd.getDefinition())) {
              fv.truePositive = 1d;
              break;
            }
          }
          fv.setqId(Integer.parseInt(goldEntry.getqID()));
          c.collect(fv);
        }
      }
    });
    return labeledVectorDataSet;
  }

  private String resourcePath(String resourceName) {
    ClassLoader classLoader = getClass().getClassLoader();
    URL resource = classLoader.getResource(resourceName);
    return decodePath(resource.getFile());
  }

  private static String decodePath(String urlEncodedPath) {
    try {
      return URLDecoder.decode(urlEncodedPath, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw Throwables.propagate(e);
    }
  }

  public static DataSource<String> readWikiDump(FlinkMlpCommandConfig config, ExecutionEnvironment env) {
    return FlinkMlpRelationFinder.readWikiDump(config, env);
  }
}
