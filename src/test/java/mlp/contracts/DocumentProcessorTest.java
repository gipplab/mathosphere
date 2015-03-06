package mlp.contracts;

import java.io.InputStream;

import mlp.RelationFinder;
import mlp.types.WikiDocument;

import org.apache.commons.io.IOUtils;
import org.apache.flink.api.java.tuple.Tuple2;
import org.junit.Test;

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

        System.out.println(out.getList());
    }

}
