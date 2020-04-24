package com.formulasearchengine.mathosphere.mlp.pojos;

import com.google.common.collect.Multiset;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParsedWikiDocument {
  private String title;
  private Multiset<String> identifiers;
  private List<Sentence> sentences;
  private DocumentMetaLib lib;

  public ParsedWikiDocument() {
  }

  public ParsedWikiDocument(String title, Multiset<String> identifiers, List<Sentence> sentences, DocumentMetaLib lib) {
    this.title = title;
    this.identifiers = identifiers;
    this.lib = lib;
    this.sentences = sentences;
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

  public Map<String, MathTag> getFormulas() {
    return lib.getFormulaLib();
  }

  public void setFormulas(List<MathTag> formulas) {
    lib.setAllFormula(formulas);
  }

  public void setSentences(List<Sentence> sentences) {
    this.sentences = sentences;
  }

  public Map<String, SpecialToken> getLinkMap() {
    return lib.getLinkLib();
  }
}
