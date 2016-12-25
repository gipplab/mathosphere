package com.formulasearchengine.mathosphere.mlp.text;

import com.alexeygrigorev.rseq.*;
import com.formulasearchengine.mathosphere.mlp.pojos.*;

import java.util.*;

/**
 * Simply extracts whole sentences with identifiers and definitions.
 */
public class SimplePatternMatcher {
  public static final String IDENTIFIER = "identifier";
  public static final String DEFINITION = "definition";
  private List<Pattern<Word>> patterns;

  public SimplePatternMatcher(List<Pattern<Word>> patterns) {
    this.patterns = patterns;
  }

  public Collection<Relation> match(Sentence sentence, ParsedWikiDocument doc) {
    List<Relation> result = new ArrayList<>();
    List<Match<Word>> identifierMatches = patterns.get(0).find(sentence.getWords());
    if (identifierMatches.size() > 0) {
      List<Match<Word>> definiensMatches = patterns.get(1).find(sentence.getWords());
      if (definiensMatches.size() > 0) {
        for (Match<Word> identifier : identifierMatches) {
          for (Match<Word> definiens : definiensMatches) {
            if (definiens.getVariable(DEFINITION).getWord().length() >= 3) {
              Relation relation = new Relation();
              relation.setIdentifier(identifier.getVariable(IDENTIFIER).getWord());
              relation.setDefinition(definiens.getVariable(DEFINITION), doc);
              Word definiensWord = sentence.getWords().remove(definiens.matchedFrom());
              Word cleanDefiniensWord = new Word(relation.getDefinition(), definiensWord.getPosTag());
              sentence.getWords().add(definiens.matchedFrom(), cleanDefiniensWord);
              relation.setSentence(sentence);
              relation.setIdentifierPosition(identifier.matchedFrom());
              relation.setWordPosition(definiens.matchedFrom());
              result.add(relation);
            }
          }
        }
      }
    }
    return result;
  }

  public static SimplePatternMatcher generatePatterns(Set<String> identifiers) {

    Matcher<Word> identifier = BeanMatchers.in(Word.class, "word", identifiers).captureAs(IDENTIFIER);
    Matcher<Word> definition = posRegExp("(NN[PS]{0,2}|NP\\+?|NN\\+|LNK)").captureAs(DEFINITION);

    List<Pattern<Word>> patterns = Arrays.asList(
      Pattern.create(identifier),
      Pattern.create(definition)
    );
    return new SimplePatternMatcher(patterns);
  }

  public static XMatcher<Word> anyWord() {
    return Matchers.anything();
  }

  protected static XMatcher<Word> word(String word) {
    return BeanMatchers.eq(Word.class, "word", word);
  }

  protected static XMatcher<Word> pos(String pos) {
    return BeanMatchers.eq(Word.class, "posTag", pos);
  }

  protected static XMatcher<Word> posRegExp(String regexp) {
    return BeanMatchers.regex(Word.class, "posTag", regexp);
  }
}
