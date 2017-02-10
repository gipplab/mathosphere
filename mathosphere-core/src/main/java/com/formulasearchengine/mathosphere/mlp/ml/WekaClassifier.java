package com.formulasearchengine.mathosphere.mlp.ml;

import com.formulasearchengine.mathosphere.mlp.cli.MachineLearningDefinienClassifierConfig;
import com.formulasearchengine.mathosphere.mlp.pojos.WikiDocumentOutput;
import com.formulasearchengine.mlp.evaluation.pojo.IdentifierDefinition;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instance;
import weka.core.Instances;

import java.io.*;
import java.util.*;

import static com.formulasearchengine.mathosphere.mlp.ml.WekaUtils.*;

/**
 * Created by Leo on 23.12.2016.
 * Classifies extracted relations with the provided machine learning model.
 */
public class WekaClassifier extends RichFlatMapFunction<WikiDocumentOutput, String> {

  public final MachineLearningDefinienClassifierConfig config;
  private FilteredClassifier svm;
  private DependencyParser parser;

  public WekaClassifier(MachineLearningDefinienClassifierConfig config) throws IOException {
    this.config = config;
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    svm = (FilteredClassifier) weka.core.SerializationHelper.read(config.getSvmModel());
    parser = DependencyParser.loadFromModelFile(config.dependencyParserModel());
  }

  @Override
  public void flatMap(WikiDocumentOutput doc, Collector<String> out) throws Exception {
    Instances instances;
    WekaUtils wekaUtils = new WekaUtils();
    instances = wekaUtils.createInstances("AllRelations");
    wekaUtils.addRelationsToInstances(parser, doc.getRelations(), doc.getTitle(), doc.getqId(), instances, doc.getMaxSentenceLength());
    Set<IdentifierDefinition> extractions = new HashSet<>();
    for (Instance instance : instances) {
      String match = instances.classAttribute().value(0);
      String predictedClass = instances.classAttribute().value((int) svm.classifyInstance(instance));
      if (match.equals(predictedClass)) {
        IdentifierDefinition extraction = new IdentifierDefinition(
          instance.stringValue(instance.attribute(instances.attribute(IDENTIFIER).index())),
          instance.stringValue(instance.attribute(instances.attribute(DEFINIEN).index())));
        //put in set to deal with duplicates
        extractions.add(extraction);
      }
    }
    for (IdentifierDefinition extraction : extractions) {
      String string = identifierDefinitionToEscapedString(doc, extraction);
      out.collect(string);
    }
  }

  private String identifierDefinitionToEscapedString(WikiDocumentOutput doc, IdentifierDefinition extraction) {
    return doc.getqId() + ",\""
      + doc.getTitle().replaceAll("\\s", "_") + "\",\""
      + extraction.getIdentifier() + "\",\""
      + extraction.getDefinition() + "\"";
  }
}

