package com.formulasearchengine.mathosphere.mlp.ml;

import com.formulasearchengine.mathosphere.mlp.pojos.WikiDocumentOutput;
import libsvm.svm;
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

import java.io.File;
import java.util.*;

import static com.formulasearchengine.mathosphere.mlp.ml.WekaUtils.*;
import static weka.core.Range.indicesToRangeList;

/**
 * Created by Leo on 23.12.2016.
 */
public class WekaLearner implements GroupReduceFunction<WikiDocumentOutput, Object> {

  private static final int folds = 10;
  private static final Integer[] rand = new Integer[]{
    99, 53, 70, 23, 86, 19, 84, 76, 34, 82,
    32, 50, 47, 63, 54, 20, 74, 94, 46, 18,
    22, 42, 100, 88, 96, 24, 31, 7, 12, 17,
    26, 33, 29, 25, 79, 90, 56, 81, 4, 72,
    27, 80, 60, 97, 5, 11, 67, 37, 2, 78,
    6, 21, 51, 28, 91, 35, 8, 71, 14, 41,
    52, 55, 62, 38, 30, 66, 58, 75, 93, 40,
    48, 49, 89, 15, 69, 61, 65, 85, 9, 77,
    98, 1, 43, 3, 83, 16, 36, 95, 44, 92,
    59, 68, 45, 13, 39, 73, 87, 10, 57, 64};

  @Override
  public void reduce(Iterable<WikiDocumentOutput> values, Collector<Object> out) throws Exception {
    Instances instances = createInstances("AllRelations");
    for (WikiDocumentOutput value : values) {
      addRelationsToInstances(value.getRelations(), value.getTitle(), value.getqId(), instances, value.getMaxSentenceLength());
    }
    StringToWordVector stringToWordVector = new StringToWordVector();
    stringToWordVector.setAttributeIndices(indicesToRangeList(new int[]{
      instances.attribute(SURFACE_TEXT_AND_POS_TAG_OF_TWO_PRECEDING_AND_FOLLOWING_TOKENS_AROUND_THE_DESC_CANDIDATE).index(),
      instances.attribute(SURFACE_TEXT_AND_POS_TAG_OF_THREE_PRECEDING_AND_FOLLOWING_TOKENS_AROUND_THE_PAIRED_MATH_EXPR).index(),
      instances.attribute(SURFACE_TEXT_OF_THE_FIRST_VERB_THAT_APPEARS_BETWEEN_THE_DESC_CANDIDATE_AND_THE_TARGET_MATH_EXPR).index(),
      instances.attribute(DEP_3_FROM_IDENTIFIER).index(),
      instances.attribute(DEP_3_FROM_DEFINIEN).index(),
      instances.attribute(SENTENCE).index()}));
    stringToWordVector.setWordsToKeep(1000);
    NGramTokenizer nGramTokenizer = new NGramTokenizer();
    nGramTokenizer.setNGramMaxSize(3);
    nGramTokenizer.setNGramMinSize(1);
    nGramTokenizer.setDelimiters(nGramTokenizer.getDelimiters().replaceAll(":", ""));
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

    for (int p = 100; p <= 100; p += 5) {
      double[] C = {Math.pow(2, -5), Math.pow(2, -3), Math.pow(2, -1), Math.pow(2, 0), Math.pow(2, 1), Math.pow(2, 3), Math.pow(2, 5), Math.pow(2, 7), Math.pow(2, 9), Math.pow(2, 11), Math.pow(2, 13), Math.pow(2, 15)};
      double[] Y = C.clone();
      for (int i = 0; i < C.length; i++) {
        Y[C.length - i - 1] = C[i];
      }

      for (int c = 0; c < C.length; c++) {
        for (int y = 0; y < Y.length; y++) {
          Resample sampler = new Resample();
          sampler.setInputFormat(stringsReplacedData);
          sampler.setRandomSeed(1);
          sampler.setBiasToUniformClass(0);
          sampler.setSampleSizePercent(p);
          Instances reduced = Filter.useFilter(stringsReplacedData, sampler);

          Resample resampleFilter = new Resample();
          resampleFilter.setRandomSeed(1);
          resampleFilter.setBiasToUniformClass(1d);
          resampleFilter.setInputFormat(reduced);
          Instances resampled = Filter.useFilter(reduced, resampleFilter);

          LibSVM svm = new LibSVM();
          svm.setCost(c);
          svm.setGamma(y);
          MultiFilter multi = new MultiFilter();
          multi.setFilters(new Filter[]{removeFilter});
          FilteredClassifier filteredClassifier = new FilteredClassifier();
          filteredClassifier.setClassifier(svm);
          filteredClassifier.setFilter(multi);

          Evaluation eval = new Evaluation(reduced);
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
                //test.add(a);
              }
            }
            for (int i = 0; i < resampled.numInstances(); i++) {
              Instance a = resampled.instance(i);
              if (testIds.contains(Integer.parseInt(a.stringValue(a.attribute(stringsReplacedData.attribute(Q_ID).index()))))) {
                //train.add(a);
              } else {
                test.add(a);
              }
            }


            // build and evaluate classifier
            svm.setModelFile(new File("C:\\tmp\\output\\model_" + p + "_percent_fold_" + n));
            Classifier clsCopy = FilteredClassifier.makeCopy(filteredClassifier);
            clsCopy.buildClassifier(train);
            eval.setPriors(train);
            eval.evaluateModel(clsCopy, test);
            averagePrecision[n] = eval.precision(0);
            averageRecall[n] = eval.recall(0);

          }
          //System.out.print(p + " percent ");
          System.out.print("Cost " + c + ", gamma, " + y);
          System.out.print(", prec, " + Arrays.toString(averagePrecision));
          System.out.println(", recall, " + Arrays.toString(averageRecall));
        }
      }
    }
    out.collect(new Object());
  }
}
