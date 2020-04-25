package com.formulasearchengine.mathosphere.mlp.pojos;

import java.util.*;

/**
 * A sentence is a list of words. For convenience, it contains information about all special tokens
 */
public class Sentence {
  private final int section;
  private final List<Word> words;
  private final Set<String> sentenceIdentifier;
  private final Set<MathTag> sentenceMath;

  public Sentence(List<Word> words, Set<String> identifier, Set<MathTag> formulae) {
    this(0, words, identifier, formulae);
  }

  public Sentence(int section, List<Word> words, Set<String> identifier, Set<MathTag> formulae) {
    this.section = section;
    this.words = words;
    this.sentenceIdentifier = identifier;
    this.sentenceMath = formulae;
  }

  public List<Word> getWords() {
    return words;
  }

  public boolean containsIdentifier(String identifier) {
    return sentenceIdentifier.contains(identifier);
  }

  public int getSection() {
    return section;
  }

  public Set<String> getIdentifiers() {
    return sentenceIdentifier;
  }

  public Set<MathTag> getMath() {
    return sentenceMath;
  }

  @Override
  public String toString() {
    return "Sentence [words=" + words + ", identifiers=" + sentenceIdentifier + "]";
  }

}
