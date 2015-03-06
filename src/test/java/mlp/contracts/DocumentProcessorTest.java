package mlp.contracts;

import java.io.InputStream;
import java.util.List;

import mlp.RelationFinder;
import mlp.types.WikiDocument;

import org.apache.commons.io.IOUtils;
import org.apache.flink.api.java.tuple.Tuple2;
import org.junit.Test;

import static org.junit.Assert.*;

public class DocumentProcessorTest {

    @Test
    public void testFlatMap() throws Exception {
        InputStream stream = RelationFinder.class.getResourceAsStream("augmentendwikitext.xml");
        String documents = IOUtils.toString(stream);
        String[] pages = documents.split("</page>");

        DocumentProcessor documentProcessor = new DocumentProcessor();

        ListCollector<Tuple2<String, WikiDocument>> out = new ListCollector<>();
        for (String page : pages) {
            documentProcessor.flatMap(page, out);
        }

        List<Tuple2<String, WikiDocument>> output = out.getList();
        assertEquals(2, output.size());

        Tuple2<String, WikiDocument> doc1 = output.get(0);
        assertEquals(doc1.f0, "Schrödinger equation");
        assertTrue(doc1.f1.containsIndetifier("Ψ"));
        assertTrue(doc1.f1.containsIndetifier("H"));

        Tuple2<String, WikiDocument> doc2 = output.get(1);
        assertEquals(doc2.f0, "Gas constant");
        assertTrue(doc2.f1.containsIndetifier("R"));
        assertTrue(doc2.f1.containsIndetifier("J"));
    }
}
