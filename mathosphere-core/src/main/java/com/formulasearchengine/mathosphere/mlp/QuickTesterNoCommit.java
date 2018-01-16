package com.formulasearchengine.mathosphere.mlp;

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
import weka.core.converters.ArffLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.formulasearchengine.mathosphere.mlp.ml.WekaUtils.*;

/**
 * Created by Leo on 23.12.2016.
 * Classifies extracted relations with the provided machine learning model.
 * Retains only the highest ranking, positive relations.
 */
public class QuickTesterNoCommit{

    private static FilteredClassifier svm;
    private static StringToWordVector stringToWordVector;
    private static final Path base = Paths.get("t/output");

    public static void open() throws Exception {
        svm = (FilteredClassifier) weka.core.SerializationHelper.read( base.resolve("svm_model_c_2.0_gamma_0.022097087.model").toString() );
        stringToWordVector = (StringToWordVector) weka.core.SerializationHelper.read(base.resolve("string_filter_c_2.0_gamma_0.022097087.model").toString());

//        svm = (FilteredClassifier) weka.core.SerializationHelper.read("C:\\Develop\\mathosphere4\\mathosphere-core\\target\\svm_model__c_1.0_gamma_0.018581361.model");
//        stringToWordVector = (StringToWordVector) weka.core.SerializationHelper.read("C:\\Develop\\mathosphere4\\mathosphere-core\\target\\string_filter__c_1.0_gamma_0.018581361.model");
    }

    public static Instances readInstances()  throws Exception {
        BufferedReader reader =
                new BufferedReader(new FileReader( base.resolve("instances.arff").toString()) );
        ArffLoader.ArffReader arff = new ArffLoader.ArffReader(reader);
        Instances instances;
        instances = arff.getData();
        instances.setClassIndex(instances.numAttributes() - 1);
        return instances;
    }

    public static void main(String[] args) throws Exception {
        open();
        Instances instances;
        instances = readInstances();
        Instances stringReplaced = Filter.useFilter(instances, stringToWordVector);
        for (int i = 0; i < instances.size(); i++) {
            Instance instance = stringReplaced.get(i);
            String predictedClass = instances.classAttribute().value((int) svm.classifyInstance(instance));
            if (predictedClass.equals(MATCH)) {
                System.out.println(instance.toString() + " matched");
            } else {
                System.out.println(instance.toString() + " not matched");
            }
        }
    }
}


