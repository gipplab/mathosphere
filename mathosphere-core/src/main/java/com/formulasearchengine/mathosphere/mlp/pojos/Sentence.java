package com.formulasearchengine.mathosphere.mlp.pojos;

import java.util.List;
import java.util.Set;

public class Sentence {

  private List<Word> words;
  private Set<String> identifiers;
  private List<MathTag> formulas;

  public Sentence(List<Word> words, Set<String> identifiers, List<MathTag> formulas) {
    this.words = words;
    this.identifiers = identifiers;
    this.formulas = formulas;
  }

  public List<Word> getWords() {
    return words;
  }

  public boolean contains(String identifier) {
    return identifiers.contains(identifier);
  }

  public Set<String> getIdentifiers() {
    return identifiers;
  }

  public List<MathTag> getFormulas() {
    return formulas;
  }

  public void setFormulas(List<MathTag> formulas) {
    this.formulas = formulas;
  }

  @Override
  public String toString() {
    return "Sentence [words=" + words + ", identifiers=" + identifiers + "]";
  }

}
