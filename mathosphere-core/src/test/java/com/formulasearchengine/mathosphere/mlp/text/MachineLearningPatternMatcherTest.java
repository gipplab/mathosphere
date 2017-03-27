package com.formulasearchengine.mathosphere.mlp.text;

import com.formulasearchengine.mathosphere.mlp.pojos.Sentence;
import com.formulasearchengine.mathosphere.mlp.pojos.Word;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static com.formulasearchengine.mathosphere.mlp.ml.WekaUtils.LONGEST_SENTENCE_IN_ENGISH;

/**
 * Created by Leo on 20.01.2017.
 */
public class MachineLearningPatternMatcherTest {
  public static final double EPSILON = 1e-15;

  public static final double ONE_OCCURRENCE = 1d / LONGEST_SENTENCE_IN_ENGISH;
  public static final double TWO_OCCURRENCES = 2d / LONGEST_SENTENCE_IN_ENGISH;

  public static final Word DENOTES = new Word(MachineLearningPatternMatcher.DENOTES, "");
  public static final Word THE = new Word("definite or indefinite article", "DT");
  public static final Word IS = new Word(MachineLearningPatternMatcher.IS, "");
  public static final Word DENOTED = new Word(MachineLearningPatternMatcher.DENOTED, "");
  public static final Word BY = new Word(MachineLearningPatternMatcher.BY, "");
  public static final Word BE = new Word(MachineLearningPatternMatcher.BE, "");
  public static final Word LET = new Word(MachineLearningPatternMatcher.LET, "");
  public static final Word COLON = new Word(":", ":");
  public static final Word COMMA = new Word(",", ",");
  public static final Word OTHERMATH = new Word("E^2", "MATH");
  public static final Word OPENING = new Word("(", "-LRB-");
  public static final Word CLOSING = new Word(")", "-RRB-");
  private Word identifier = new Word(MachineLearningPatternMatcher.IDENTIFIER, "ID");
  private Word definiens = new Word(MachineLearningPatternMatcher.DEFINITION, "NN");
  private Word random = new Word("random", "NN");
  private ArrayList<Word> words;

  @Before
  public void setup() {
    words = new ArrayList<>();
  }

