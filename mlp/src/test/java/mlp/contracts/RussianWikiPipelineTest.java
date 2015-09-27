package mlp.contracts;

import mlp.Config;
import mlp.pojos.ParsedWikiDocument;
import mlp.pojos.RawWikiDocument;
import org.junit.Test;

import java.util.List;

public class RussianWikiPipelineTest {

  @Test
  public void fullPipeline() throws Exception {
    String wikiRuFile = "wikirusample.xml";

    TextAnnotatorMapper textAnnotator = ruTextAnnotator();

    List<RawWikiDocument> docs = readDocs(wikiRuFile);

    for (RawWikiDocument doc : docs) {
      ParsedWikiDocument parsedDoc = textAnnotator.map(doc);
      System.out.println(parsedDoc);
    }
  }

  private List<RawWikiDocument> readDocs(String wikiRuFile) throws Exception {
    return TextAnnotatorMapperTest.readWikiTextDocuments(wikiRuFile);
  }

  private static TextAnnotatorMapper ruTextAnnotator() throws Exception {
    String[] params = {"--language", "ru", "-pos", ""};
    Config config = Config.from(params);

    TextAnnotatorMapper textAnnotator = new TextAnnotatorMapper(config);
    textAnnotator.open(null);
    return textAnnotator;
  }
}
