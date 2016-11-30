package com.formulasearchengine.mathosphere.mlp.text;

import com.alexeygrigorev.rseq.*;
import com.formulasearchengine.mathosphere.mlp.pojos.Word;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class MachineLearningPatternMatcher extends MyPatternMatcher {
  public static final XMatcher<Word> IS = word("is");
  public static final XMatcher<Word> ARE = word("are");
  public static final XMatcher<Word> LET = word("let");
  public static final XMatcher<Word> SET = word("set");
  public static final XMatcher<Word> DENOTE = word("denote");
  public static final XMatcher<Word> DENOTES = word("denotes");
  public static final XMatcher<Word> BE = word("be");
  public static final XMatcher<Word> DENOTED = word("denoted");
  public static final XMatcher<Word> DEFINED = word("defined");
  public static final XMatcher<Word> GIVEN = word("given");
  public static final XMatcher<Word> AS = word("as");
  public static final XMatcher<Word> BY = word("by");
  public static final XMatcher<Word> STANDFOR = word("stand for");
  public static final XMatcher<Word> MEAN = word("mean");
  public static final XMatcher<Word> WORD = word("stands for");
  public static final XMatcher<Word> MEANS = word("means");
  public static Matcher<Word> THE = pos("DT");

  public MachineLearningPatternMatcher(List<Pattern<Word>> patterns) {
    super(patterns);
  }

  public static MachineLearningPatternMatcher generatePatterns(Set<String> identifiers) {
    Matcher<Word> isOrAre = IS.or(ARE);
    Matcher<Word> isOrAre0 = IS.or(ARE).zeroOrMore();
    Matcher<Word> letOrSet = LET.or(SET);
    Matcher<Word> one = DENOTE.or(DENOTES).or(BE);
    Matcher<Word> denote = DENOTE.or(DENOTES);
    Matcher<Word> two = DENOTED.or(DEFINED).or(GIVEN);
    Matcher<Word> asOrBy = AS.or(BY);
    Matcher<Word> three = DENOTES.or(DENOTE).or(STANDFOR).or(WORD.or(MEAN)).or(MEANS);

    Matcher<Word> identifier = BeanMatchers.in(Word.class, "word", identifiers).captureAs("identifier");
    Matcher<Word> othermath = BeanMatchers.in(Word.class, "word", identifiers).captureAs("othermath");
    Matcher<Word> definition = posRegExp("(NN[PS]{0,2}|NP\\+?|NN\\+|LNK)").captureAs("definition");
    Matcher<Word> ptn1 = regExp("(D|d)enote(d)?");
    Matcher<Word> ptn1_2 = regExp("(as|by)");
    Matcher<Word> ptn1_3 = posRegExp("MATH").or(definition);

    Matcher<Word> ptn2 = regExp("((L|l)et|(S|s)et)");
    Matcher<Word> ptn2_2 = posRegExp("MATH");
    Matcher<Word> ptn2_3 = regExp("(denote|denotes|be)");
    Matcher<Word> ptn2_4 = posRegExp("NP");

    Matcher<Word> ptn3 = regExp("NP (is|are)? (denoted|defined|given) (as|by) MATH\\.?");
    Matcher<Word> ptn4 = regExp("MATH (denotes|denote|(stand|stands)\\sfor|mean|means) NP\\.?");
    Matcher<Word> ptn5 = regExp("MATH (is|are) NP\\.?");
    Matcher<Word> ptn6 = regExp("NP (is|are) MATH\\.?");
    List<Pattern<Word>> patterns = Arrays.asList(
      Pattern.create(ptn1, ptn1_2, identifier, definition),
      Pattern.create(ptn2, identifier, ptn2_3, definition),
      Pattern.create(definition, isOrAre0, two, identifier),
      //ptn4 = re.compile(r"MATH (denotes|denote|(stand|stands)\sfor|mean|means) NP\.?")
      Pattern.create(identifier, three, definition),
      //ptn5 = re.compile(r"MATH (is|are) NP\.?")
      Pattern.create(identifier, isOrAre0, definition),
      //ptn6 = re.compile(r"NP (is|are) MATH\.?")
      Pattern.create(definition, isOrAre0, identifier),
      Pattern.create(ptn6)
    );

    /*
    ptn1 = re.compile(r"(D|d)enote(d)? (as|by)\sMATH\sNP\.?")
    ptn2 = re.compile(r"((L|l)et|(S|s)et) MATH (denote|denotes|be) NP\.?")
    ptn3 = re.compile(r"NP (is|are)? (denoted|defined|given) (as|by) MATH\.?")
    ptn4 = re.compile(r"MATH (denotes|denote|(stand|stands)\sfor|mean|means) NP\.?")
    ptn5 = re.compile(r"MATH (is|are) NP\.?")
    ptn6 = re.compile(r"NP (is|are) MATH\.?")
     */
    return new MachineLearningPatternMatcher(patterns);
  }
}