  @Test
  public void testPattern1() {
    words.add(definiens);
    words.add(identifier);
    Sentence s = new Sentence(words, null, null);
    double[] result = new MachineLearningPatternMatcher().match(s, MachineLearningPatternMatcher.IDENTIFIER, MachineLearningPatternMatcher.DEFINITION, words.indexOf(identifier), words.indexOf(definiens));
    Assert.assertArrayEquals(new double[]{0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, result, EPSILON);
  }

  @Test
  public void testPattern1FalsePositive() {
    words.add(random);
    words.add(identifier);
    Sentence s = new Sentence(words, null, null);
    double[] result = new MachineLearningPatternMatcher().match(s, MachineLearningPatternMatcher.IDENTIFIER, MachineLearningPatternMatcher.DEFINITION, words.indexOf(identifier), words.indexOf(definiens));
    Assert.assertArrayEquals(new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, result, EPSILON);
  }

  @Test
  public void testPattern2() {
    words.add(identifier);
    words.add(definiens);
    Sentence s = new Sentence(words, null, null);
    double[] result = new MachineLearningPatternMatcher().match(s, MachineLearningPatternMatcher.IDENTIFIER, MachineLearningPatternMatcher.DEFINITION, words.indexOf(identifier), words.indexOf(definiens));
    Assert.assertArrayEquals(new double[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, result, EPSILON);
  }

  @Test
  public void testPattern3() {
    words.add(identifier);
    words.add(DENOTES);
    words.add(definiens);
    Sentence s = new Sentence(words, null, null);
    double[] result = new MachineLearningPatternMatcher().match(s, MachineLearningPatternMatcher.IDENTIFIER, MachineLearningPatternMatcher.DEFINITION, words.indexOf(identifier), words.indexOf(definiens));
    Assert.assertArrayEquals(new double[]{0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0}, result, EPSILON);
  }

  @Test
  public void testPattern4() {
    words.add(identifier);
    words.add(DENOTES);
    words.add(THE);
    words.add(definiens);
    Sentence s = new Sentence(words, null, null);
    double[] result = new MachineLearningPatternMatcher().match(s, MachineLearningPatternMatcher.IDENTIFIER, MachineLearningPatternMatcher.DEFINITION, words.indexOf(identifier), words.indexOf(definiens));
    Assert.assertArrayEquals(new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0}, result, EPSILON);
  }

  @Test
  public void testPattern5() {
    words.add(identifier);
    words.add(IS);
    words.add(definiens);
    Sentence s = new Sentence(words, null, null);
    double[] result = new MachineLearningPatternMatcher().match(s, MachineLearningPatternMatcher.IDENTIFIER, MachineLearningPatternMatcher.DEFINITION, words.indexOf(identifier), words.indexOf(definiens));
    Assert.assertArrayEquals(new double[]{0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, result, EPSILON);
  }

  @Test
  public void testPattern6() {
    words.add(identifier);
    words.add(IS);
    words.add(THE);
    words.add(definiens);
    Sentence s = new Sentence(words, null, null);
    double[] result = new MachineLearningPatternMatcher().match(s, MachineLearningPatternMatcher.IDENTIFIER, MachineLearningPatternMatcher.DEFINITION, words.indexOf(identifier), words.indexOf(definiens));
    Assert.assertArrayEquals(new double[]{0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, result, EPSILON);
  }

  @Test
  public void testPattern7() {
    words.add(identifier);
    words.add(IS);
    words.add(DENOTED);
    words.add(BY);
    words.add(definiens);
    Sentence s = new Sentence(words, null, null);
    double[] result = new MachineLearningPatternMatcher().match(s, MachineLearningPatternMatcher.IDENTIFIER, MachineLearningPatternMatcher.DEFINITION, words.indexOf(identifier), words.indexOf(definiens));
    Assert.assertArrayEquals(new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, result, EPSILON);
  }

  @Test
  public void testPattern8() {
    words.add(identifier);
    words.add(IS);
    words.add(DENOTED);
    words.add(BY);
    words.add(THE);
    words.add(definiens);
    Sentence s = new Sentence(words, null, null);
    double[] result = new MachineLearningPatternMatcher().match(s, MachineLearningPatternMatcher.IDENTIFIER, MachineLearningPatternMatcher.DEFINITION, words.indexOf(identifier), words.indexOf(definiens));
    Assert.assertArrayEquals(new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, result, EPSILON);
  }

  @Test
  public void testPattern9() {
    words.add(LET);
    words.add(identifier);
    words.add(BE);
    words.add(DENOTED);
    words.add(BY);
    words.add(definiens);
    Sentence s = new Sentence(words, null, null);
    double[] result = new MachineLearningPatternMatcher().match(s, MachineLearningPatternMatcher.IDENTIFIER, MachineLearningPatternMatcher.DEFINITION, words.indexOf(identifier), words.indexOf(definiens));
    Assert.assertArrayEquals(new double[]{0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, result, EPSILON);
  }

  @Test
  public void testPattern10() {
    words.add(LET);
    words.add(identifier);
    words.add(BE);
    words.add(DENOTED);
    words.add(BY);
    words.add(THE);
    words.add(definiens);
    Sentence s = new Sentence(words, null, null);
    double[] result = new MachineLearningPatternMatcher().match(s, MachineLearningPatternMatcher.IDENTIFIER, MachineLearningPatternMatcher.DEFINITION, words.indexOf(identifier), words.indexOf(definiens));
    Assert.assertArrayEquals(new double[]{0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0}, result, EPSILON);
    //destroy pattern test
    s.getWords().add(3, random);
    result = new MachineLearningPatternMatcher().match(s, MachineLearningPatternMatcher.IDENTIFIER, MachineLearningPatternMatcher.DEFINITION, words.indexOf(identifier), words.indexOf(definiens));
    Assert.assertArrayEquals(new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, result, EPSILON);
  }

  @Test
  public void testColon() {
    words.add(identifier);
    words.add(COLON);
    words.add(random);
    words.add(definiens);
    Sentence s = new Sentence(words, null, null);
    double[] result = new MachineLearningPatternMatcher().match(s, MachineLearningPatternMatcher.IDENTIFIER, MachineLearningPatternMatcher.DEFINITION, words.indexOf(identifier), words.indexOf(definiens));
    Assert.assertArrayEquals(new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0}, result, EPSILON);
    //remove colon
    s.getWords().remove(COLON);
    //colon at end
    s.getWords().add(COLON);
    result = new MachineLearningPatternMatcher().match(s, MachineLearningPatternMatcher.IDENTIFIER, MachineLearningPatternMatcher.DEFINITION, words.indexOf(identifier), words.indexOf(definiens));
    Assert.assertArrayEquals(new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, result, EPSILON);
  }

  @Test
  public void testComma() {
    words.add(identifier);
    words.add(COMMA);
    words.add(random);
    words.add(definiens);
    Sentence s = new Sentence(words, null, null);
    double[] result = new MachineLearningPatternMatcher().match(s, MachineLearningPatternMatcher.IDENTIFIER, MachineLearningPatternMatcher.DEFINITION, words.indexOf(identifier), words.indexOf(definiens));
    Assert.assertArrayEquals(new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0}, result, EPSILON);
    //remove colon
    s.getWords().remove(COMMA);
    //colon at end
    s.getWords().add(COMMA);
    result = new MachineLearningPatternMatcher().match(s, MachineLearningPatternMatcher.IDENTIFIER, MachineLearningPatternMatcher.DEFINITION, words.indexOf(identifier), words.indexOf(definiens));
    Assert.assertArrayEquals(new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, result, EPSILON);
  }

  @Test
  public void testOthermath() {
    words.add(identifier);
    words.add(OTHERMATH);
    words.add(random);
    words.add(definiens);
    Sentence s = new Sentence(words, null, null);
    double[] result = new MachineLearningPatternMatcher().match(s, MachineLearningPatternMatcher.IDENTIFIER, MachineLearningPatternMatcher.DEFINITION, words.indexOf(identifier), words.indexOf(definiens));
    Assert.assertArrayEquals(new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0}, result, EPSILON);
    //remove OTHERMATH
    s.getWords().remove(OTHERMATH);
    //OTHERMATH at beginning
    s.getWords().add(0, OTHERMATH);
    result = new MachineLearningPatternMatcher().match(s, MachineLearningPatternMatcher.IDENTIFIER, MachineLearningPatternMatcher.DEFINITION, words.indexOf(identifier), words.indexOf(definiens));
    Assert.assertArrayEquals(new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, result, EPSILON);
  }

  @Test
  public void testOpenParentheses() {
    words.add(identifier);
    words.add(OPENING);
    words.add(random);
    words.add(definiens);
    Sentence s = new Sentence(words, null, null);
    double[] result = new MachineLearningPatternMatcher().match(s, MachineLearningPatternMatcher.IDENTIFIER, MachineLearningPatternMatcher.DEFINITION, words.indexOf(identifier), words.indexOf(definiens));
    Assert.assertArrayEquals(new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0}, result, EPSILON);
    //remove Parentheses
    s.getWords().remove(OPENING);
    //Parentheses at end
    s.getWords().add(OPENING);
    result = new MachineLearningPatternMatcher().match(s, MachineLearningPatternMatcher.IDENTIFIER, MachineLearningPatternMatcher.DEFINITION, words.indexOf(identifier), words.indexOf(definiens));
    Assert.assertArrayEquals(new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, result, EPSILON);
  }

