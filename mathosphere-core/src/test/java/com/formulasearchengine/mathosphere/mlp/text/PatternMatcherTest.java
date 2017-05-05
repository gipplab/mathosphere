package com.formulasearchengine.mathosphere.mlp.text;

import com.formulasearchengine.mathosphere.mlp.pojos.ParsedWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.Word;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static com.formulasearchengine.mathosphere.mlp.text.PatternMatcher.*;

/**
 * Created by Leo on 26.01.2017.
 */
public class PatternMatcherTest {

  public static final String SPEED_OF_LIGHT = "speed of light";
  public static final String TIMES = "times";
  public static final String C_2 = "c^2";
  public static final String M = "m";
  public static final String MASS = "mass";
  public static final String EQUALS = "=";
  public static final String ENERGY = "energy";
  public static final String E = "E";
  public static final String ID = "ID";
  public static final String SYM = "SYM";
  public static final String NN = "NN";
  public static final String VB = "VB";
  public static final String NNP = "NNP";
  public static final String IS = "is";
  public static final String ARE = "are";
  private PatternMatcher patternMatcher;
  private Set<String> identifiers = new HashSet<>();
  private ParsedWikiDocument parsedWikiDocument = new ParsedWikiDocument();

  @Before
  public void setup() {
    identifiers.add("E");
    identifiers.add("m");
    identifiers.add("c^2");
    patternMatcher = generatePatterns(identifiers);
  }

  @Test
  public void apposition() throws Exception {
    List<Word> words = new ArrayList<>();
    words.add(new Word(ENERGY, NN));
    words.add(new Word(E, ID));
    words.add(new Word(EQUALS, SYM));
    words.add(new Word(MASS, NN));
    words.add(new Word(M, ID));
    words.add(new Word(TIMES, VB));
    words.add(new Word(SPEED_OF_LIGHT, NNP));
    words.add(new Word(C_2, ID));
    List<PatternMatcher.IdentifierMatch> matches = patternMatcher.match(words, parsedWikiDocument);
    Assert.assertEquals(3, matches.size());
    Collections.sort(matches, new Comparator<PatternMatcher.IdentifierMatch>() {
      @Override
      public int compare(IdentifierMatch o1, IdentifierMatch o2) {
        return o1.getIdentifier().toLowerCase().compareTo(o2.getIdentifier().toLowerCase());
      }
    });
    for (int i = 0; i < matches.size(); i++) {
      IdentifierMatch match = matches.get(i);
      switch (i) {
        case 0: //c
          Assert.assertEquals(C_2, match.getIdentifier());
          Assert.assertEquals(SPEED_OF_LIGHT, match.getDefinition());
          break;
        case 1: //c
          Assert.assertEquals(E, match.getIdentifier());
          Assert.assertEquals(ENERGY, match.getDefinition());
          break;
        case 2: //c
          Assert.assertEquals(M, match.getIdentifier());
          Assert.assertEquals(MASS, match.getDefinition());
          break;
      }
    }
  }

  @Test
  public void pattern2() throws Exception {
    List<Word> words = new ArrayList<>();
    words.add(new Word(E, ID));
    words.add(new Word(IS, "bar"));
    words.add(new Word(ENERGY, NN));
    //split word
    words.add(new Word(TIMES, VB));
    words.add(new Word(C_2, ID));
    words.add(new Word(ARE, "foo"));
    words.add(new Word(SPEED_OF_LIGHT, NNP));
    List<PatternMatcher.IdentifierMatch> matches = patternMatcher.match(words, parsedWikiDocument);
    Assert.assertEquals(2, matches.size());
    Collections.sort(matches, new Comparator<PatternMatcher.IdentifierMatch>() {
      @Override
      public int compare(IdentifierMatch o1, IdentifierMatch o2) {
        return o1.getIdentifier().toLowerCase().compareTo(o2.getIdentifier().toLowerCase());
      }
    });
    for (int i = 0; i < matches.size(); i++) {
      IdentifierMatch match = matches.get(i);
      switch (i) {
        case 0: //c
          Assert.assertEquals(C_2, match.getIdentifier());
          Assert.assertEquals(SPEED_OF_LIGHT, match.getDefinition());
          Assert.assertEquals(2, match.getPosition());
          break;
        case 1: //E
          Assert.assertEquals(E, match.getIdentifier());
          Assert.assertEquals(ENERGY, match.getDefinition());
          Assert.assertEquals(2, match.getPosition());
          break;
      }
    }
  }

  @Test
  public void patternRepeat() throws Exception {
    List<Word> words = new ArrayList<>();
    words.add(new Word(E, ID));
    words.add(new Word(IS, "bar"));
    words.add(new Word(ENERGY, NN));
    //split word
    words.add(new Word(TIMES, VB));
    words.add(new Word(E, ID));
    words.add(new Word(IS, "bar"));
    words.add(new Word(ENERGY, NN));
    List<PatternMatcher.IdentifierMatch> matches = patternMatcher.match(words, parsedWikiDocument);
    Assert.assertEquals(2, matches.size());
    Collections.sort(matches, new Comparator<PatternMatcher.IdentifierMatch>() {
      @Override
      public int compare(IdentifierMatch o1, IdentifierMatch o2) {
        return o1.getIdentifier().toLowerCase().compareTo(o2.getIdentifier().toLowerCase());
      }
    });
    for (int i = 0; i < matches.size(); i++) {
      IdentifierMatch match = matches.get(i);
      switch (i) {
        case 0: //c
          Assert.assertEquals(E, match.getIdentifier());
          Assert.assertEquals(ENERGY, match.getDefinition());
          Assert.assertEquals(2, match.getPosition());
          break;
        case 1: //c
          Assert.assertEquals(E, match.getIdentifier());
          Assert.assertEquals(ENERGY, match.getDefinition());
          Assert.assertEquals(2, match.getPosition());
          break;
      }
    }
  }

}