package mlp.contracts;

import java.io.InputStream;

import mlp.RelationFinder;
import mlp.flink.ListCollector;
import mlp.pojos.Relation;
import mlp.pojos.WikiDocument;
import mlp.pojos.WikiDocumentText;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatternMatcherMapperTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(PatternMatcherMapperTest.class);

    @Test
    public void testShodingerFull() throws Exception {
        WikiDocument doc = CreateCandidatesMapperTest.read("augmentendwikitext.xml");

        PatternMatcherMapper patternMatcher = new PatternMatcherMapper();
        ListCollector<Relation> out = new ListCollector<>();
        patternMatcher.flatMap(doc, out);

        for (Relation relation : out.getList()) {
            LOGGER.debug("relation: {}", relation);
        }
    }

    @Test
    public void testShrodingerPart() throws Exception {
        InputStream input = RelationFinder.class.getResourceAsStream("escaped.txt");
        String text = IOUtils.toString(input);
        WikiDocumentText documentText = new WikiDocumentText(1, "Document", 0, text);

        TextAnnotatorMapper textAnnotator = TextAnnotatorMapperTest.TEST_INSTANCE;
        WikiDocument doc = textAnnotator.map(documentText);

        PatternMatcherMapper patternMatcher = new PatternMatcherMapper();
        ListCollector<Relation> out = new ListCollector<>();
        patternMatcher.flatMap(doc, out);

        for (Relation relation : out.getList()) {
            LOGGER.debug("relation: {}, sentence: {}", relation, relation.getSentence());
        }
    }

}
