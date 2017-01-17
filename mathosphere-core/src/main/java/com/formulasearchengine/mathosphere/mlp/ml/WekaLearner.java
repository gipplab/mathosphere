package com.formulasearchengine.mathosphere.mlp.ml;

import com.formulasearchengine.mathosphere.mlp.cli.MachineLearningDefinienExtractionConfig;
import com.formulasearchengine.mathosphere.mlp.pojos.WikiDocumentOutput;
import com.formulasearchengine.mlp.evaluation.Evaluator;
import com.formulasearchengine.mlp.evaluation.pojo.GoldEntry;
import com.formulasearchengine.mlp.evaluation.pojo.ScoreSummary;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import org.apache.commons.io.FileUtils;
import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.util.Collector;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.tokenizers.NGramTokenizer;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.supervised.instance.Resample;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

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

  /**
   * Cost.
   */
  public static final double[] C_corase = {Math.pow(2, -7), Math.pow(2, -5), Math.pow(2, -3), Math.pow(2, -1), Math.pow(2, 1), Math.pow(2, 3), Math.pow(2, 5), Math.pow(2, 7), Math.pow(2, 9), Math.pow(2, 11), Math.pow(2, 13), Math.pow(2, 15)};

  /**
   * Gamma.
   */
  public static final double[] Y_corase = {Math.pow(2, -15), Math.pow(2, -13), Math.pow(2, -11), Math.pow(2, -9), Math.pow(2, -7), Math.pow(2, -5), Math.pow(2, -3), Math.pow(2, -1), Math.pow(2, 1), Math.pow(2, 3)};

  /**
   * Cost of the interesting region.
   */
  public static final double[] C_fine = {
    Math.pow(2, -9)
    , Math.pow(2, -8.75), Math.pow(2, -8.5), Math.pow(2, -8.25), Math.pow(2, -8)
    , Math.pow(2, -7.75), Math.pow(2, -7.5), Math.pow(2, -7.25), Math.pow(2, -7)
    , Math.pow(2, -6.75), Math.pow(2, -6.5), Math.pow(2, -6.25), Math.pow(2, -6)
    , Math.pow(2, -5.75), Math.pow(2, -5.5), Math.pow(2, -5.25), Math.pow(2, -5)
    , Math.pow(2, -4.75), Math.pow(2, -4.5), Math.pow(2, -4.25), Math.pow(2, -4)
    , Math.pow(2, -3.75), Math.pow(2, -3.5), Math.pow(2, -3.25), Math.pow(2, -3)
    , Math.pow(2, -2.75), Math.pow(2, -2.5), Math.pow(2, -2.25), Math.pow(2, -2)
    , Math.pow(2, -1.75), Math.pow(2, -1.5), Math.pow(2, -1.25), Math.pow(2, -1)
    , Math.pow(2, -0.75), Math.pow(2, -0.5), Math.pow(2, -0.25), Math.pow(2, 0)
  };
  /**
   * Gamma of the interesting region.
   */
  public static final double[] Y_fine = {
    Math.pow(2, -7)
    , Math.pow(2, -6.75), Math.pow(2, -6.5), Math.pow(2, -6.25), Math.pow(2, -6)
    , Math.pow(2, -5.75), Math.pow(2, -5.5), Math.pow(2, -5.25), Math.pow(2, -5)
    , Math.pow(2, -4.75), Math.pow(2, -4.5), Math.pow(2, -4.25), Math.pow(2, -4)
    , Math.pow(2, -3.75), Math.pow(2, -3.5), Math.pow(2, -3.25), Math.pow(2, -3)
    , Math.pow(2, -2.75), Math.pow(2, -2.5), Math.pow(2, -2.25), Math.pow(2, -2)
  };

  /**
   * Cost for highest accuracy.
   */
  public static final double[] C_best = {Math.pow(2, -3.75)};

  /**
   * Gamma for highest accuracy.
   */
  public static final double[] Y_best = {Math.pow(2, -5.25)};
  private final ArrayList<GoldEntry> gold;

  public WekaLearner(MachineLearningDefinienExtractionConfig config) throws IOException {
    this.config = config;
    this.gold = (new Evaluator()).readGoldEntries(new File(config.getQueries()));
  }

  public final MachineLearningDefinienExtractionConfig config;

  @Override
  public void reduce(Iterable<WikiDocumentOutput> values, Collector<Object> out) throws Exception {
    double[] percentages = config.getPercent();
    double[] C_used = config.getSvmCost();
    double[] Y_used = config.getSvmGamma();
    File output = new File(config.getOutputDir() + "/svm_cross_eval_statistics.csv");
    File outputDetails = new File(config.getOutputDir() + "/svm_cross_eval_detailed_statistics.txt");
    File extractedDefiniens = new File(config.getOutputDir() + "/classifications.csv");
    DependencyParser parser = DependencyParser.loadFromModelFile(config.dependencyParserModel());
    Instances instances = createInstances("AllRelations");
    for (WikiDocumentOutput value : values) {
      addRelationsToInstances(parser, value.getRelations(), value.getTitle(), value.getqId(), instances, value.getMaxSentenceLength());
    }
    StringToWordVector stringToWordVector = new StringToWordVector();
    stringToWordVector.setAttributeIndices(indicesToRangeList(new int[]{
      instances.attribute(SURFACE_TEXT_AND_POS_TAG_OF_TWO_PRECEDING_AND_FOLLOWING_TOKENS_AROUND_THE_DESC_CANDIDATE).index(),
      instances.attribute(SURFACE_TEXT_AND_POS_TAG_OF_THREE_PRECEDING_AND_FOLLOWING_TOKENS_AROUND_THE_PAIRED_MATH_EXPR).index(),
      instances.attribute(SURFACE_TEXT_OF_THE_FIRST_VERB_THAT_APPEARS_BETWEEN_THE_DESC_CANDIDATE_AND_THE_TARGET_MATH_EXPR).index(),
      instances.attribute(DEP_3_FROM_IDENTIFIER).index(),
      instances.attribute(DEP_3_FROM_DEFINIEN).index()}));
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

    FileUtils.deleteQuietly(output);
    FileUtils.deleteQuietly(outputDetails);
    FileUtils.deleteQuietly(extractedDefiniens);
    List<Double[]> parameters = new ArrayList<>();
    for (double p : percentages) {
      for (double c : C_used) {
        for (double y : Y_used) {
          parameters.add(new Double[]{p, c, y});
        }
      }
    }
    List<EvaluationResult> evaluationResults;
    if (config.isMultiThreadedCrossEvaluation()) {
      evaluationResults = parameters.parallelStream()
        .map(parameter -> crossEvaluate(stringsReplacedData, removeFilter, parameter[0], parameter[1], parameter[2])).collect(Collectors.toList());
    } else {
      evaluationResults = parameters.parallelStream()
        .map(parameter -> crossEvaluate(stringsReplacedData, removeFilter, parameter[0], parameter[1], parameter[2])).collect(Collectors.toList());
    }
    for (
      EvaluationResult evaluationResult : evaluationResults) {
      FileUtils.write(outputDetails, "Cost; " + Utils.doubleToString(evaluationResult.gamma, 10) + "; gamma; " + Utils.doubleToString(evaluationResult.gamma, 10) + "\n" + Arrays.toString(evaluationResult.text) + "\n", true);
      //remove duplicates
      StringBuilder e = new StringBuilder();
      Set<String> set = new HashSet();
      set.addAll(evaluationResult.extractions);
      List<String> list = new ArrayList<>(set);
      list.sort(Comparator.naturalOrder());
      for (String extraction : list) {
        e.append(extraction).append("\n");
      }
      Evaluator evaluator = new Evaluator();
      FileUtils.write(extractedDefiniens, "Cost; "
        + Utils.doubleToString(evaluationResult.cost, 10)
        + "; gamma; " + Utils.doubleToString(evaluationResult.gamma, 10)
        + "; percentage_of_data_used; " + evaluationResult.percent
        + "\\n", true);
      FileUtils.write(extractedDefiniens, e.toString(), true);
      StringReader reader = new StringReader(e.toString());
      ScoreSummary scoreSummary = evaluator.evaluate(evaluator.readExtractions(reader, gold, false), gold);
      //Output files
      FileUtils.write(output, "Cost; " + Utils.doubleToString(evaluationResult.cost, 10)
        + "; gamma; " + Utils.doubleToString(evaluationResult.gamma, 10)
        + "; percentage_of_data_used; " + evaluationResult.percent
        + "; accuracy; " + Arrays.toString(evaluationResult.accuracy).replaceAll("\\[|\\]", "").replaceAll(",", ";")
        + "; avg_accuracy; " + average(evaluationResult.accuracy)
        + "; prec; " + Arrays.toString(evaluationResult.averagePrecision).replaceAll("\\[|\\]", "").replaceAll(",", ";")
        + "; avg_prec; " + average(evaluationResult.averagePrecision)
        + "; recall; " + Arrays.toString(evaluationResult.averageRecall).replaceAll("\\[|\\]", "").replaceAll(",", ";")
        + "; avg_recall; " + average(evaluationResult.averageRecall)
        + "; avg_F1; " + evaluationResult.getF1()
        + "; tp; " + scoreSummary.tp + "; fn; " + scoreSummary.fn + "; fp; " + scoreSummary.fp
        + "\n", true);
    }
    out.collect(new Object());
  }

  private EvaluationResult crossEvaluate(Instances stringsReplacedData, Remove removeFilter, double percent, double cost, double gamma) {
    try {
      EvaluationResult result = new EvaluationResult(folds, percent, cost, gamma);
      //draw random sample
      Resample sampler = new Resample();
      sampler.setInputFormat(stringsReplacedData);
      sampler.setRandomSeed(1);
      //do not change distribution
      sampler.setBiasToUniformClass(0);
      sampler.setSampleSizePercent(percent);
      Instances reduced = Filter.useFilter(stringsReplacedData, sampler);

      //oversampling to deal with the ratio of the classes
      Resample resampleFilter = new Resample();
      resampleFilter.setRandomSeed(1);
      resampleFilter.setBiasToUniformClass(1d);
      resampleFilter.setInputFormat(reduced);
      Instances resampled = Filter.useFilter(reduced, resampleFilter);

      if (config.isMultiThreadedCrossEvaluation()) {
        //Do the computation in parallel
        Thread[] threads = new Thread[folds];
        for (int n = 0; n < folds; n++) {
          threads[n] = new Thread(getRunnable(n, removeFilter, cost, gamma, resampled, result.averagePrecision, result.averageRecall, result.accuracy, result.text, result.extractions));
          threads[n].start();
        }
        for (int n = 0; n < folds; n++) {
          threads[n].join();
        }
      } else {
        for (int n = 0; n < folds; n++) {
          trainAndTest(n, removeFilter, cost, gamma, resampled, result.averagePrecision, result.averageRecall, result.accuracy, result.text, result.extractions);
        }
      }
      return result;
    } catch (Exception e) {
      return new EvaluationResult(folds, percent, cost, gamma);
    }
  }

  private Runnable getRunnable(int n, Filter removeFilter, double cost, double gamma, Instances resampled,
                               double[] averagePrecision, double[] averageRecall, double[] accuracy, String[] text,
                               Collection<String> extractions) throws Exception {
    return new Runnable() {
      @Override
      public void run() {
        try {
          trainAndTest(n, removeFilter, cost, gamma, resampled, averagePrecision, averageRecall, accuracy, text, extractions);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
  }

  ;

  private void trainAndTest(int n, Filter removeFilter, double cost, double gamma, Instances resampled,
                            double[] averagePrecision, double[] averageRecall, double[] accuracy, String[] text,
                            Collection<String> extractions) throws Exception {
    LibSVM svm = new LibSVM();
    svm.setCost(cost);
    svm.setGamma(gamma);
    MultiFilter multi = new MultiFilter();
    multi.setFilters(new Filter[]{removeFilter});
    FilteredClassifier filteredClassifier = new FilteredClassifier();
    filteredClassifier.setClassifier(svm);
    filteredClassifier.setFilter(multi);

    List<Integer> testIds = Arrays.asList(Arrays.copyOfRange(rand, 10 * n, 10 * (n + 1)));
    Instances train = new Instances(resampled, 1);
    Instances test = new Instances(resampled, 1);
    for (int i = 0; i < resampled.numInstances(); i++) {
      Instance a = resampled.instance(i);
      if (testIds.contains(Integer.parseInt(a.stringValue(a.attribute(resampled.attribute(Q_ID).index()))))) {
        test.add(a);
      } else {
        train.add(a);
      }
    }
    // build and evaluate classifier
    if (config.getWriteSvmModel()) {
      svm.setModelFile(new File("C:\\tmp\\output\\model_" + config.getPercent() + "_percent_fold_" + n));
    }
    Classifier clsCopy = FilteredClassifier.makeCopy(filteredClassifier);
    clsCopy.buildClassifier(train);
    //extract matches
    for (int i = 0; i < test.size(); i++) {
      Instance instance = test.get(i);
      String match = train.classAttribute().value(0);
      String predictedClass = train.classAttribute().value((int) clsCopy.classifyInstance(instance));
      if (match.equals(predictedClass)) {
        String extraction =
          instance.stringValue(instance.attribute(train.attribute(Q_ID).index())) + ","
            + "\"" + instance.stringValue(instance.attribute(train.attribute(TITLE).index())).replaceAll("\\s", "_") + "\","
            + "\"" + instance.stringValue(instance.attribute(train.attribute(IDENTIFIER).index())) + "\","
            + "\"" + instance.stringValue(instance.attribute(train.attribute(DEFINIEN).index())).toLowerCase() + "\"";
        extractions.add(extraction);
      }
    }
    Evaluation eval = new Evaluation(resampled);
    eval.setPriors(train);
    eval.evaluateModel(clsCopy, test);
    averagePrecision[n] = eval.precision(0);
    averageRecall[n] = eval.recall(0);
    accuracy[n] = eval.pctCorrect() / 100d;
    StringBuilder b = new StringBuilder();
    b.append(", fold, ").append(n).append("\n").append(eval.toClassDetailsString()).append("\n").append(eval.toSummaryString(true));
    text[n] = b.toString();
    //}
  }
}
