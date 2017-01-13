package com.formulasearchengine.mathosphere.mlp.text;

import com.alexeygrigorev.rseq.*;
import com.formulasearchengine.mathosphere.mlp.features.FeatureVector;
import com.formulasearchengine.mathosphere.mlp.pojos.ParsedWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.Sentence;
import com.formulasearchengine.mathosphere.mlp.pojos.WikidataLink;
import com.formulasearchengine.mathosphere.mlp.pojos.Word;

import java.util.*;

public class MyPatternMatcher {
  public static final String IDENTIFIER = "identifier";
  public static final String DEFINITION = "definition";

  public static int[] match(Sentence sentence, String identifierText, String definiens, int identifierPosition, int definiensPosition) {
    Matcher<Word> isOrAre = word("is").or(word("are"));
    Matcher<Word> let = word("let");
    Matcher<Word> be = word("be");
    Matcher<Word> by = word("by");
    Matcher<Word> denotes = word("denotes").or(word("denote"));
    Matcher<Word> denoted = word("denoted");

    Matcher<Word> the = pos("DT");

    Matcher<Word> identifier = BeanMatchers.eq(Word.class, "word", identifierText).captureAs(IDENTIFIER);
    Matcher<Word> definition = posRegExp("(NN[PS]{0,2}|NP\\+?|NN\\+|LNK)").captureAs(DEFINITION);
    Matcher<Word> otherMathExpression = posRegExp("(ID|MATH)").captureAs("othermath");

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
      //colon
      Pattern.create(pos(":")),
      //12
      //comma
      Pattern.create(pos(",")),
      //13
      //othermath
      Pattern.create(otherMathExpression),
      //14
      //parens
      Pattern.create(word("\\(")),
      Pattern.create(word("\\)"))
    );

    int[] result = new int[16];
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
        case 10:
          for (Match<Word> match : matches) {
            Word matchedDefiniens = match.getVariable(DEFINITION);
            if (matchedDefiniens != null && matchedDefiniens.getWord().equals(definiens))
              result[i] = 1;
          }
          break;
        case 11:
        case 12:
        case 13:
          for (Match<Word> match : matches) {
            if (inRange(match.matchedFrom(), identifierPosition, definiensPosition))
              result[i] = 1;
          }
          break;
        case 14:
          openingParentheses = matches.stream().filter(m -> inRange(m.matchedFrom(), identifierPosition, definiensPosition)).count();
          break;
        case 15:
          //definiens in parentheses
          long closingParentheses = matches.stream().filter(m -> inRange(m.matchedFrom(), identifierPosition, definiensPosition)).count();
          if (identifierPosition < definiensPosition)
            //more opening parentheses than closing -> definiens in parentheses
            result[13] = openingParentheses - closingParentheses > 0 ? 1 : 0;
          if (identifierPosition > definiensPosition)
            //more closing parentheses than opening -> identifier in parentheses
            result[14] = closingParentheses - openingParentheses > 0 ? 1 : 0;
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

  protected static XMatcher<Word> regExp(String regexp) {
    return BeanMatchers.regex(Word.class, "word", regexp);
  }
}
