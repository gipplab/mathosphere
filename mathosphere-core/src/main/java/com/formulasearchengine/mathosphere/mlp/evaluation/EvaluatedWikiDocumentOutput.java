package com.formulasearchengine.mathosphere.mlp.evaluation;

import com.formulasearchengine.mathosphere.mlp.pojos.GoldEntry;
import com.formulasearchengine.mathosphere.mlp.pojos.IdentifierDefinition;
import com.formulasearchengine.mathosphere.mlp.pojos.WikiDocumentOutput;
import com.google.common.collect.Multiset;

import java.util.*;

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
  private GoldEntry gold;
  private double descriptionExtractionPrecision = 0;
  private boolean success = true;
  private Set<IdentifierDefinition> truePositiveDefinitions;
  private Set<IdentifierDefinition> falsePositiveDefinitions;
  private Set<IdentifierDefinition> falseNegativeDefinitions;

  public Set<IdentifierDefinition> getTruePositiveDefinitions() {
    return truePositiveDefinitions;
  }

  public void setTruePositiveDefinitions(Set<IdentifierDefinition> truePositiveDefinitions) {
    this.truePositiveDefinitions = truePositiveDefinitions;
  }

  public Set<IdentifierDefinition> getFalsePositiveDefinitions() {
    return falsePositiveDefinitions;
  }

  public void setFalsePositiveDefinitions(Set<IdentifierDefinition> falsePositiveDefinitions) {
    this.falsePositiveDefinitions = falsePositiveDefinitions;
  }

  public Set<IdentifierDefinition> getFalseNegativeDefinitions() {
    return falseNegativeDefinitions;
  }

  public void setFalseNegativeDefinitions(Set<IdentifierDefinition> falseNegativeDefinitions) {
    this.falseNegativeDefinitions = falseNegativeDefinitions;
  }

  /**
   * Get the gold standard for this WikiDocument
   *
   * @return
   */
  public GoldEntry getGold() {
    return gold;
  }

  /**
   * Set the gold standard for this WikiDocument
   */
  public void setGold(Map<String, Object> gold) {
    List<IdentifierDefinition> definitions = new ArrayList<>();
    Map<String, String> rawDefinitions = (Map<String, String>) gold.get("definitions");
    for (String identifier : rawDefinitions.keySet()) {
      List<String> defeniens = getDefiniens(rawDefinitions, identifier);
      for (String defenien : defeniens) {
        definitions.add(new IdentifierDefinition(identifier, defenien));
      }
    }
    Map<String, String> formula = (Map<String, String>) gold.get("formula");
    this.gold = new GoldEntry(formula.get("qID"), formula.get("oldId"), formula.get("fid"), formula.get("math_inputtex"), formula.get("title"), definitions);
  }

  /**
   * Set the gold standard for this WikiDocument
   */
  public void setGold(GoldEntry gold) {
    this.gold = gold;
  }

  public static List<String> getDefiniens(Map definitions, String identifier) {
    List<String> result = new ArrayList<>();
    List definiens = (List) definitions.get(identifier);
    for (Object definien : definiens) {
      if (definien instanceof Map) {
        Map<String, String> var = (Map) definien;
        for (Map.Entry<String, String> stringStringEntry : var.entrySet()) {
          // there is only one entry
          //remove everything in brackets
          final String def = stringStringEntry.getValue().trim().replaceAll("\\s*\\(.*?\\)$", "");
          result.add(def);
        }
      } else {
        result.add((String) definien);
      }
    }
    return result;
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
