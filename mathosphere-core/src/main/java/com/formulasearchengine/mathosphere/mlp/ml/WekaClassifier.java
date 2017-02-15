package com.formulasearchengine.mathosphere.mlp.ml;

import com.formulasearchengine.mathosphere.mlp.cli.MachineLearningDefinienClassifierConfig;
import com.formulasearchengine.mathosphere.mlp.pojos.Relation;
import com.formulasearchengine.mathosphere.mlp.pojos.Sentence;
import com.formulasearchengine.mathosphere.mlp.pojos.WikiDocumentOutput;
import com.formulasearchengine.mlp.evaluation.pojo.IdentifierDefinition;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.trees.GrammaticalStructure;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instance;
import weka.core.Instances;

import java.io.*;
import java.util.*;

import static com.formulasearchengine.mathosphere.mlp.ml.WekaUtils.*;

/**
 * Created by Leo on 23.12.2016.
 * Classifies extracted relations with the provided machine learning model.
 * Retains only the highest ranking, positive relations.
 */
public class WekaClassifier extends RichMapFunction<WikiDocumentOutput, WikiDocumentOutput> {

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
  public WikiDocumentOutput map(WikiDocumentOutput doc) throws Exception {
    System.out.println("Classifying " + doc.getTitle());
    Instances instances;
    WekaUtils wekaUtils = new WekaUtils();
    instances = wekaUtils.createInstances("AllRelations");
    Map<Sentence, GrammaticalStructure> precomputedGraphStore = wekaUtils.getPrecomputedGraphStore();
    Map<IdentifierDefinition, Relation> positiveClassifications = new HashMap<>();
    for (int i = 0; i < doc.getRelations().size(); i++) {
      Relation relation = doc.getRelations().get(i);
      wekaUtils.addRelationToInstances(parser, precomputedGraphStore, doc.getTitle(), doc.getqId(), instances, doc.getMaxSentenceLength(), relation);
      Instance instance = instances.get(i);
      double[] distribution = svm.distributionForInstance(instance);
      String predictedClass = instances.classAttribute().value((int) svm.classifyInstance(instance));
      if (predictedClass.equals(MATCH)) {
        relation.setScore(distribution[instances.classAttribute().indexOfValue(MATCH)]);
        IdentifierDefinition extraction = new IdentifierDefinition(
          instance.stringValue(instance.attribute(instances.attribute(IDENTIFIER).index())),
          instance.stringValue(instance.attribute(instances.attribute(DEFINIEN).index())));
        //put in hashmap to deal with duplicates and preserve highest score.
        if (!positiveClassifications.containsKey(extraction)) {
          positiveClassifications.put(extraction, relation);
        } else {
          if (positiveClassifications.get(extraction).getScore() < relation.getScore()) {
            positiveClassifications.put(extraction, relation);
          }
        }
      }
    }
    //replace relations with positive ones
    doc.setRelations(new ArrayList<>(positiveClassifications.values()));
    System.out.println("Classifying done " + doc.getTitle() + " considered  " + instances.size() + " definiens");
    return doc;
  }

  private String identifierDefinitionToEscapedString(WikiDocumentOutput doc, IdentifierDefinition extraction) {
    return doc.getqId() + ",\""
      + doc.getTitle().replaceAll("\\s", "_") + "\",\""
      + extraction.getIdentifier() + "\",\""
      + extraction.getDefinition() + "\"";
  }
}

