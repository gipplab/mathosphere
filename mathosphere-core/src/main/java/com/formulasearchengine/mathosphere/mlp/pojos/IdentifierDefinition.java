package com.formulasearchengine.mathosphere.mlp.pojos;


/**
 * Created by Leo on 04.10.2016.
 */
public class IdentifierDefinition {
  private String identifier;

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public String getDefinition() {
    return definition;
  }

  public void setDefinition(String definition) {
    this.definition = definition;
  }

  private String definition;

  private double score, distanceFromFirstOccurrence;

  private int identifierPosition, wordPosition;

  public double getScore() {
    return score;
  }

  public void setScore(double score) {
    this.score = score;
  }

  public double getDistanceFromFirstOccurrence() {
    return distanceFromFirstOccurrence;
  }

  public void setDistanceFromFirstOccurrence(double distanceFromFirstOccurrence) {
    this.distanceFromFirstOccurrence = distanceFromFirstOccurrence;
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

  public IdentifierDefinition() {
  }

  public IdentifierDefinition(String identifier, String definition) {
    this.identifier = identifier;
    this.definition = definition.toLowerCase();
  }

  public IdentifierDefinition(String identifier, String definition,
                              double score, double distanceFromFirstOccurrence,
                              int identifierPosition, int wordPosition ){
    this(identifier, definition);
    this.score = score;
    this.distanceFromFirstOccurrence = distanceFromFirstOccurrence;
    this.identifierPosition = identifierPosition;
    this.wordPosition = wordPosition;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    IdentifierDefinition that = (IdentifierDefinition) o;

    if (getIdentifier() != null ? !getIdentifier().equals(that.getIdentifier()) : that.getIdentifier() != null)
      return false;
    return getDefinition() != null ? getDefinition().equals(that.getDefinition()) : that.getDefinition() == null;

  }

  @Override
  public int hashCode() {
    int result = getIdentifier() != null ? getIdentifier().hashCode() : 0;
    result = 31 * result + (getDefinition() != null ? getDefinition().hashCode() : 0);
    return result;
  }
}
