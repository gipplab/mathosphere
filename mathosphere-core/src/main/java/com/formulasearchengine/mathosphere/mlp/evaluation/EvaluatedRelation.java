package com.formulasearchengine.mathosphere.mlp.evaluation;


import com.formulasearchengine.mathosphere.mlp.pojos.*;
import com.formulasearchengine.mathosphere.mlp.text.PosTag;

public class EvaluatedRelation implements Comparable<EvaluatedRelation> {

  private String identifier;
  private String definition;

  private String goldDefinition;
  private double score;
  private int identifierPosition;
  private int wordPosition;
  private Sentence sentence;

  public EvaluatedRelation() {
  }

  public EvaluatedRelation(Relation relation) {
    identifier = relation.getIdentifier();
    definition = relation.getDefinition();
    score = relation.getScore();
    wordPosition = relation.getWordPosition();
    sentence = relation.getSentence();
    identifierPosition = relation.getIdentifierPosition();
  }

  /**
   * @return the gold standard definition; null if none set
   */
  public String getGoldDefinition() {
    return goldDefinition;
  }

  /**
   * Sets the gold-standard definition
   *
   * @param goldDefinition
   */
  public void setGoldDefinition(String goldDefinition) {
    this.goldDefinition = goldDefinition;
  }

  /**
   * Checks weather or not {@link #definition} and {@link #goldDefinition} are equal.
   *
   * @return true when the relations definition matches the gold standard, false otherwise or when no gold standard is set.
   */
  public boolean definitionMatchesGoldStandard() {
    return definition != null && definition.equals(goldDefinition);
  }

  /**
   * Checks weather or not {@link #definition} and {@link #goldDefinition} are equal. Strips [[ and ]] from both.
   *
   * @return true when the relations definition matches the gold standard, false otherwise or when no gold standard is set.
   */
  public boolean definitionMatchesGoldStandardNoLink() {
    if (definition == null || goldDefinition == null)
      return false;
    final String strippedDefinition = definition.replaceAll("(\\[\\[|\\]\\])", "");
    final String strippedGoldDefinition = goldDefinition.replaceAll("(\\[\\[|\\]\\])", "");
    return strippedDefinition.equals(strippedGoldDefinition);
  }

  public double getScore() {
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
  public int compareTo(EvaluatedRelation o) {
    return ((Double) getScore()).compareTo(o.getScore());
  }

  public void setDefinition(Word word, ParsedWikiDocument doc) {
    if (word.getPosTag().equals(PosTag.LINK)) {
      String hash = word.getWord().replaceAll("^LINK_", "");
      WikidataLink link = doc.getLinkMap().get(hash);
      if (link != null) {
        this.definition = "[[" + link.getContent() + "]]";
      } else {
        this.definition = "[[" + word.getWord() + "]]";
      }
    } else {
      this.definition = word.getWord();
    }
  }
}
