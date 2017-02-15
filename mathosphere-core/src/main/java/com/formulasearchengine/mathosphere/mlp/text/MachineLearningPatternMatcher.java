package com.formulasearchengine.mathosphere.mlp.text;

import com.alexeygrigorev.rseq.*;
import com.formulasearchengine.mathosphere.mlp.ml.WekaUtils;
import com.formulasearchengine.mathosphere.mlp.pojos.Sentence;
import com.formulasearchengine.mathosphere.mlp.pojos.Word;

import java.util.*;

public class MachineLearningPatternMatcher {

  public static final String IDENTIFIER = "identifier";
  public static final String DEFINITION = "definition";
  public static final String OTHERMATH = "othermath";
  public static final String IS = "is";
  public static final String ARE = "are";
  public static final String LET = "let";
  public static final String BE = "be";
  public static final String DENOTES = "denotes";
  public static final String DENOTE = "denote";
  public static final String DENOTED = "denoted";
  public static final String BY = "by";

  public static final Matcher<Word> isOrAre = word(IS).or(word(ARE));
  public static final Matcher<Word> let = word(LET);
  public static final Matcher<Word> be = word(BE);
  public static final Matcher<Word> by = word(BY);
  public static final Matcher<Word> denotes = word(DENOTES).or(word(DENOTE));
  public static final Matcher<Word> denoted = word(DENOTED);

  public static final Matcher<Word> the = pos("DT");

  /**
   * @param sentence
   * @param identifierText
   * @param definiens
   * @param identifierPosition 0 indexed
   * @param definiensPosition  0 indexed
   * @return [pattern1, ... , pattern10, colon between, comma between, othermath between, definiens in parens, identifier in parens]
   */
  public double[] match(Sentence sentence, String identifierText, String definiens, int identifierPosition, int definiensPosition) {
    Matcher<Word> identifier = BeanMatchers.eq(Word.class, "word", identifierText).captureAs(IDENTIFIER);
    Matcher<Word> definition = BeanMatchers.eq(Word.class, "word", definiens).captureAs(DEFINITION);
    Matcher<Word> otherMathExpression = posRegExp("(ID|MATH)").captureAs(OTHERMATH);

    List<Pattern<Word>> patterns = Arrays.asList(
      //1 not in pagel
      Pattern.create(identifier, definition),
      //2 pagel 1
      Pattern.create(definition, identifier),
      //3 pagel 2
      Pattern.create(identifier, isOrAre, definition),
      //4 pagel 3
      Pattern.create(identifier, isOrAre, the, definition),
      //5 pagel 4
      Pattern.create(let, identifier, be, denoted, by, definition),
      //6 pagel 4
      Pattern.create(let, identifier, be, denoted, by, the, definition),
      //7 pagel 5
      Pattern.create(definition, isOrAre, denoted, by, identifier),
      //8 pagel 5
      Pattern.create(definition, isOrAre, denoted, by, the, identifier),
      //9 pagel 6
      Pattern.create(identifier, denotes, definition),
      //10 pagel 6
      Pattern.create(identifier, denotes, the, definition),
      //11
      //colon
      Pattern.create(pos(":")),
      //12
      //comma
      Pattern.create(pos(",")),
      //13
      //othermath
      Pattern.create(otherMathExpression),
      //14
      //definiens in parentheses, relative to identifier
      Pattern.create(word("(").or(pos("-LRB-"))),
      //15
      //identifier in parentheses, relative to definiens
      Pattern.create(word(")").or(pos("-RRB-"))),
      //16
      Pattern.create(definition),
      //17
      Pattern.create(identifier)
    );

    double[] result = new double[patterns.size()];
    long openingParentheses = 0;
    for (int i = 0; i < patterns.size(); i++) {
      Pattern<Word> pattern = patterns.get(i);
      List<Match<Word>> matches = pattern.find(sentence.getWords());
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
          for (Match<Word> match : matches) {
            //check that the positions match
            //if (match.matchedFrom() + identifierPattern.find(match.getMatchedSubsequence()).get(0).matchedFrom() == identifierPosition &&
            // match.matchedFrom() + definiensPattern.find(match.getMatchedSubsequence()).get(0).matchedFrom() == definiensPosition) {
            Word matchedDefiniens = match.getVariable(DEFINITION);
            if (matchedDefiniens != null && matchedDefiniens.getWord().equals(definiens))
              result[i] = 1;
            //}
          }
          break;
        case 10:
        case 11:
        case 12:
          for (Match<Word> match : matches) {
            if (inRange(match.matchedFrom(), identifierPosition, definiensPosition))
              result[i] = 1;
          }
          break;
        case 13:
          openingParentheses = matches.stream().filter(m -> inRange(m.matchedFrom(), identifierPosition, definiensPosition)).count();
          break;
        case 14:
          //definiens in parentheses
          long closingParentheses = matches.stream().filter(m -> inRange(m.matchedFrom(), identifierPosition, definiensPosition)).count();
          if (identifierPosition < definiensPosition) {
            if (openingParentheses - closingParentheses > 0) {
              //definiens in parentheses
              result[13] = 1;
            } else if (openingParentheses - closingParentheses < 0)
              //identifier in parentheses
              result[14] = 1;
          }
          if (identifierPosition > definiensPosition) {
            if (openingParentheses - closingParentheses > 0) {
              //identifier in parentheses
              result[14] = 1;
            } else if (openingParentheses - closingParentheses < 0)
              //definiens in parentheses
              result[13] = 1;
          }
        case 15:
          //result[15] = ((double) matches.size()) / WekaUtils.LONGEST_SENTENCE_IN_ENGISH;
          break;
        case 16:
          //result[16] = ((double) matches.size()) / WekaUtils.LONGEST_SENTENCE_IN_ENGISH;
          break;
      }
    }
    return result;
  }

  /**
   * Checks if x lies between y and z
   *
   * @return
   */
  private static boolean inRange(int x, int y, int z) {
    return ((y < x && x < z) || (z < x && x < y));
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
