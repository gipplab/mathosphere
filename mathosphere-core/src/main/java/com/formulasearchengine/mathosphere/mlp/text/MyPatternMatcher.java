package com.formulasearchengine.mathosphere.mlp.text;

import com.alexeygrigorev.rseq.*;
import com.formulasearchengine.mathosphere.mlp.features.FeatureVector;
import com.formulasearchengine.mathosphere.mlp.pojos.ParsedWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.WikidataLink;
import com.formulasearchengine.mathosphere.mlp.pojos.Word;

import java.util.*;

public class MyPatternMatcher {
  private static final String SENTENCE_NUMBER = "sentence_number";
  public static final String DOCUMENT_TITLE = "title";
  public static final String IDENTIFIER = "identifier";
  public static final String DEFINITION = "definition";
  public static final double TRUE = 1d;
  public static final double BEFORE = 1d;
  public static final double AFTER = -1d;
  private List<Pattern<Word>> patterns;
  public static Integer NUMBER_OF_FEATURES = 16;

  public enum features {
    PATTERN1,
    PATTERN2,
    PATTERN3,
    PATTERN4,
    PATTERN5,
    PATTERN6,
    PATTERN7,
    PATTERN8,
    PATTERN9,
    PATTERN10,
    COLON,
    COMMA,
    OTHERMATH,
    PARENTHESES,
    POSITION,
    WORD_DISTANCE
  }

  public MyPatternMatcher(List<Pattern<Word>> patterns) {
    this.patterns = patterns;
  }

  public Collection<FeatureVector> match(List<Word> words, ParsedWikiDocument doc) {
    HashMap<String, FeatureVector> result = new HashMap<>();
    for (int i = 0; i < patterns.size(); i++) {
      Pattern<Word> pattern = patterns.get(i);
      List<Match<Word>> matches = pattern.find(words);
      for (Match<Word> match : matches) {
        String id = match.getVariable(IDENTIFIER).getWord();

        String def = deLinkify(match.getVariable(DEFINITION), doc);

        switch (i) {
          case 0:
          case 1:
          case 2:
          case 3:
          case 4:
          case 5:
          case 6:
          case 7:
          case 8:
          case 9:
            //found by pattern
            setFeature(result, id, def, i, TRUE);
            break;
          case 10:
            setFeature(result, id, def, features.COLON.ordinal(), TRUE);
            break;
          case 11:
            setFeature(result, id, def, features.COMMA.ordinal(), TRUE);
            break;
          case 12:
            setFeature(result, id, def, features.OTHERMATH.ordinal(), TRUE);
            break;
          case 13:
            setFeature(result, id, def, features.PARENTHESES.ordinal(), TRUE);
            break;
          case 14:
            setFeature(result, id, def, features.POSITION.ordinal(), BEFORE);
            setFeature(result, id, def, features.WORD_DISTANCE.ordinal(), match.matchedTo() - match.matchedFrom());
            break;
          case 15:
            setFeature(result, id, def, features.POSITION.ordinal(), AFTER);
            setFeature(result, id, def, features.WORD_DISTANCE.ordinal(), match.matchedTo() - match.matchedFrom());
            break;
        }
      }
    }
    return result.values();
  }

  /*public Collection<FeatureVector> match(List<Word> words, ParsedWikiDocument doc) {
    HashMap<String, FeatureVector> result = new HashMap<>();
    for (int i = 0; i < patterns.size(); i++) {
      Pattern<Word> pattern = patterns.get(i);
      List<Match<Word>> matches = pattern.find(words);
      for (Match<Word> match : matches) {
        String id = match.getVariable(IDENTIFIER).getWord();

        String def = deLinkify(match.getVariable(DEFINITION), doc);

        switch (i) {
          case 0:
          case 1:
          case 2:
          case 3:
          case 4:
          case 5:
          case 6:
          case 7:
          case 8:
          case 9:
            //found by pattern
            setFeature(result, id, def, i, TRUE);
            break;
          case 10:
            setFeature(result, id, def, features.COLON.ordinal(), TRUE);
            break;
          case 11:
            setFeature(result, id, def, features.COMMA.ordinal(), TRUE);
            break;
          case 12:
            setFeature(result, id, def, features.OTHERMATH.ordinal(), TRUE);
            break;
          case 13:
            setFeature(result, id, def, features.PARENTHESES.ordinal(), TRUE);
            break;
          case 14:
            setFeature(result, id, def, features.POSITION.ordinal(), BEFORE);
            setFeature(result, id, def, features.WORD_DISTANCE.ordinal(), match.matchedTo()-match.matchedFrom());
            break;
          case 15:
            setFeature(result, id, def, features.POSITION.ordinal(), AFTER);
            setFeature(result, id, def, features.WORD_DISTANCE.ordinal(), match.matchedTo()-match.matchedFrom());
            break;
        }
      }
    }
    return result.values();
  }*/

