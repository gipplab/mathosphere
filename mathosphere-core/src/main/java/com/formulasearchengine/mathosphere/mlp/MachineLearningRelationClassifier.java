package com.formulasearchengine.mathosphere.mlp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formulasearchengine.mathosphere.mlp.cli.MachineLearningDefinienClassifierConfig;
import com.formulasearchengine.mathosphere.mlp.cli.MachineLearningDefinienExtractionConfig;
import com.formulasearchengine.mathosphere.mlp.contracts.JsonSerializerMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.StupidRelationScorer;
import com.formulasearchengine.mathosphere.mlp.contracts.TextAnnotatorMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.TextExtractorMapper;
import com.formulasearchengine.mathosphere.mlp.pojos.EvaluationResult;
import com.formulasearchengine.mathosphere.mlp.ml.WekaClassifier;
import com.formulasearchengine.mathosphere.mlp.pojos.ParsedWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.Relation;
import com.formulasearchengine.mathosphere.mlp.pojos.StrippedWikiDocumentOutput;
import com.formulasearchengine.mathosphere.mlp.pojos.WikiDocumentOutput;
import com.formulasearchengine.mathosphere.mlp.text.SimpleFeatureExtractorMapper;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.core.fs.FileSystem;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Leo on 10.02.2017.
 */
public class MachineLearningRelationClassifier {

  private static Map<String, Object> ndData;

  public static void find(MachineLearningDefinienClassifierConfig config) throws Exception {
    //parse wikipedia (subset) and process afterwards
    ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
    env.setParallelism(config.getParallelism());
    DataSource<String> source = readWikiDump(config, env);
    DataSet<ParsedWikiDocument> documents = source.flatMap(new TextExtractorMapper())
      .map(new TextAnnotatorMapper(config));
    Logger.getRootLogger().setLevel(Level.ERROR);
    DataSet<WikiDocumentOutput> instances = documents.map(new SimpleFeatureExtractorMapper(config, null));
    //process parsed wikipedia
    DataSet<WikiDocumentOutput> result = instances.map(new WekaClassifier(config));
    ObjectMapper mapper = new ObjectMapper();
    if (config.getNamespace()) {
      File ndFile = new File(config.getNdFile());
      List ndList = mapper.readValue(ndFile, List.class);
      ndData = new HashMap<>();
      for (Object o : ndList) {
        final Map entry = (Map) o;
        ndData.put(((String) entry.get("document_title")).replaceAll(" ", "_"), o);
      }
    }
    DataSet<WikiDocumentOutput> withNamespaces = result.map(new MapFunction<WikiDocumentOutput, WikiDocumentOutput>() {
      @Override
      public WikiDocumentOutput map(WikiDocumentOutput wikiDocumentOutput) throws Exception {
        if (config.getNamespace()) {
          wikiDocumentOutput.setRelations(new ArrayList<>());
          final Map nd = (Map) ndData.get(wikiDocumentOutput.getTitle().replaceAll("\\s", "_"));
          if (nd != null) {
            List relNS = (List) nd.get("namespace_relations");
            if (relNS != null)
              for (Object o : relNS) {
                Relation rel = new Relation(o);
                wikiDocumentOutput.getRelations().add(rel);
              }
          }
        }
        return wikiDocumentOutput;
      }
    });
    if (config.isEvaluate()) {
      DataSet<EvaluationResult> evaluationResult = withNamespaces.reduceGroup(new StupidRelationScorer(MachineLearningDefinienExtractionConfig.test()));
      evaluationResult.map(new JsonSerializerMapper<>()).writeAsText(config.getOutputDir() + "/extractedDefiniens/evaluated", FileSystem.WriteMode.OVERWRITE);
    }
    DataSet<StrippedWikiDocumentOutput> stripped_result = withNamespaces.map(stripSentenceMapper);

    //write and kick off flink execution
    stripped_result.map(new JsonSerializerMapper<>())
      .writeAsText(config.getOutputDir() + "/extractedDefiniens/json", FileSystem.WriteMode.OVERWRITE);
    env.execute();
  }

  public static DataSource<String> readWikiDump(MachineLearningDefinienClassifierConfig config, ExecutionEnvironment
    env) {
    return FlinkMlpRelationFinder.readWikiDump(config, env);
  }

  private static MapFunction<WikiDocumentOutput, StrippedWikiDocumentOutput> stripSentenceMapper =
    (MapFunction<WikiDocumentOutput, StrippedWikiDocumentOutput>) wikiDocumentOutput ->
      new StrippedWikiDocumentOutput(wikiDocumentOutput);
}
