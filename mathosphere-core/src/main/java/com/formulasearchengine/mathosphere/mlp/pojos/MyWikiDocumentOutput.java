package com.formulasearchengine.mathosphere.mlp.pojos;

import com.formulasearchengine.mathosphere.mlp.features.Feature;
import com.formulasearchengine.mathosphere.mlp.features.FeatureVector;
import com.google.common.collect.Multiset;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class MyWikiDocumentOutput {

  private String title;
  private List<FeatureVector> featureVectors;
  //private Set<Multiset.Entry<String>> identifiers;


  public boolean isSuccess() {
    return success;
  }

  private boolean success = true;

  public MyWikiDocumentOutput() {
  }

  public MyWikiDocumentOutput(boolean s) {
    this.success = s;
  }

  public MyWikiDocumentOutput(String title, List<FeatureVector> featureVectors, Multiset<String> identifiers) {
    this.title = title;
    this.featureVectors = featureVectors;
    //this.identifiers = identifiers.entrySet();
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Set<Multiset.Entry<String>> getIdentifiers() {
    return null;// identifiers;
  }

  public void setIdentifiers(Set<Multiset.Entry<String>> identifiers) {
    //this.identifiers = identifiers;
  }

  public List<FeatureVector> getFeatureVectors() {
    return featureVectors;
  }

  public void setFeatureVectors(List<FeatureVector> featureVectors) {
    this.featureVectors = featureVectors;
  }
}
