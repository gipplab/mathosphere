package com.formulasearchengine.mathosphere.mlp.text;

import com.formulasearchengine.mathosphere.mlp.PatternMatchingRelationFinder;
import com.formulasearchengine.mathosphere.mlp.cli.FlinkMlpCommandConfig;
import com.formulasearchengine.mathosphere.mlp.pojos.DocumentMetaLib;
import com.formulasearchengine.mathosphere.mlp.pojos.MathTag;
import com.formulasearchengine.mathosphere.mlp.pojos.Sentence;
import com.formulasearchengine.mathosphere.mlp.pojos.Word;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PosTaggerTest {

  private static final Logger LOGGER = LogManager.getLogger(PosTaggerTest.class.getName());

  @Test
  public void annotation() throws Exception {
    FlinkMlpCommandConfig cfg = FlinkMlpCommandConfig.test();
    TextAnnotator annotator = new TextAnnotator(cfg);
    String text = readText("escaped.txt");

    WikiTextParser parser = new WikiTextParser(text);
    List<String> cleanText = parser.parse();
    DocumentMetaLib lib = parser.getMetaLibrary();
//    System.out.println(cleanText);

    List<Sentence> result = annotator.annotate(cleanText, lib);

    // TODO planck constant is replaced by normal 'h' in UnicodeUtils.java Line 101 (why so ever)
    List<Word> expected = Arrays.asList(
            w("where", "WRB"), w("Ψ", "MATH"), w("is", "VBZ"), w("the", "DT"),
            w("wave function", "LNK"), w("of", "IN"), w("the quantum system", "NP"),
            w(",", ","), w("i", "MATH"), w("is", "VBZ"), w("the imaginary unit", "LNK")
    );

    List<Word> sentence = result.get(0).getWords();
    sentence = TextAnnotator.unwrapPlaceholder(sentence, lib);
    assertTrue(sentence.size() >= expected.size());
    assertEquals(expected.toString(), sentence.subList(12, 12 + expected.size()).toString());
    LOGGER.debug("full result: {}", result);
  }

  @Test
  public void annotationMathWordings() throws Exception {
    FlinkMlpCommandConfig cfg = FlinkMlpCommandConfig.test();
    TextAnnotator annotator = new TextAnnotator(cfg);
    String text = "Bessel functions of the first kind, denoted as {{math|J<sub>α</sub>(x)}}, are solutions of Bessel's differential equation.";

    WikiTextParser parser = new WikiTextParser(text);
    List<String> cleanText = parser.parse();
    DocumentMetaLib lib = parser.getMetaLibrary();

    List<Sentence> result = annotator.annotate(cleanText, lib);

    List<Word> expected = Arrays.asList(
            w("Bessel function of the first kind", "NP"),
            w(",", ","), w("denoted", "VBN"), w("as", "IN"), w("FORMULA_c60e75c1b449c3a8c1735102f828843d", "MATH"),
            w("are", "VBP"), w("solutions of Bessel's differential equation", "NP"), w(".", ".")
    );

    List<Word> sentence = result.get(0).getWords();
    LOGGER.debug("Words: {}", sentence);
    sentence = TextAnnotator.unwrapPlaceholder(sentence, lib);
    assertTrue(sentence.toString(), sentence.size() >= expected.size());
    assertEquals(sentence.toString(), expected.toString(), sentence.subList(0, expected.size()).toString());
    LOGGER.debug("full result: {}", result);
  }

  @Test
  public void dashWordingTest() throws Exception {
    FlinkMlpCommandConfig cfg = FlinkMlpCommandConfig.test();
    TextAnnotator annotator = new TextAnnotator(cfg);
    String text = "The first few Meixner–Pollaczek polynomials are <math>x</math>";

    WikiTextParser parser = new WikiTextParser(text);
    List<String> cleanText = parser.parse();
    DocumentMetaLib lib = parser.getMetaLibrary();

    List<Sentence> result = annotator.annotate(cleanText, lib);

    List<Word> expected = Arrays.asList(
            w("The", "DT"),
            w("first few Meixner -- Pollaczek polynomial", "NP"),
            w("are", "VBP"), w("x", "MATH")
    );

    List<Word> sentence = result.get(0).getWords();
    LOGGER.debug("Words: {}", sentence);
    sentence = TextAnnotator.unwrapPlaceholder(sentence, lib);
    assertTrue(sentence.toString(), sentence.size() >= expected.size());
    assertEquals(sentence.toString(), expected.toString(), sentence.subList(0, expected.size()).toString());
    LOGGER.debug("full result: {}", result);
  }

  @Test
  public void joinLinks_withLinks() {
    List<Word> in = Arrays.asList(w("Since", "IN"), w("``", "``"), w("energy", "NN"), w("''", "''"),
        w("and", "CC"), w("``", "``"), w("momentum", "NN"), w("''", "''"), w("are", "VBP"),
        w("related", "VBN"));

    List<Word> expected = Arrays.asList(w("Since", "IN"), w("energy", PosTag.LINK), w("and", "CC"),
        w("momentum", PosTag.LINK), w("are", "VBP"), w("related", "VBN"));

    List<Word> actual = PosTagger.concatenateLinks(in, new HashSet<String>());
    assertEquals(expected.toString(), actual.toString());
  }

  @Test
  public void joinLinks_noLinks() {
    List<Word> in = Arrays.asList(w("Since", "IN"), w("energy", "NN"), w("and", "CC"),
        w("momentum", "NN"), w("are", "VBP"), w("related", "VBN"));
    List<Word> expected = in;
    List<Word> actual = PosTagger.concatenateLinks(in, new HashSet<String>());
    assertEquals(expected.toString(), actual.toString());
  }

  @Test
  public void concatenate_inside() {
    List<Word> in = Arrays.asList(w("Since", "IN"), w("energy", "NN"), w("momentum", "NN"),
        w("related", "VBN"));
    List<Word> expected = Arrays.asList(w("Since", "IN"), w("energy momentum", "NP"),
        w("related", "VBN"));
    List<Word> actual = PosTagger.concatenateSuccessiveNounsToNounSequence(in);
    assertEquals(expected.toString(), actual.toString());
  }

  @Test
  public void concatenate_noSucc() {
    List<Word> in = Arrays.asList(w("Since", "IN"), w("energy", "NN"), w("and", "CC"),
        w("momentum", "NN"), w("are", "VBP"), w("related", "VBN"));
    List<Word> expected = in;
    List<Word> actual = PosTagger.concatenateSuccessiveNounsToNounSequence(in);
    assertEquals(expected.toString(), actual.toString());
  }

  @Test
  public void concatenateJJtoNP() {
    List<Word> in = Arrays.asList(w("to", "TO"), w("be", "VB"), w("the", "DT"), w("same", "JJ"),
        w("type", "NN"));
    List<Word> expected = Arrays.asList(w("to", "TO"), w("be", "VB"), w("the", "DT"),
        w("same type", "NP"));
    List<Word> actual = PosTagger.concatenateTwoSuccessiveRegexTags(in, "JJ", "NN", "NP");
    assertEquals(expected.toString(), actual.toString());
  }

  @Test
  public void concatenateJJtoNP_notFollowed() {
    List<Word> in = Arrays.asList(w("be", "VB"), w("the", "DT"), w("same", "JJ"), w("to", "TO"));
    List<Word> expected = in;
    List<Word> actual = PosTagger.concatenateTwoSuccessiveRegexTags(in, "JJ", "NN", "NP");
    assertEquals(expected.toString(), actual.toString());
  }

  @Test
  public void concatenateJJtoNP_JPLast() {
    List<Word> in = Arrays.asList(w("to", "TO"), w("be", "VB"), w("the", "DT"), w("same", "JJ"));
    List<Word> expected = in;
    List<Word> actual = PosTagger.concatenateTwoSuccessiveRegexTags(in, "JJ", "NN", "NP");
    assertEquals(expected.toString(), actual.toString());
  }

  @Test
  public void concatenateJJtoNPSEQ() {
    List<Word> in = Arrays.asList(w("is", "TO"), w("very", "JJ"), w("Jacobi", "NNP"), w("polynomials", "NNS"),
            w("going", "VB"));
    List<Word> expected = Arrays.asList(w("is", "TO"), w("very Jacobi polynomials", "NP"), w("going", "VB"));
    List<Word> actual = PosTagger.concatenatePhrases(in);
    assertEquals(expected.toString(), actual.toString());
  }

  public static Word w(String word, String tag) {
    return new Word(word, tag);
  }

  public static String readText(String name) throws IOException {
    InputStream inputStream = PatternMatchingRelationFinder.class.getResourceAsStream(name);
    return IOUtils.toString(inputStream,"UTF-8");
  }

}
