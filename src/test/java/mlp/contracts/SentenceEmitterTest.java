package mlp.contracts;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import mlp.Config;
import mlp.RelationFinder;
import mlp.types.Sentence;
import mlp.types.WikiDocument;

import org.apache.commons.io.IOUtils;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.junit.Test;

public class SentenceEmitterTest {

    private static final String DEFAULT_POS_MODEL = Config.test().getModel();

    @Test
    public void test() throws Exception {
        List<Tuple2<String, WikiDocument>> docs = readTestWikiDocuments();

        SentenceEmitter sentenceEmitter = new SentenceEmitter(DEFAULT_POS_MODEL);
        sentenceEmitter.open(null);

        ListCollector<Tuple3<String, Sentence, Double>> out = new ListCollector<>();
        for (Tuple2<String, WikiDocument> in : docs) {
            sentenceEmitter.flatMap(in, out);
        }

        for (Tuple3<String, Sentence, Double> t3 : out.getList()) {
            System.out.println(t3);
        }
    }

    public static List<Tuple2<String, WikiDocument>> readTestWikiDocuments() throws IOException, Exception {
        InputStream stream = RelationFinder.class.getResourceAsStream("augmentendwikitext.xml");
        String documents = IOUtils.toString(stream);
        String[] pages = documents.split("</page>");

        DocumentProcessor documentProcessor = new DocumentProcessor();

        ListCollector<Tuple2<String, WikiDocument>> out = new ListCollector<>();
        for (String page : pages) {
            documentProcessor.flatMap(page, out);
        }

        return out.getList();
    }

}
