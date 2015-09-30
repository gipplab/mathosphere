package mlp.contracts;

import mlp.PatternMatchingRelationFinder;
import mlp.flink.ListCollector;
import mlp.pojos.RawWikiDocument;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.*;

public class TextExtractorMapperTest {

  @Test
  public void test() throws Exception {
    InputStream stream = PatternMatchingRelationFinder.class.getResourceAsStream("augmentendwikitext.xml");
    String rawImput = IOUtils.toString(stream);
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

}
