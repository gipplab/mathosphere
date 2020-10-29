package com.formulasearchengine.mathosphere.mlp.pojos;

import com.formulasearchengine.mathosphere.mlp.text.PosTag;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.GrammaticalStructure;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A sentence is a list of words. For convenience, it contains information about all special tokens
 */
public class Sentence {
  private final int section;
  private final List<Word> words;
  private final Set<String> sentenceIdentifier;
  private final Set<MathTag> sentenceMath;
  private GrammaticalStructure parseTree;
  private SemanticGraph graph;

  public Sentence(List<Word> words, Set<String> identifier, Set<MathTag> formulae) {
    this(0, words, identifier, formulae);
  }

  public Sentence(int section, List<Word> words, Set<String> identifier, Set<MathTag> formulae){
    this(section, words, identifier, formulae, null);
  }

  public Sentence(int section, List<Word> words, Set<String> identifier, Set<MathTag> formulae, GrammaticalStructure parseTree) {
    this.section = section;
    this.words = words;
    this.sentenceIdentifier = identifier;
    this.sentenceMath = formulae;
    this.parseTree = parseTree;
    if ( parseTree != null ) {
      this.graph = new SemanticGraph(parseTree.typedDependencies());
    }
  }

  public List<Word> getWords() {
    return words;
  }

  public List<Word> getNouns() {
    List<Word> nouns = new LinkedList<>();
    for ( int i = 0; i < words.size(); i++ ) {
      Word w = words.get(i);
      // if it is an -RBL- or something similar, we ignore that
      if ( w.getPosTag().matches(PosTag.DEFINIEN_REGEX) && !w.getWord().matches("^-.*-$") ) {
        nouns.add(w);
      }
    }
    return nouns;
  }

  public boolean containsFormulaWithAllIdentifier(MathTag formula) {
    return !getFormulaWithAllIdentifiers(formula).isEmpty();
  }

  public Set<MathTag> getFormulaWithAllIdentifiers(MathTag formula) {
    return sentenceMath.stream()
            .filter(m -> m.containsIdentifier(formula.getIdentifiers().elementSet()))
            .collect(Collectors.toSet());
  }

  public boolean containsIdentifier(String... identifier) {
    return sentenceIdentifier.containsAll(Arrays.asList(identifier));
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

  public int getGraphDistance(int word1, int word2){
    try {
      IndexedWord wi1 = graph.getNodeByIndex(word1+1);
      IndexedWord wi2 = graph.getNodeByIndex(word2+1);
      List<SemanticGraphEdge> edges = graph.getShortestUndirectedPathEdges(wi1, wi2);
      return edges.size();
    } catch (IllegalArgumentException iae) {
      iae.printStackTrace();
      return -1;
    }
  }

  @Override
  public String toString() {
    return "Sentence [words=" + words + ", identifiers=" + sentenceIdentifier + "]";
  }

}
