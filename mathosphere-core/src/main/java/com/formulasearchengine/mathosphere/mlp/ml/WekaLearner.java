package com.formulasearchengine.mathosphere.mlp.ml;

import com.formulasearchengine.mathosphere.mlp.cli.MachineLearningDefinienExtractionConfig;
import com.formulasearchengine.mathosphere.mlp.pojos.EvaluationResult;
import com.formulasearchengine.mathosphere.mlp.pojos.WikiDocumentOutput;
import com.formulasearchengine.mlp.evaluation.Evaluator;
import com.formulasearchengine.mlp.evaluation.pojo.GoldEntry;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import org.apache.commons.io.FileUtils;
import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.api.common.functions.util.ListCollector;
import org.apache.flink.util.Collector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.core.tokenizers.NGramTokenizer;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

import static com.formulasearchengine.mathosphere.mlp.ml.WekaUtils.*;
import static java.util.stream.Collectors.toList;
import static weka.core.Range.indicesToRangeList;

/**
 * Created by Leo on 23.12.2016.
 */
public class WekaLearner implements GroupReduceFunction<WikiDocumentOutput, EvaluationResult> {

  private static final Logger LOG = LogManager.getLogger( WekaLearner.class.getName() );

  private static final int folds = 10;
  private static final int totalQids = 100;
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
  public static final Double[] C_coarse = {Math.pow(2, -7), Math.pow(2, -5), Math.pow(2, -3), Math.pow(2, -1), Math.pow(2, 1), Math.pow(2, 3), Math.pow(2, 5), Math.pow(2, 7), Math.pow(2, 9), Math.pow(2, 11), Math.pow(2, 13), Math.pow(2, 15)};

  /**
   * Gamma.
   */
  public static final Double[] Y_coarse = {Math.pow(2, -15), Math.pow(2, -13), Math.pow(2, -11), Math.pow(2, -9), Math.pow(2, -7), Math.pow(2, -5), Math.pow(2, -3), Math.pow(2, -1), Math.pow(2, 1), Math.pow(2, 3)};

