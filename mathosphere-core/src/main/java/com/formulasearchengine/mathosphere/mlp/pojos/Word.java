package com.formulasearchengine.mathosphere.mlp.pojos;

import com.formulasearchengine.mathosphere.mlp.text.PosTag;

import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.Objects;

public class Word {

  private String word;
  private String lemma;
  private String posTag;
  private String originalPosTag;

  private Position position;

  public Word() {
  }

  public Word(String word, String posTag) {
    this(new Position(0), word, posTag);
  }

  public Word(Position position, String word, String posTag) {
    this(position, word, word, posTag);
  }

  public Word(Position position, String word, String lemma, String posTag) {
    this.position = position;
    this.word = word;
    this.lemma = lemma;
    this.posTag = posTag;
    this.originalPosTag = posTag;
  }

  public Word setOriginalPosTag(String originalPosTag) {
    this.originalPosTag = originalPosTag;
    return this;
  }

  public String getOriginalPosTag() {
    return originalPosTag;
  }

  public String getLemma() {
    return lemma;
  }

  public Word setLemma(String lemma) {
    this.lemma = lemma;
    return this;
  }

  @Override
  public String toString() {
    return "'" + word + "':" + posTag;
  }

  public String getWord() {
    return word;
  }

  public String getPosTag() {
    return posTag;
  }

  public Position getPosition(){
    return position;
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getWord(), posTag);
  }

  public String toLowerCase() {
    if (PosTag.IDENTIFIER.equals(posTag)) {
      return word;
    } else {
      return word.toLowerCase();
    }
  }

}
