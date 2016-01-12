package com.formulasearchengine.mathosphere.mlp.pojos;


import com.formulasearchengine.mathosphere.mlp.text.PosTag;

import org.apache.flink.api.java.tuple.Tuple2;

import java.util.Map;

public class Relation implements Comparable<Relation> {

  private String identifier;
  private String definition;
  private double score;
  private int identifierPosition;
  private int wordPosition;
  private Sentence sentence;
  private Integer relevance;

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
    if ( res == 0 ){
      res = countLowerCaseChars(o.getDefinition()).compareTo(countLowerCaseChars(getDefinition()));
    }
    return res;
  }

  public int compareNameScore(Relation o) {
    int res = getIdentifier().compareTo(o.getIdentifier());
    if (res == 0) {
      res = getDefinition().compareToIgnoreCase(o.getDefinition());
    }
    if ( res == 0 ){
      res = compareTo(o); //Sort by score desc
    }
    return res;
  }
  private Integer countLowerCaseChars(String s){
    int upperCase = 0;
    for (int k = 0; k < s.length(); k++) {
      if (Character.isUpperCase(s.charAt(k))) upperCase++;

    }
    return upperCase;
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

  public Integer getRelevance() {
    return relevance;
  }

  public Relation setRelevance(int relevance) {
    this.relevance = relevance;
    return this;
  }

  public Tuple2<String,String> getTuple(){
    return new Tuple2<>(identifier,definition);
  }
}