  /**
   * Cost of the interesting region.
   */
  public static final Double[] C_fine = {
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
  public static final Double[] Y_fine = {
    Math.pow(2, -7)
    , Math.pow(2, -6.75), Math.pow(2, -6.5), Math.pow(2, -6.25), Math.pow(2, -6)
    , Math.pow(2, -5.75), Math.pow(2, -5.5), Math.pow(2, -5.25), Math.pow(2, -5)
    , Math.pow(2, -4.75), Math.pow(2, -4.5), Math.pow(2, -4.25), Math.pow(2, -4)
    , Math.pow(2, -3.75), Math.pow(2, -3.5), Math.pow(2, -3.25), Math.pow(2, -3)
    , Math.pow(2, -2.75), Math.pow(2, -2.5), Math.pow(2, -2.25), Math.pow(2, -2)
  };

  /**
   * Cost for highest accuracy in the SVM.
   * c = 0.074325445
   */
  public static final Double[] C_best_accuracy = {Math.pow(2, -3.75)};

  /**
   * Gamma for highest accuracy in the SVM.
   * γ = 0.026278013
   */
  public static final Double[] Y_best_accuracy = {Math.pow(2, -5.25)};

  /**
   * Cost for highest recall in the evaluation. (Post SVM evaluation metric.)
   * tp: 77	fn: 233	fp: 110
   * c = 0.074325445
   */
  public static final Double[] C_best_recall = {Math.pow(2, -3.75)};
  /**
   * Gamma for highest recall in the evaluation. (Post SVM evaluation metric.)
   * tp: 77	fn: 233	fp: 110
   * γ = 0.011048544
   */
  public static final Double[] Y_best_recall = {Math.pow(2, -6.5)};

  /**
   * Cost for highest F1  in the evaluation. (Post SVM evaluation metric.)
   * tp: 70	fn: 240	fp: 44
   * c = 0.4204482076
   */
  public static final Double[] C_best_F1 = {Math.pow(2, -1.25)};
  /**
   * Gamma for highest F1 in the evaluation. (Post SVM evaluation metric.)
   * tp: 70	fn: 240	fp: 44
   * γ = 0.018581361
   */
  public static final Double[] Y_best_F1 = {Math.pow(2, -5.75)};
  public static final String INSTANCES_ARFF_FILE_NAME = "/instances.arff";

  private final ArrayList<GoldEntry> gold;

  public WekaLearner(MachineLearningDefinienExtractionConfig config) throws IOException {
    this.config = config;
    this.gold = (new Evaluator()).readGoldEntries(new File(config.getGoldFile()));
  }

  public final MachineLearningDefinienExtractionConfig config;

  @Override
  public void reduce(Iterable<WikiDocumentOutput> values, Collector<EvaluationResult> out) throws Exception {
    Instances instances;
    DependencyParser parser = DependencyParser.loadFromModelFile(config.dependencyParserModel());
    WekaUtils wekaUtils = new WekaUtils();
    instances = wekaUtils.createInstances("AllRelations");
    for (WikiDocumentOutput value : values) {
      wekaUtils.addRelationsToInstances(parser, value.getRelations(), value.getTitle(), value.getqId(), instances, value.getMaxSentenceLength());
    }
    if (config.isWriteInstances()) {
      File instancesFile = new File(config.getOutputDir() + INSTANCES_ARFF_FILE_NAME);
      ArffSaver arffSaver = new ArffSaver();
      arffSaver.setFile(instancesFile);
      arffSaver.setInstances(instances);
      arffSaver.writeBatch();
    }
    //do model once with all data
    if (config.getWriteSvmModel()) {
      generateAndWriteFullModel(instances);
    }
    process(out, instances);
  }

  /**
   * Generate the model with all data and write it with the appropriate filters.
   *
   * @param instances as returned from {@link WekaUtils#createInstances(String)}
   * @throws Exception
   */
  private void generateAndWriteFullModel(Instances instances) throws Exception {
    StringToWordVector stringToWordVector = getStringToWordVectorFilter(instances);
    Instances stringsReplacedData = Filter.useFilter(instances, stringToWordVector);
    Instances resampled = dumbResample(stringsReplacedData);
    Remove removeFilter = getRemoveFilter(stringsReplacedData);
    LibSVM svmForOut = new LibSVM();
    svmForOut.setCost(config.getSvmCost().get(0));
    svmForOut.setGamma(config.getSvmGamma().get(0));
    FilteredClassifier filteredClassifierForOut = new FilteredClassifier();
    filteredClassifierForOut.setClassifier(svmForOut);
    filteredClassifierForOut.setFilter(removeFilter);
    filteredClassifierForOut.buildClassifier(resampled);
    weka.core.SerializationHelper.write(config.getOutputDir() + "/svm_model_c_" + config.getSvmCost().get(0) + "_gamma_" + config.getSvmGamma().get(0) + ".model", filteredClassifierForOut);
    weka.core.SerializationHelper.write(config.getOutputDir() + "/string_filter_c_" + config.getSvmCost().get(0) + "_gamma_" + config.getSvmGamma().get(0) + ".model", stringToWordVector);
  }

  public List<EvaluationResult> processFromInstances() throws Exception {
    BufferedReader reader =
      new BufferedReader(new FileReader(config.getInstancesFile()));
    ArffLoader.ArffReader arff = new ArffLoader.ArffReader(reader);
    Instances instances;
    instances = arff.getData();
    instances.setClassIndex(instances.numAttributes() - 1);
    ArrayList<EvaluationResult> evaluationResults = new ArrayList<>();
    //wrap
    Collector<EvaluationResult> c = new ListCollector<>(evaluationResults);
    process(c, instances);
    return evaluationResults;
  }

  /**
   * Do the pre-processing, training and testing.
   *
   * @param out       the result of the testing
   * @param instances the instances to use for the testing and training.
   * @throws Exception
   */
  private void process(Collector<EvaluationResult> out, Instances instances) throws Exception {
    if (config.isCoarseSearch()) {
      config.setSvmCost(Arrays.asList(WekaLearner.C_coarse));
      config.setSvmGamma(Arrays.asList(WekaLearner.Y_coarse));
    } else if (config.isFineSearch()) {
      config.setSvmCost(Arrays.asList(WekaLearner.C_fine));
      config.setSvmGamma(Arrays.asList(WekaLearner.Y_fine));
    }
    List<Double> percentages = config.getPercent();
    List<Double> C_used = config.getSvmCost();
    List<Double> Y_used = config.getSvmGamma();
    File output = new File(config.getOutputDir() + "/svm_cross_eval_statistics.csv");
    File outputDetails = new File(config.getOutputDir() + "/svm_cross_eval_detailed_statistics.txt");
    File extractedDefiniens = new File(config.getOutputDir() + "/classifications.csv");

    StringToWordVector stringToWordVector = getStringToWordVectorFilter(instances);
    Instances stringsReplacedData = Filter.useFilter(instances, stringToWordVector);

    Remove removeFilter = getRemoveFilter(stringsReplacedData);
    removeFilter.setInputFormat(stringsReplacedData);

    FileUtils.deleteQuietly(output);
    FileUtils.deleteQuietly(outputDetails);
    FileUtils.deleteQuietly(extractedDefiniens);
    Double[] oversample = new Double[]{0d};//, 10d, 20d, 50d, 70d, 100d, 120d, 150d};
    List<Double[]> parameters = new ArrayList<>();
    for (double p : percentages) {
      for (double c : C_used) {
        for (double y : Y_used) {
          for (double o : oversample)
            parameters.add(new Double[]{p, c, y, o});
        }
      }
    }
    ForkJoinPool forkJoinPool = new ForkJoinPool(config.getParallelism());
    Stream<EvaluationResult> a = parameters.parallelStream().map(
      parameter -> crossEvaluate(stringsReplacedData, removeFilter, parameter[0], parameter[1], parameter[2], parameter[3]));
    Callable<List<EvaluationResult>> task = () -> a.collect(toList());
    List<EvaluationResult> evaluationResults = forkJoinPool.submit(task).get();
    for (EvaluationResult evaluationResult : evaluationResults) {
      FileUtils.write(outputDetails, "Cost; " + Utils.doubleToString(evaluationResult.cost, 10) + "; gamma; " + Utils.doubleToString(evaluationResult.gamma, 10) + "\n" + Arrays.toString(evaluationResult.text) + "\n", true);
      //remove duplicates from extraction
      StringBuilder e = new StringBuilder();
      Set<String> set = new HashSet<>();
      set.addAll(evaluationResult.extractions);
      List<String> list = new ArrayList<>(set);
      list.sort(Comparator.naturalOrder());
      for (String extraction : list) {
        e.append(extraction).append("\n");
      }
      Evaluator evaluator = new Evaluator();
      StringReader reader = new StringReader(e.toString());
      evaluationResult.setScoreSummary(evaluator.evaluate(evaluator.readExtractions(reader, gold, false), gold));
      //Output files
      FileUtils.write(extractedDefiniens, "Cost; "
        + Utils.doubleToString(evaluationResult.cost, 10)
        + "; gamma; " + Utils.doubleToString(evaluationResult.gamma, 10)
        + "; percentage_of_data_used; " + evaluationResult.percent
        + "\n", true);
      FileUtils.write(extractedDefiniens, e.toString(), true);
      FileUtils.write(output, evaluationResult.toString() + "\n", true);
      out.collect(evaluationResult);
    }
  }

  private StringToWordVector getStringToWordVectorFilter(Instances instances) throws Exception {
    StringToWordVector stringToWordVector = new StringToWordVector();
    stringToWordVector.setAttributeIndices(indicesToRangeList(new int[]{
      instances.attribute(SURFACE_TEXT_AND_POS_TAG_OF_TWO_PRECEDING_AND_FOLLOWING_TOKENS_AROUND_THE_DESC_CANDIDATE).index(),
      instances.attribute(SURFACE_TEXT_AND_POS_TAG_OF_THREE_PRECEDING_AND_FOLLOWING_TOKENS_AROUND_THE_PAIRED_MATH_EXPR).index(),
      instances.attribute(SURFACE_TEXT_OF_THE_FIRST_VERB_THAT_APPEARS_BETWEEN_THE_DESC_CANDIDATE_AND_THE_TARGET_MATH_EXPR).index(),
      instances.attribute(SURFACE_TEXT_AND_POS_TAG_OF_DEPENDENCY_WITH_LENGTH_3_FROM_IDENTIFIER).index(),
      instances.attribute(SURFACE_TEXT_AND_POS_TAG_OF_DEPENDENCY_WITH_LENGTH_3_FROM_DEFINIEN).index()}));
    stringToWordVector.setWordsToKeep(1000);
    NGramTokenizer nGramTokenizer = new NGramTokenizer();
    nGramTokenizer.setNGramMaxSize(3);
    nGramTokenizer.setNGramMinSize(1);
    nGramTokenizer.setDelimiters(nGramTokenizer.getDelimiters().replaceAll(":", ""));
    stringToWordVector.setTokenizer(nGramTokenizer);
    stringToWordVector.setInputFormat(instances);
    return stringToWordVector;
  }

  private Remove getRemoveFilter(Instances instances) throws Exception {
    Remove removeFilter = new Remove();
    removeFilter.setAttributeIndices(indicesToRangeList(new int[]{
      instances.attribute(TITLE).index(),
      instances.attribute(IDENTIFIER).index(),
      instances.attribute(DEFINIEN).index(),
      instances.attribute(Q_ID).index(),
    }));
    removeFilter.setInputFormat(instances);
    return removeFilter;
  }

  private EvaluationResult crossEvaluate(Instances stringsReplacedData, Remove removeFilter, double percent, double cost, double gamma, double oversample) {
    try {
      System.out.println("Cost; " + Utils.doubleToString(cost, 10)
        + "; gamma; " + Utils.doubleToString(gamma, 10));
      EvaluationResult result = new EvaluationResult(config.isLeaveOneOutEvaluation() ? totalQids : folds, percent, cost, gamma);
      result.prefix = "oversample; " + oversample;
      Instances reduced;
      if (percent != 100) {
        //draw random sample, careful, this actually has an effect, even for setSampleSizePercent(100) and setBiasToUniformClass(0)
        reduced = downsample(stringsReplacedData, percent);
      } else {
        reduced = stringsReplacedData;
      }
      Instances resampled = resample(oversample, reduced);
      int counter = 0;
      while (10 * counter < totalQids) {
        for (int n = 0; n < folds; n++) {
          trainAndTest(10 * counter + n, removeFilter, cost, gamma, stringsReplacedData, resampled, result);
        }
        if (!config.isLeaveOneOutEvaluation()) {
          break;
        }
        counter++;
      }
      return result;
    } catch (Exception e) {
      LOG.error("Error during the Weka machine learning algorithm. Reason: " + e.getMessage(), e);
      return new EvaluationResult(folds, percent, cost, gamma);
    }
  }

  private Instances resample(double oversample, Instances reduced) throws Exception {
    Instances resampled;

    //oversampling to deal with the ratio of the classes
    if (true) {
      resampled = dumbResample(reduced);
    } else {
      resampled = smote(reduced, oversample);
    }
    return resampled;
  }

  private Instances downsample(Instances stringsReplacedData, double percent) throws Exception {
    Instances reduced;
    Resample sampler = new Resample();
    sampler.setRandomSeed(1);
    //do not change distribution
    sampler.setBiasToUniformClass(0);
    sampler.setSampleSizePercent(percent);
    sampler.setInputFormat(stringsReplacedData);
    reduced = Filter.useFilter(stringsReplacedData, sampler);
    return reduced;
  }

  private Instances dumbResample(Instances reduced) throws Exception {
    Resample resampleFilter = new Resample();
    resampleFilter.setRandomSeed(1);
    resampleFilter.setBiasToUniformClass(1);
    resampleFilter.setInputFormat(reduced);
    return Filter.useFilter(reduced, resampleFilter);
  }

  private Instances smote(Instances stringsReplacedData, double oversample) throws Exception {
    Instances resampled;
    SMOTE smote = getSmoteFilter(stringsReplacedData, oversample);
    resampled = Filter.useFilter(stringsReplacedData, smote);
    return resampled;
  }

  private SMOTE getSmoteFilter(Instances stringsReplacedData, double oversample) throws Exception {
    SMOTE smote = new SMOTE();
    smote.setRandomSeed(1);
    smote.setPercentage(oversample);
    smote.setNearestNeighbors(5);
    smote.setInputFormat(stringsReplacedData);
    return smote;
  }

  /**
   * @param n                fold.
   * @param removeFilter     the filter that removes the string attributes title, qid, identifier and definiens.
   * @param cost             cost for the svm.
   * @param gamma            gamma for the svm.
   * @param beforeResampling plain data for test set generation, strings replaced.
   * @param resampled        resampled training data.
   * @param result           for returning the results.
   * @throws Exception weka may throw.
   */
  private void trainAndTest(int n, Filter removeFilter, double cost, double gamma, Instances beforeResampling, Instances resampled,
                            EvaluationResult result) throws Exception {
    LibSVM svm = new LibSVM();
    svm.setCost(cost);
    svm.setGamma(gamma);
    FilteredClassifier filteredClassifier = new FilteredClassifier();
    filteredClassifier.setClassifier(svm);
    filteredClassifier.setFilter(removeFilter);
    List<Integer> testIds;
    if (config.isLeaveOneOutEvaluation()) {
      testIds = new ArrayList<>();
      testIds.add(n);
    } else {
      testIds = Arrays.asList(Arrays.copyOfRange(rand, folds * n, folds * (n + 1)));
    }
    Instances train = new Instances(resampled, 1);
    Instances test = new Instances(beforeResampling, 1);
    //build test and training set independently
    for (int i = 0; i < resampled.numInstances(); i++) {
      Instance a = resampled.instance(i);
      if (!testIds.contains(Integer.parseInt(a.stringValue(a.attribute(resampled.attribute(Q_ID).index()))))) {
        train.add(a);
      }
    }
    for (int i = 0; i < beforeResampling.numInstances(); i++) {
      Instance a = beforeResampling.instance(i);
      if (testIds.contains(Integer.parseInt(a.stringValue(a.attribute(beforeResampling.attribute(Q_ID).index()))))) {
        //from unresampled data for accurate accuracy predictions
        test.add(a);
      }
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
        result.extractions.add(extraction);
      }
    }
    Evaluation eval = new Evaluation(resampled);
    eval.setPriors(train);
    eval.evaluateModel(clsCopy, test);
    result.averagePrecision[n] = eval.precision(0);
    result.averageRecall[n] = eval.recall(0);
    result.accuracy[n] = eval.pctCorrect() / 100d;
    StringBuilder b = new StringBuilder();
    b.append(", fold, ").append(n).append("\n").append(eval.toClassDetailsString()).append("\n").append(eval.toSummaryString(true));
    result.text[n] = b.toString();
  }
}
