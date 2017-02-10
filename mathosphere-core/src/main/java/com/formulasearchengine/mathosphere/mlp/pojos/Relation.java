package com.formulasearchengine.mathosphere.mlp.pojos;


import com.formulasearchengine.mathosphere.mlp.text.PosTag;

import org.apache.flink.api.java.tuple.Tuple2;

import java.util.Map;

import static com.formulasearchengine.mathosphere.mlp.text.WikiTextUtils.deLinkify;

public class Relation implements Comparable<Relation> {

  public double getDistanceFromFirstIdentifierOccurence() {
    return distanceFromFirstIdentifierOccurence;
  }

  public void setDistanceFromFirstIdentifierOccurence(double distanceFromFirstIdentifierOccurence) {
    this.distanceFromFirstIdentifierOccurence = distanceFromFirstIdentifierOccurence;
  }

  /**
   * The distance from the definiens distraction sentence to the sentence where the identifier occurs for the first time.
   * 0 if the definiens is found in the same sentence as the first occurrence of the identifier.
   * Normalized with the length of the document in sentences.
   */
  private double distanceFromFirstIdentifierOccurence;
  private String identifier;
  private String definition;
  /**
   * The calculated score.
   */
  private double score;

  /**
   * Relative term frequency of the definiens in the document.
   */
  private double relativeTermFrequency;

  private int identifierPosition;
  /**
   * Position of the definiens.
   */
  private int wordPosition;
  /**
   * The sentence containing the relation.
   */
  private Sentence sentence;
  /**
   * The relevance score, when compared to e.g. a gold standard.
   * 0 = irrelevant; 1 = partly relevant; 2 = correct definiens.
   */
  private Integer relevance = 0;

  public Relation() {
  }

  public Relation(String identifier, String definition) {
    this.identifier = identifier;
    this.definition = definition;
  }

  public Relation(Object json) {
    Map item = (Map) json;
    identifier = (String) item.get("identifier");
    definition = (String) item.get("top_definition");
    score = (double) item.get("top_definition_score");
  }

  public Double getScore() {
    return score;
  }

  public void setScore(double score) {
    this.score = score;
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public String getDefinition() {
    return definition;
  }

  public void setDefinition(Word word) {
    if (word.getPosTag().equals(PosTag.LINK)) {
      this.definition = "[[" + word.getWord() + "]]";
    } else {
      this.definition = word.getWord();
    }
  }

  public void setDefinition(String definition) {
    this.definition = definition;
  }

  public Sentence getSentence() {
    return sentence;
  }

  public void setSentence(Sentence sentence) {
    this.sentence = sentence;
  }

  public int getIdentifierPosition() {
    return identifierPosition;
  }

  public void setIdentifierPosition(int identifierPosition) {
    this.identifierPosition = identifierPosition;
  }

  public int getWordPosition() {
    return wordPosition;
  }

  public void setWordPosition(int wordPosition) {
    this.wordPosition = wordPosition;
  }

  @Override
  public String toString() {
    return "Relation [" + identifier + ", score=" + score + ", word=" + definition + "]";
  }

  @Override
  public int compareTo(Relation o) {
    return (o.getScore()).compareTo(getScore());
  }


  public int compareToName(Relation o) {
    int res = getIdentifier().compareTo(o.getIdentifier());
    if (res == 0) {
      res = getDefinition().compareToIgnoreCase(o.getDefinition());
    }
    if (res == 0) {
      res = countLowerCaseChars(o.getDefinition()).compareTo(countLowerCaseChars(getDefinition()));
    }
    return res;
  }

  public int compareNameScore(Relation o) {
    int res = getIdentifier().compareTo(o.getIdentifier());
    if (res == 0) {
      res = getDefinition().compareToIgnoreCase(o.getDefinition());
    }
    if (res == 0) {
      res = compareTo(o); //Sort by score desc
    }
    return res;
  }

  private Integer countLowerCaseChars(String s) {
    int upperCase = 0;
    for (int k = 0; k < s.length(); k++) {
      if (Character.isUpperCase(s.charAt(k))) upperCase++;

    }
    return upperCase;
  }

  public void setDefinition(Word word, ParsedWikiDocument doc) {
    this.definition = deLinkify(word, doc);
  }

  public Integer getRelevance() {
    return relevance;
  }

  public Relation setRelevance(int relevance) {
    this.relevance = relevance;
    return this;
  }

  public Tuple2<String, String> getTuple() {
    return new Tuple2<>(identifier, definition);
  }

  public double getRelativeTermFrequency() {
    return relativeTermFrequency;
  }

  public void setRelativeTermFrequency(double termFrequency) {
    this.relativeTermFrequency = termFrequency;
  }
}
