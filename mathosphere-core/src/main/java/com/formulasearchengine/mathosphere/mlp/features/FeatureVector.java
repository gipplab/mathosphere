package com.formulasearchengine.mathosphere.mlp.features;

import java.util.Arrays;

/**
 * Created by Leo on 18.11.2016.
 */
public class FeatureVector implements Comparable {
  public final String identifier;
  public final String definition;
  public final double[] values;
  public double truePositive = -1d;
  private Integer qId;

  public FeatureVector(String identifier, String definition, double[] values) {
    this.identifier = identifier;
    this.definition = definition;
    this.values = values;
  }

  public FeatureVector(String identifier, String definition, int numberOfFeatures) {
    this(identifier, definition, new double[numberOfFeatures]);
    Arrays.fill(values, -1d);
  }

  public FeatureVector setFeature(Integer feature, double value) {
    values[feature] = value;
    return this;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FeatureVector feature = (FeatureVector) o;

    if (!identifier.equals(feature.identifier)) return false;
    return definition.equals(feature.definition);

  }

  @Override
  public int hashCode() {
    int result = identifier.hashCode();
    result = 31 * result + definition.hashCode();
    return result;
  }

  @Override
  public int compareTo(Object o) {
    FeatureVector f = (FeatureVector) o;
    int temp = identifier.compareTo(f.identifier);
    if (temp != 0) return temp;
    return definition.compareTo(f.definition);
  }

  @Override
  public String toString() {
    return new StringBuilder().append("qId ").append(getqId()).append("; label ").append(truePositive).append("; identifier ").append(identifier).append(" ").append(definition).append(" ").append(Arrays.toString(values)).toString();
  }

  public Integer getqId() {
    return qId;
  }

  public void setqId(Integer qId) {
    this.qId = qId;
  }
}
