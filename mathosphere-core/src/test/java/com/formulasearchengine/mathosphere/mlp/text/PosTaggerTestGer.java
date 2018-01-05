package com.formulasearchengine.mathosphere.mlp.text;

import com.formulasearchengine.mathosphere.mlp.PatternMatchingRelationFinder;
import com.formulasearchengine.mathosphere.mlp.cli.FlinkMlpCommandConfig;
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
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PosTaggerTestGer {

  private static final Logger LOGGER = LogManager.getLogger(PosTaggerTestGer.class.getName());
  /**
   * other models "edu/stanford/nlp/models/pos-tagger/german/german-fast.tagger",
   * "edu/stanford/nlp/models/pos-tagger/german/german-fast-caseless.tagger",
   * "edu/stanford/nlp/models/pos-tagger/german/german-hgc.tagger"
   */
  private static final String GER = "edu/stanford/nlp/models/pos-tagger/german/german-fast.tagger";

  // other models
  @Test
  public void simpleGermanTest() throws Exception {
    FlinkMlpCommandConfig cfg = FlinkMlpCommandConfig.test();
    cfg.setModel(GER);
    PosTagger nlpProcessor = PosTagger.create(cfg);
    String text = "Dies ist ein simpler Beispieltext.";

    List<MathTag> mathTags = WikiTextUtils.findMathTags(text);

    String newText = WikiTextUtils.replaceAllFormulas(text, mathTags);
    String cleanText = WikiTextUtils.extractPlainText(newText);

    List<Sentence> result = nlpProcessor.process(cleanText, mathTags);

    List<Word> expected = Arrays.asList(w("Dies", "PDS"), w("ist", "VAFIN"), w("ein", "ART"), w("simpler", "ADJA"), w("Beispieltext", "NN"),
        w(".", "$."));

    List<Word> sentence = result.get(0).getWords();
    assertEquals(expected, sentence.subList(0, expected.size()));
    LOGGER.debug("full result: {}", result);
  }

  @Test
  public void mediumGermanTest() throws Exception {
    final String text = IOUtils.toString(PosTaggerTest.class.getResourceAsStream("deText.txt"),"UTF-8");

    FlinkMlpCommandConfig cfg = FlinkMlpCommandConfig.test();
    cfg.setModel(GER);
    PosTagger nlpProcessor = PosTagger.create(cfg);


    List<MathTag> mathTags = WikiTextUtils.findMathTags(text);

    String newText = WikiTextUtils.replaceAllFormulas(text, mathTags);
    long t0 = System.nanoTime();
    String cleanText = WikiTextUtils.extractPlainText(newText);
    System.out.println((System.nanoTime() - t0) / 1000000 + "ms for cleaning.");
    List<Sentence> result = nlpProcessor.process(cleanText, mathTags);
  }

  public static Word w(String word, String tag) {
    return new Word(word, tag);
  }

  public static String readText(String name) throws IOException {
    InputStream inputStream = PatternMatchingRelationFinder.class.getResourceAsStream(name);
    return IOUtils.toString(inputStream,"UTF-8");
  }

}
