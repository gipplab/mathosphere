package mlp.contracts;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.List;

import mlp.RelationFinder;
import mlp.flink.ListCollector;
import mlp.pojos.WikiDocumentText;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class TextExtractorMapperTest {

    @Test
    public void test() throws Exception {
        InputStream stream = RelationFinder.class.getResourceAsStream("augmentendwikitext.xml");
        String rawImput = IOUtils.toString(stream);
        assertTrue(rawImput.contains("&lt;math"));

        String[] pages = rawImput.split("</page>");
        TextExtractorMapper textExtractor = new TextExtractorMapper();

        ListCollector<WikiDocumentText> out = new ListCollector<>();
        for (String page : pages) {
            textExtractor.flatMap(page, out);
        }

        List<WikiDocumentText> output = out.getList();
        assertEquals(2, output.size());

        WikiDocumentText doc1 = output.get(0);
        assertEquals(doc1.title, "Schrödinger equation");
        assertFalse(doc1.text.contains("&lt;math"));
        assertTrue(doc1.text.contains("<math"));

        WikiDocumentText doc2 = output.get(1);
        assertEquals(doc2.title, "Gas constant");
    }

}
