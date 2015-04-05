package mlp.contracts;

import java.io.InputStream;

import mlp.PatternMatchingRelationFinder;
import mlp.pojos.IndentifiersRepresentation;
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
        WikiDocument doc = CreateCandidatesMapperTest.read("augmentendwikitext.xml", 1);

        PatternMatcherMapper patternMatcher = new PatternMatcherMapper();
        IndentifiersRepresentation identifiers = patternMatcher.map(doc);

        for (Relation relation : identifiers.getRelations()) {
            LOGGER.debug("relation: {}", relation);
        }
    }

    @Test
    public void testShrodingerPart() throws Exception {
        InputStream input = PatternMatchingRelationFinder.class.getResourceAsStream("escaped.txt");
        String text = IOUtils.toString(input);
        WikiDocumentText documentText = new WikiDocumentText("Document", 0, text);

        TextAnnotatorMapper textAnnotator = TextAnnotatorMapperTest.TEST_INSTANCE;
        WikiDocument doc = textAnnotator.map(documentText);

        PatternMatcherMapper patternMatcher = new PatternMatcherMapper();
        IndentifiersRepresentation identifiers = patternMatcher.map(doc);

        for (Relation relation : identifiers.getRelations()) {
            LOGGER.debug("relation: {}, sentence: {}", relation, relation.getSentence());
        }
    }

}
