package com.formulasearchengine.mathosphere.mlp.contracts;

import com.formulasearchengine.mathosphere.mlp.cli.FlinkMlpCommandConfig;
import com.formulasearchengine.mathosphere.mlp.pojos.ParsedWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.RawWikiDocument;
import org.junit.Test;

import java.util.List;

public class RussianWikiPipelineTest {

    private static TextAnnotatorMapper ruTextAnnotator() throws Exception {
        String[] params = {"--language", "ru", "-pos", "", "-in", "fake-input", "-out", "fake-output"};
        FlinkMlpCommandConfig config = FlinkMlpCommandConfig.from(params);

        TextAnnotatorMapper textAnnotator = new TextAnnotatorMapper(config);
        textAnnotator.open(null);
        return textAnnotator;
    }

    @Test
    public void fullPipeline() throws Exception {
        String wikiRuFile = "com/formulasearchengine/mathosphere/mlp/wikirusample.xml";

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
}
