package com.formulasearchengine.mathosphere.mlp.ml;

import com.formulasearchengine.mlp.evaluation.pojo.ScoreSummary;
import weka.core.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.formulasearchengine.mathosphere.mlp.ml.WekaUtils.average;

/**
 * Created by Leo on 17.01.2017.
 */
public class EvaluationResult {
  public final double[] averagePrecision;
  public final double[] averageRecall;
  public final double[] accuracy;
  public final String[] text;

  public ScoreSummary getScoreSummary() {
    return scoreSummary;
  }

  public void setScoreSummary(ScoreSummary scoreSummary) {
    this.scoreSummary = scoreSummary;
  }

  private ScoreSummary scoreSummary;
  public final int folds;
  public final double percent;
  public final double cost;
  public final double gamma;
  public final List<String> extractions = new ArrayList<>();

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

  @Override
  public String toString() {
    return "Cost; " + Utils.doubleToString(cost, 10)
      + "; gamma; " + Utils.doubleToString(gamma, 10)
      + "; percentage_of_data_used; " + percent
      + "; accuracy; " + Arrays.toString(accuracy).replaceAll("\\[|\\]", "").replaceAll(",", ";")
      + "; avg_accuracy; " + average(accuracy)
      + "; prec; " + Arrays.toString(averagePrecision).replaceAll("\\[|\\]", "").replaceAll(",", ";")
      + "; avg_prec; " + average(averagePrecision)
      + "; recall; " + Arrays.toString(averageRecall).replaceAll("\\[|\\]", "").replaceAll(",", ";")
      + "; avg_recall; " + average(averageRecall)
      + "; avg_F1; " + getF1()
      + "; tp; " + getScoreSummary().tp
      + "; fn; " + getScoreSummary().fn
      + "; fp; " + getScoreSummary().fp;
  }

  public double getF1() {
    return 2 * WekaUtils.average(averageRecall) * WekaUtils.average(averagePrecision) / (WekaUtils.average(averageRecall) + WekaUtils.average(averagePrecision));
  }
}
