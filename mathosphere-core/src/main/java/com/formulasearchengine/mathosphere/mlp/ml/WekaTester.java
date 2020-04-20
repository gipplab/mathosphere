package com.formulasearchengine.mathosphere.mlp.ml;

import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.formulasearchengine.mathosphere.mlp.ml.WekaUtils.MATCH;

/**
 * @author Andre Greiner-Petter
 */
public class WekaTester {
//    private static final Path BASE = Paths.get("src/test/resources/com/formulasearchengine/mathosphere/mlp/ml/");
    private static final Path BASE = Paths.get("test-ml-w-jacobi");
    private static final Path SVM_MODEL_PATH = BASE.resolve("svm_model_c_1.0_gamma_0.0185881361.model");
    private static final Path FILTER_PATH = BASE.resolve("string_filter_c_1.0_gamma_0.0185881361.model");
    private static final Path ARFF_PATH = BASE.resolve("instances.arff");

    private static FilteredClassifier svm;
    private static StringToWordVector stringToWordVector;

    public static void open() throws Exception {
        svm = (FilteredClassifier) weka.core.SerializationHelper.read(SVM_MODEL_PATH.toString());
        stringToWordVector = (StringToWordVector) weka.core.SerializationHelper.read(FILTER_PATH.toString());
    }

    public static Instances readInstances() throws Exception {
        BufferedReader reader =
                new BufferedReader(new FileReader(ARFF_PATH.toFile()));
        ArffLoader.ArffReader arff = new ArffLoader.ArffReader(reader);
        Instances instances;
        instances = arff.getData();
        instances.setClassIndex(instances.numAttributes() - 1);
        return instances;
    }

    public static void main(String[] args) throws Exception {
        open();
        Instances instances = readInstances();
        Instances stringReplaced = Filter.useFilter(instances, stringToWordVector);
        for (int i = 0; i < instances.size(); i++) {
//            Instance instance = instances.get(i);
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
