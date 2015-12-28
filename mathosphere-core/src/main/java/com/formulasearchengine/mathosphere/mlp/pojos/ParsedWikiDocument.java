package com.formulasearchengine.mathosphere.mlp.pojos;

import com.google.common.collect.Multiset;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParsedWikiDocument {

  private List<WikidataLink> links;
  private String title;
  private Multiset<String> identifiers;
  private List<Formula> formulas;
  private List<Sentence> sentences;
  private Map<String,WikidataLink> linkMap = null;

  public ParsedWikiDocument() {
  }

  public ParsedWikiDocument(String title, Multiset<String> identifiers, List<Formula> formulas,
                            List<Sentence> sentences) {
    this.title = title;
    this.identifiers = identifiers;
    this.formulas = formulas;
    this.sentences = sentences;
  }

  public ParsedWikiDocument(String title, Multiset<String> identifiers, List<Formula> formulas, List<Sentence> sentences, List<WikidataLink> links) {
    this.title = title;
    this.identifiers = identifiers;
    this.formulas = formulas;
    this.sentences = sentences;
    this.links = links;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public List<Sentence> getSentences() {
    return sentences;
  }

  public Multiset<String> getIdentifiers() {
    return identifiers;
  }

  public void setIdentifiers(Multiset<String> identifiers) {
    this.identifiers = identifiers;
  }

  public List<Formula> getFormulas() {
    return formulas;
  }

  public void setFormulas(List<Formula> formulas) {
    this.formulas = formulas;
  }

  public void setSentences(List<Sentence> sentences) {
    this.sentences = sentences;
  }

  public List<WikidataLink> getLinks() {
    return links;
  }

  public ParsedWikiDocument setLinks(List<WikidataLink> links) {
    this.links = links;
    this.linkMap = null;
    return this;
  }

  public Map<String, WikidataLink> getLinkMap() {
    if ( linkMap == null ){
      linkMap = new HashMap<>();
      for (WikidataLink i : links){
        linkMap.put(i.getContentHash(),i);
      }
    }
    return linkMap;
  }
}
