package com.formulasearchengine.mathosphere.mlp.contracts;

import com.formulasearchengine.mathosphere.mlp.PatternMatchingRelationFinder;
import com.formulasearchengine.mathosphere.mlp.flink.ListCollector;
import com.formulasearchengine.mathosphere.mlp.pojos.RawWikiDocument;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

public class TextExtractorMapperTest {

  @Test
  public void test() throws Exception {
    InputStream stream = PatternMatchingRelationFinder.class.getResourceAsStream("augmentendwikitext.xml");
    String rawImput = IOUtils.toString(stream,"UTF-8");
    assertTrue(rawImput.contains("&lt;math"));

    String[] pages = rawImput.split("</page>");
    TextExtractorMapper textExtractor = new TextExtractorMapper();

    ListCollector<RawWikiDocument> out = new ListCollector<>();
    for (String page : pages) {
      textExtractor.flatMap(page, out);
    }

    List<RawWikiDocument> output = out.getList();
    assertEquals(2, output.size());

    RawWikiDocument doc1 = output.get(0);
    assertEquals("Schrödinger equation", doc1.title);
    assertFalse(doc1.text.contains("&lt;math"));
    assertTrue(doc1.text.contains("<math"));

    RawWikiDocument doc2 = output.get(1);
    assertEquals(doc2.title, "Gas constant");
  }

  @Test
  public void testGer() throws Exception {
    InputStream stream = PatternMatchingRelationFinder.class.getResourceAsStream("dewikimath-20151213130534.xml");
    String rawImput = IOUtils.toString(stream,"UTF-8");
    final String expected = IOUtils.toString(PatternMatchingRelationFinder.class.getResourceAsStream("text/deText.txt"),"UTF-8");
    assertTrue(rawImput.contains("&lt;math"));

    String[] pages = rawImput.split("</page>");
    TextExtractorMapper textExtractor = new TextExtractorMapper();

    ListCollector<RawWikiDocument> out = new ListCollector<>();
    for (String page : pages) {
      textExtractor.flatMap(page, out);
    }

    List<RawWikiDocument> output = out.getList();
    assertEquals(1, output.size());

    RawWikiDocument doc1 = output.get(0);
    assertEquals("Clapeyron-Gleichung", doc1.title);
    assertThat(doc1.text, not(containsString("&lt;math")));
    assertThat(doc1.text, containsString("<math"));
    assertEquals(expected, doc1.text);
  }

}
