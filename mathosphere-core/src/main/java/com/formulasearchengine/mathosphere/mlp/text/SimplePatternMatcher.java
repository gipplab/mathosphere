package com.formulasearchengine.mathosphere.mlp.text;

import com.alexeygrigorev.rseq.*;
import com.formulasearchengine.mathosphere.mlp.features.FeatureVector;
import com.formulasearchengine.mathosphere.mlp.pojos.*;

import java.util.*;

import static com.formulasearchengine.mathosphere.mlp.text.WikiTextUtils.deLinkify;

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
    for (int i = 0; i < patterns.size(); i++) {
      Pattern<Word> pattern = patterns.get(i);
      List<Match<Word>> matches = pattern.find(sentence.getWords());
      for (Match<Word> match : matches) {
        Relation relation = new Relation();
        relation.setIdentifier(match.getVariable(IDENTIFIER).getWord());
        relation.setDefinition(match.getVariable(DEFINITION), doc);
        relation.setSentence(sentence);
        relation.setIdentifierPosition(match.matchedFrom() + match.getMatchedSubsequence().indexOf(match.getVariable(IDENTIFIER)));
        relation.setWordPosition(match.matchedFrom() + match.getMatchedSubsequence().indexOf(match.getVariable(DEFINITION)));
        result.add(relation);
      }
    }
    return result;
  }

  public static SimplePatternMatcher generatePatterns(Set<String> identifiers) {

    Matcher<Word> identifier = BeanMatchers.in(Word.class, "word", identifiers).captureAs(IDENTIFIER);
    Matcher<Word> definition = posRegExp("(NN[PS]{0,2}|NP\\+?|NN\\+|LNK)").captureAs(DEFINITION);

    List<Pattern<Word>> patterns = Arrays.asList(
      //1
      Pattern.create(definition, anyWord(), identifier),
      //2
      Pattern.create(identifier, anyWord(), definition)
    );
    return new SimplePatternMatcher(patterns);
  }

  public static XMatcher<Word> anyWord() {
    return word(".");
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
