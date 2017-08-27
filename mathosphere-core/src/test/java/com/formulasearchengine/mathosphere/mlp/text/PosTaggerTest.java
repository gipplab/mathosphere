package com.formulasearchengine.mathosphere.mlp.text;

import com.formulasearchengine.mathosphere.mlp.PatternMatchingRelationFinder;
import com.formulasearchengine.mathosphere.mlp.cli.FlinkMlpCommandConfig;
import com.formulasearchengine.mathosphere.mlp.pojos.MathTag;
import com.formulasearchengine.mathosphere.mlp.pojos.Sentence;
import com.formulasearchengine.mathosphere.mlp.pojos.Word;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PosTaggerTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(PosTaggerTest.class);

  @Test
  public void annotation() throws Exception {
    FlinkMlpCommandConfig cfg = FlinkMlpCommandConfig.test();
    PosTagger nlpProcessor = PosTagger.create(cfg);
    String text = readText("escaped.txt");

    List<MathTag> mathTags = WikiTextUtils.findMathTags(text);

    String newText = WikiTextUtils.replaceAllFormulas(text, mathTags);
    String cleanText = WikiTextUtils.extractPlainText(newText);

    List<Sentence> result = nlpProcessor.process(cleanText, mathTags);

    List<Word> expected = Arrays.asList(w("where", "WRB"), w("Ψ", "ID"), w("is", "VBZ"), w("the", "DT"),
        w("wave function", "LNK"), w("of", "IN"), w("the", "DT"), w("quantum system", "NN+"),
      w(",", ","), w("i", "FW"), w("is", "VBZ"), w("the", "DT"), w("imaginary unit", "LNK"),
        w(",", ","), w("ħ", "NN"), w("is", "VBZ"), w("the", "DT"),
        w("reduced Planck constant", "LNK"));

    List<Word> sentence = result.get(0).getWords();
    assertEquals(expected, sentence.subList(0, expected.size()));
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
    assertEquals(expected, actual);
  }

  @Test
  public void joinLinks_noLinks() {
    List<Word> in = Arrays.asList(w("Since", "IN"), w("energy", "NN"), w("and", "CC"),
        w("momentum", "NN"), w("are", "VBP"), w("related", "VBN"));
    List<Word> expected = in;
    List<Word> actual = PosTagger.concatenateLinks(in, new HashSet<String>());
    assertEquals(expected, actual);
  }

  @Test
  public void concatenate_inside() {
    List<Word> in = Arrays.asList(w("Since", "IN"), w("energy", "NN"), w("momentum", "NN"),
        w("related", "VBN"));
    List<Word> expected = Arrays.asList(w("Since", "IN"), w("energy momentum", "NN+"),
        w("related", "VBN"));
    List<Word> actual = PosTagger.concatenateSuccessiveNounsToNounSequence(in);
    assertEquals(expected, actual);
  }

  @Test
  public void concatenate_noSucc() {
    List<Word> in = Arrays.asList(w("Since", "IN"), w("energy", "NN"), w("and", "CC"),
        w("momentum", "NN"), w("are", "VBP"), w("related", "VBN"));
    List<Word> expected = in;
    List<Word> actual = PosTagger.concatenateSuccessiveNounsToNounSequence(in);
    assertEquals(expected, actual);
  }

  @Test
  public void concatenateJJtoNP() {
    List<Word> in = Arrays.asList(w("to", "TO"), w("be", "VB"), w("the", "DT"), w("same", "JJ"),
        w("type", "NN"));
    List<Word> expected = Arrays.asList(w("to", "TO"), w("be", "VB"), w("the", "DT"),
        w("same type", "NP"));
    List<Word> actual = PosTagger.contatenateSuccessive2Tags(in, "JJ", "NN", "NP");
    assertEquals(expected, actual);
  }

  @Test
  public void concatenateJJtoNP_notFollowed() {
    List<Word> in = Arrays.asList(w("be", "VB"), w("the", "DT"), w("same", "JJ"), w("to", "TO"));
    List<Word> expected = in;
    List<Word> actual = PosTagger.contatenateSuccessive2Tags(in, "JJ", "NN", "NP");
    assertEquals(expected, actual);
  }

  @Test
  public void concatenateJJtoNP_JPLast() {
    List<Word> in = Arrays.asList(w("to", "TO"), w("be", "VB"), w("the", "DT"), w("same", "JJ"));
    List<Word> expected = in;
    List<Word> actual = PosTagger.contatenateSuccessive2Tags(in, "JJ", "NN", "NP");
    assertEquals(expected, actual);
  }

  public static Word w(String word, String tag) {
    return new Word(word, tag);
  }

  public static String readText(String name) throws IOException {
    InputStream inputStream = PatternMatchingRelationFinder.class.getResourceAsStream(name);
    return IOUtils.toString(inputStream,"UTF-8");
  }

}
