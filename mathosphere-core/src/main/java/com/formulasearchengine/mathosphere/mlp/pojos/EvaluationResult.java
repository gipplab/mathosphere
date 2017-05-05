package com.formulasearchengine.mathosphere.mlp.pojos;

import com.formulasearchengine.mathosphere.mlp.ml.WekaUtils;
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
  public static final float NUMBER_OF_GOLD_ENTRIES = 310f;
  public final double[] averagePrecision;
  public final double[] averageRecall;
  public final double[] accuracy;
  public final String[] text;
  public String prefix = "";

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
    return prefix + "Cost; " + Utils.doubleToString(cost, 10)
      + "; gamma; " + Utils.doubleToString(gamma, 10)
      + "; percentage_of_data_used; " + percent
      + "; 10-folds_accuracy; " + Arrays.toString(accuracy).replaceAll("\\[|\\]", "").replaceAll(",", ";")
      + "; avg_10-folds_accuracy; " + average(accuracy)
      + "; 10-folds_prec; " + Arrays.toString(averagePrecision).replaceAll("\\[|\\]", "").replaceAll(",", ";")
      + "; avg_10-folds_prec; " + average(averagePrecision)
      + "; 10-folds_recall; " + Arrays.toString(averageRecall).replaceAll("\\[|\\]", "").replaceAll(",", ";")
      + "; avg_10-folds_recall; " + average(averageRecall)
      + "; avg_10-folds_F1; " + getF1()
      + "; tp; " + getScoreSummary().tp
      + "; fn; " + getScoreSummary().fn
      + "; fp; " + getScoreSummary().fp
      + "; precision for " + NUMBER_OF_GOLD_ENTRIES + " gold entries; " + getPrecision()
      + "; recall for " + NUMBER_OF_GOLD_ENTRIES + " gold entries; " + getRecall()
      + "; f1 for " + NUMBER_OF_GOLD_ENTRIES + " gold entries; " + calcF1(getPrecision(), getRecall());
  }

  protected double getRecall() {
    return (double) getScoreSummary().tp / NUMBER_OF_GOLD_ENTRIES;
  }

  protected double getPrecision() {
    return (double) getScoreSummary().tp / (getScoreSummary().tp + getScoreSummary().fp);
  }

  protected double getF1() {
    return calcF1(WekaUtils.average(averagePrecision), WekaUtils.average(averageRecall));
  }

  protected double calcF1(double precision, double recall) {
    return 2 * precision * recall / (precision + recall);
  }
}
