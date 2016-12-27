package com.formulasearchengine.mathosphere.mlp.ml;

import com.formulasearchengine.mathosphere.mlp.pojos.WikiDocumentOutput;
import com.google.common.collect.Lists;
import com.sun.org.apache.xpath.internal.SourceTree;
import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.util.Collector;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.tokenizers.NGramTokenizer;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.supervised.instance.Resample;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import static com.formulasearchengine.mathosphere.utils.WekaUtils.*;
import static weka.core.Range.indicesToRangeList;

/**
 * Created by Leo on 23.12.2016.
 */
public class WekaLearner implements GroupReduceFunction<WikiDocumentOutput, Object> {

  private static final int folds = 10;
  private static final Integer[] rand = new Integer[]{
    99, 53, 70, 23, 86, 19, 84, 76, 34, 82, 32, 50,
    47, 63, 54, 20, 74, 94, 46, 18, 22, 42, 100, 88,
    96, 24, 31, 7, 12, 17, 26, 33, 29, 25, 79, 90,
    56, 81, 4, 72, 27, 80, 60, 97, 5, 11, 67, 37,
    2, 78, 6, 21, 51, 28, 91, 35, 8, 71, 14, 41, 52,
    55, 62, 38, 30, 66, 58, 75, 93, 40, 48, 49, 89,
    15, 69, 61, 65, 85, 9, 77, 98, 1, 43, 3, 83, 16, 36,
    95, 44, 92, 59, 68, 45, 13, 39, 73, 87, 10, 57, 64};

  @Override
  public void reduce(Iterable<WikiDocumentOutput> values, Collector<Object> out) throws Exception {
    Instances instances = createInstances("AllRelations");
    for (WikiDocumentOutput value : values) {
      addRelationsToInstances(value.getRelations(), value.getTitle(), value.getqId(), instances);
    }
    StringToWordVector stringToWordVector = new StringToWordVector();
    stringToWordVector.setAttributeIndices(indicesToRangeList(new int[]{
      instances.attribute(SURFACE_TEXT_AND_POS_TAG_OF_TWO_PRECEDING_AND_FOLLOWING_TOKENS_AROUND_THE_DESC_CANDIDATE).index(),
      instances.attribute(SURFACE_TEXT_AND_POS_TAG_OF_THREE_PRECEDING_AND_FOLLOWING_TOKENS_AROUND_THE_PAIRED_MATH_EXPR).index(),
      instances.attribute(SURFACE_TEXT_OF_THE_FIRST_VERB_THAT_APPEARS_BETWEEN_THE_DESC_CANDIDATE_AND_THE_TARGET_MATH_EXPR).index(),
      instances.attribute(SENTENCE).index()}));
    stringToWordVector.setWordsToKeep(1000);
    NGramTokenizer nGramTokenizer = new NGramTokenizer();
    nGramTokenizer.setNGramMaxSize(3);
    nGramTokenizer.setNGramMinSize(1);
    stringToWordVector.setInputFormat(instances);
    stringToWordVector.setTokenizer(nGramTokenizer);
    Instances stringsReplacedData = Filter.useFilter(instances, stringToWordVector);

    Remove removeFilter = new Remove();
    removeFilter.setAttributeIndices(indicesToRangeList(new int[]{
      instances.attribute(TITLE).index(),
      instances.attribute(IDENTIFIER).index(),
      instances.attribute(DEFINIEN).index(),
      instances.attribute(Q_ID).index(),
    }));
    removeFilter.setInputFormat(stringsReplacedData);
    //Instances removeFilteredData = Filter.useFilter(stringsReplacedData, removeFilter);
    Resample resampleFilter = new Resample();
    resampleFilter.setRandomSeed(1);
    resampleFilter.setBiasToUniformClass(1d);
    //resampleFilter.setInputFormat(removeFilteredData);
    resampleFilter.setInputFormat(stringsReplacedData);
    Instances resampled = Filter.useFilter(stringsReplacedData, resampleFilter);
    LibSVM svm = new LibSVM();
    //svm.setWeights("32 1");
    //svm.buildClassifier(filteredData);
    MultiFilter multi = new MultiFilter();
    multi.setFilters(new Filter[]{removeFilter});
    FilteredClassifier filteredClassifier = new FilteredClassifier();
    filteredClassifier.setClassifier(svm);
    filteredClassifier.setFilter(multi);

    Evaluation eval = new Evaluation(stringsReplacedData);
    resampled.stratify(folds);
    double[] averagePrecision = new double[folds];
    double[] averageRecall = new double[folds];
    for (int n = 0; n < folds; n++) {
      List<Integer> testIds = Arrays.asList(Arrays.copyOfRange(rand, 10 * n, 10 * (n + 1)));
      Instances train = new Instances(resampled, 1);
      Instances test = new Instances(resampled, 1);
      for (int i = 0; i < resampled.numInstances(); i++) {
        Instance a = resampled.instance(i);
        if (testIds.contains(Integer.parseInt(a.stringValue(a.attribute(resampled.attribute(Q_ID).index()))))) {
          train.add(a);
        } else {
          test.add(a);
        }
      }
      // the above code is used by the StratifiedRemoveFolds filter, the
      // code below by the Explorer/Experimenter:
      // Instances train = randData.trainCV(folds, n, rand);

      // build and evaluate classifier
      Classifier clsCopy = FilteredClassifier.makeCopy(filteredClassifier);
      clsCopy.buildClassifier(train);
      eval.setPriors(train);
      eval.evaluateModel(clsCopy, test);
      System.out.println("=== fold " + n + " === ");
      System.out.println(eval.toSummaryString());
      System.out.println(eval.toClassDetailsString());
      averagePrecision[n] = eval.precision(0);
      averageRecall[n] = eval.recall(0);
    }
    System.out.println(eval.toSummaryString());
    System.out.println(eval.toClassDetailsString());
    System.out.println(eval.toMatrixString());
    System.out.println(eval.toCumulativeMarginDistributionString());
    System.out.println(" === average 'match' precision === " + DoubleStream.of(averagePrecision).sum() / averagePrecision.length);
    System.out.println(" === average 'match' recall === " + DoubleStream.of(averageRecall).sum() / averageRecall.length);
    out.collect(new Object());
  }
}