  public void setFeature(HashMap<String, FeatureVector> result, String id, String def, int feature, double value) {
    FeatureVector temp = result.get(key(id, def));
    if (temp != null) {
      temp.setFeature(feature, value);
    } else {
      result.put(key(id, def), new FeatureVector(id, def, NUMBER_OF_FEATURES).setFeature(feature, value));
    }
  }

  private String key(String id, String def) {
    return id + def;
  }

  private String deLinkify(Word word, ParsedWikiDocument doc) {
    String definition;
    if (word.getPosTag().equals(PosTag.LINK)) {
      String hash = word.getWord().replaceAll("^LINK_", "");
      WikidataLink link = doc.getLinkMap().get(hash);
      if (link != null) {
        definition = "[[" + link.getContent() + "]]";
      } else {
        definition = "[[" + word.getWord() + "]]";
      }
    } else {
      definition = word.getWord();
    }
    return definition;
  }

  public static MyPatternMatcher generatePatterns(Set<String> identifiers) {
    Matcher<Word> isOrAre = word("is").or(word("are"));
    Matcher<Word> let = word("let");
    Matcher<Word> be = word("be");
    Matcher<Word> by = word("by");
    Matcher<Word> openParentheses = word("(").or(word("{").or(word("[")));
    Matcher<Word> closeParentheses = word(")").or(word("}").or(word("]")));
    Matcher<Word> denotes = word("denotes").or(word("denote"));
    Matcher<Word> denoted = word("denoted");

    Matcher<Word> the = pos("DT");

    Matcher<Word> identifier = BeanMatchers.in(Word.class, "word", identifiers).captureAs(IDENTIFIER);
    Matcher<Word> otherIdentifier = BeanMatchers.in(Word.class, "word", identifiers);
    Matcher<Word> definition = posRegExp("(NN[PS]{0,2}|NP\\+?|NN\\+|LNK)").captureAs(DEFINITION);

    List<Pattern<Word>> patterns = Arrays.asList(
      //1
      Pattern.create(definition, identifier),
      //2
      Pattern.create(identifier, definition),
      //3
      Pattern.create(identifier, denotes, definition),
      //4
      Pattern.create(identifier, denotes, the, definition),
      //5
      Pattern.create(identifier, isOrAre, definition),
      //6
      Pattern.create(identifier, isOrAre, the, definition),
      //7
      Pattern.create(identifier, isOrAre, denoted, by, definition),
      //8
      Pattern.create(identifier, isOrAre, denoted, by, the, definition),
      //9
      Pattern.create(let, identifier, be, denoted, by, definition),
      //10
      Pattern.create(let, identifier, be, denoted, by, the, definition),
      //11
      //paper: 7
      Pattern.create(identifier, anyWord().zeroOrMore(), word(":"), anyWord().zeroOrMore(), definition),
      //12
      //paper: 8
      Pattern.create(identifier, anyWord().zeroOrMore(), word(","), anyWord().zeroOrMore(), definition),
      //13
      //paper: 9
      Pattern.create(identifier, anyWord().zeroOrMore(), otherIdentifier, anyWord().zeroOrMore(), definition),
      //14
      //paper: 10
      Pattern.create(openParentheses, anyWord().zeroOrMore(), identifier, anyWord().zeroOrMore(), closeParentheses, anyWord().zeroOrMore(), definition),
      //15
      //paper: 11 - 21 catchAll
      Pattern.create(definition, anyWord().zeroOrMore(), identifier),
      //16
      Pattern.create(identifier, anyWord().zeroOrMore(), definition)

    );

    return new MyPatternMatcher(patterns);
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

  protected static XMatcher<Word> regExp(String regexp) {
    return BeanMatchers.regex(Word.class, "word", regexp);
  }
}