  @Test
  public void testClosingParentheses() {
    words.add(identifier);
    words.add(CLOSING);
    words.add(random);
    words.add(definiens);
    Sentence s = new Sentence(words, null, null);
    double[] result = new MachineLearningPatternMatcher().match(s, MachineLearningPatternMatcher.IDENTIFIER, MachineLearningPatternMatcher.DEFINITION, words.indexOf(identifier), words.indexOf(definiens));
    Assert.assertArrayEquals(new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}, result, EPSILON);
  }

  @Test
  public void testManyParentheses() {
    words.add(definiens);
    words.add(CLOSING);
    words.add(random);
    words.add(OPENING);
    words.add(identifier);
    Sentence s = new Sentence(words, null, null);
    double[] result = new MachineLearningPatternMatcher().match(s, MachineLearningPatternMatcher.IDENTIFIER, MachineLearningPatternMatcher.DEFINITION, words.indexOf(identifier), words.indexOf(definiens));
    Assert.assertArrayEquals(new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, result, EPSILON);
  }

  @Test
  public void testManyParentheses2() {
    words.add(definiens);
    words.add(OPENING);
    words.add(random);
    words.add(CLOSING);
    words.add(OPENING);
    words.add(identifier);
    words.add(CLOSING);
    Sentence s = new Sentence(words, null, null);
    double[] result = new MachineLearningPatternMatcher().match(s, MachineLearningPatternMatcher.IDENTIFIER, MachineLearningPatternMatcher.DEFINITION, words.indexOf(identifier), words.indexOf(definiens));
    Assert.assertArrayEquals(new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}, result, EPSILON);
  }

  @Test
  public void testManyParenthesesDefiniensInParentheses() {
    words.add(OPENING);
    words.add(definiens);
    words.add(OPENING);
    words.add(random);
    words.add(CLOSING);
    words.add(CLOSING);
    words.add(identifier);
    Sentence s = new Sentence(words, null, null);
    double[] result = new MachineLearningPatternMatcher().match(s, MachineLearningPatternMatcher.IDENTIFIER, MachineLearningPatternMatcher.DEFINITION, words.indexOf(identifier), words.indexOf(definiens));
    Assert.assertArrayEquals(new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0}, result, EPSILON);
  }


  @Test
  public void testIdentifierAndDefiniensPositions() {
    words.add(identifier);
    words.add(definiens);
    words.add(identifier);
    Sentence s = new Sentence(words, null, null);
    double[] result = new MachineLearningPatternMatcher().match(s, MachineLearningPatternMatcher.IDENTIFIER, MachineLearningPatternMatcher.DEFINITION, 2, 1);
    Assert.assertArrayEquals(new double[]{1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, result, EPSILON);
    result = new MachineLearningPatternMatcher().match(s, MachineLearningPatternMatcher.IDENTIFIER, MachineLearningPatternMatcher.DEFINITION, 0, 1);
    Assert.assertArrayEquals(new double[]{1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, result, EPSILON);
  }
}
