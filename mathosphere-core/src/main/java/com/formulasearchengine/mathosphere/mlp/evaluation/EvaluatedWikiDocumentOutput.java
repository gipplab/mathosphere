package com.formulasearchengine.mathosphere.mlp.evaluation;

import com.formulasearchengine.mathosphere.mlp.pojos.WikiDocumentOutput;
import com.google.common.collect.Multiset;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Wikipedia document that has ben analysed with the mlp pipeline and been evaluated against a gold standard.
 */
public class EvaluatedWikiDocumentOutput {

  private String title;
  private List<EvaluatedRelation> relations;
  private Set<Multiset.Entry<String>> identifiers;
  private double identifierExtractionRecall = 0;
  private double identifierExtractionPrecision = 0;
  private double descriptionExtractionRecall = 0;
  private Map<String, Object> gold;
  private double descriptionExtractionPrecision = 0;
  private boolean success = true;

  /**
   * Get the gold standard for this WikiDocument
   *
   * @return
   */
  public Map<String, Object> getGold() {
    return gold;
  }

  /**
   * Set the gold standard for this WikiDocument
   */
  public void setGold(Map<String, Object> gold) {
    this.gold = gold;
  }


  public double getDescriptionExtractionRecall() {
    return descriptionExtractionRecall;
  }

  public void setDescriptionExtractionRecall(double descriptionExtractionRecall) {
    this.descriptionExtractionRecall = descriptionExtractionRecall;
  }

  public double getDescriptionExtractionPrecision() {
    return descriptionExtractionPrecision;
  }

  public void setDescriptionExtractionPrecision(double descriptionExtractionPrecision) {
    this.descriptionExtractionPrecision = descriptionExtractionPrecision;
  }


  public double getIdentifierExtractionRecall() {
    return identifierExtractionRecall;
  }

  public void setIdentifierExtractionRecall(double identifierExtractionRecall) {
    this.identifierExtractionRecall = identifierExtractionRecall;
  }

  public double getIdentifierExtractionPrecision() {
    return identifierExtractionPrecision;
  }

  public void setIdentifierExtractionPrecision(double identifierExtractionPrecision) {
    this.identifierExtractionPrecision = identifierExtractionPrecision;
  }

  public boolean isSuccess() {
    return success;
  }

  public EvaluatedWikiDocumentOutput() {
  }

  public EvaluatedWikiDocumentOutput(WikiDocumentOutput wikiDocumentOutput) {
    title = wikiDocumentOutput.getTitle();
    identifiers = wikiDocumentOutput.getIdentifiers();
    success = wikiDocumentOutput.isSuccess();
  }

  public EvaluatedWikiDocumentOutput(boolean s) {
    this.success = s;
  }

  public EvaluatedWikiDocumentOutput(String title, List<EvaluatedRelation> relations, Multiset<String> identifiers) {
    this.title = title;
    this.relations = relations;
    this.identifiers = identifiers.entrySet();
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public List<EvaluatedRelation> getRelations() {
    return relations;
  }

  public void setRelations(List<EvaluatedRelation> relations) {
    this.relations = relations;
  }

  public Set<Multiset.Entry<String>> getIdentifiers() {
    return identifiers;
  }

  public void setIdentifiers(Set<Multiset.Entry<String>> identifiers) {
    this.identifiers = identifiers;
  }
}
