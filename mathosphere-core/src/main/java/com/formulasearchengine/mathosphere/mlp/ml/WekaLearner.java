package com.formulasearchengine.mathosphere.mlp.ml;

import com.formulasearchengine.mathosphere.mlp.pojos.WikiDocumentOutput;
import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.util.Collector;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.LibSVM;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.util.Random;

import static com.formulasearchengine.mathosphere.utils.WekaUtils.ATTRIBUTE_SENTENCE;
import static com.formulasearchengine.mathosphere.utils.WekaUtils.addRelationsToInstances;
import static com.formulasearchengine.mathosphere.utils.WekaUtils.createInstances;
import static weka.core.Range.indicesToRangeList;

/**
 * Created by Leo on 23.12.2016.
 */
public class WekaLearner implements GroupReduceFunction<WikiDocumentOutput, Object> {

  @Override
  public void reduce(Iterable<WikiDocumentOutput> values, Collector<Object> out) throws Exception {
    Instances instances = createInstances("AllRelations");
    for (WikiDocumentOutput value : values) {
      addRelationsToInstances(value.getRelations(), value.getTitle(), instances);
    }
    StringToWordVector filter = new StringToWordVector();
    filter.setAttributeIndices(indicesToRangeList(new int[]{ATTRIBUTE_SENTENCE}));
    filter.setInputFormat(instances);

    Instances filteredData = Filter.useFilter(instances, filter);
    Remove removeFilter = new Remove();
    removeFilter.setAttributeIndices(indicesToRangeList(new int[]{0, 1, 2}));
    removeFilter.setInputFormat(filteredData);
    Instances removeFilteredData = Filter.useFilter(filteredData, removeFilter);
    System.out.println(instances.toString());
    Resample resampleFilter = new Resample();
    resampleFilter.setRandomSeed(1);
    resampleFilter.setBiasToUniformClass(1d);
    resampleFilter.setInputFormat(removeFilteredData);
    Instances resampled = Filter.useFilter(removeFilteredData, resampleFilter);
    LibSVM svm = new LibSVM();
    //svm.buildClassifier(filteredData);
    Evaluation eval = new Evaluation(resampled);
    Random rand = new Random(1);  // using seed = 1
    int folds = 10;
    eval.crossValidateModel(svm, resampled, folds, rand);
    System.out.println(eval.toSummaryString());
    System.out.println(eval.toClassDetailsString());
    System.out.println(eval.toMatrixString());
    System.out.println(eval.toCumulativeMarginDistributionString());

    out.collect(new Object());
  }
}
