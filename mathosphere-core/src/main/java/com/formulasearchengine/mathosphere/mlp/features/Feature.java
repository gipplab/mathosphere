package com.formulasearchengine.mathosphere.mlp.features;

import com.formulasearchengine.mathosphere.mlp.text.MyPatternMatcher;

/**
 * Created by Leo on 18.11.2016.
 */
public class Feature implements Comparable {
  public final String identifier;
  public final String definition;
  public final Integer feature;
  public final double value;

  public Feature(String identifier, String definition, Integer feature, double value) {
    this.identifier = identifier;
    this.definition = definition;
    this.feature = feature;
    this.value = value;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Feature feature = (Feature) o;

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
    Feature f = (Feature) o;
    int temp = identifier.compareTo(f.identifier);
    if (temp != 0) return temp;
    return definition.compareTo(f.definition);
  }
}
