package com.formulasearchengine.mathosphere.mlp.ml;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Leo on 17.01.2017.
 */
public class EvaluationResult {
  public final double[] averagePrecision;
  public final double[] averageRecall;
  public final double[] accuracy;
  public final String[] text;
  public final int folds;
  public final double percent;
  public final double cost;
  public final double gamma;
  Collection<String> extractions = new ConcurrentLinkedQueue();

  public EvaluationResult(int folds, double percent, double cost, double gamma) {
    this.folds = folds;
    text = new String[folds];
    accuracy = new double[folds];
    averageRecall = new double[folds];
    averagePrecision = new double[folds];
    this.percent = percent;
    this.cost = cost;
    this.gamma = gamma;
  }

  public double getF1() {
    return 2 * WekaUtils.average(averageRecall) * WekaUtils.average(averagePrecision) / (WekaUtils.average(averageRecall) + WekaUtils.average(averagePrecision));
  }
}
