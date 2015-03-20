package mlp.contracts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import mlp.RelationFinder;
import mlp.contracts.PatternMatcherMapper.Match;
import mlp.contracts.PatternMatcherMapper.Pattern;
import mlp.flink.ListCollector;
import mlp.pojos.Relation;
import mlp.pojos.WikiDocument;
import mlp.pojos.WikiDocumentText;
import mlp.pojos.Word;

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
            LOGGER.debug("relation: {}", relation);
        }
    }


    @Test
    public void findPattern_first() {
        String identifier = "p";
        List<Word> sentence = Arrays.asList(w("p", "LNK"), w("is", "VBZ"), w("the", "DT"),
                w("wave function", "LNK"), w(",", ","), w("i", "FW"), w("is", "VBZ"), w("the", "DT"),
                w("imaginary unit", "LNK"), w(",", ","), w("ħ", "NN"), w("is", "VBZ"), w("the", "DT"),
                w("reduced Planck constant", "LNK"));
        Pattern input = Pattern.of(identifier, "is", "the", Pattern.DEFINITION);
        Match actualMatch = PatternMatcherMapper.findPattern(sentence, input);
        Match expectedMatch = new Match(w("wave function", "LNK"), 0, 3);
        assertEquals(expectedMatch, actualMatch);
    }

    @Test
    public void findPattern_last() {
        String identifier = "ħ";
        List<Word> sentence = Arrays.asList(w("p", "LNK"), w("is", "VBZ"), w("the", "DT"),
                w("wave function", "LNK"), w(",", ","), w("ħ", "NN"), w("is", "VBZ"), w("the", "DT"),
                w("reduced Planck constant", "LNK"));
        Pattern input = Pattern.of(identifier, "is", "the", Pattern.DEFINITION);
        Match actualMatch = PatternMatcherMapper.findPattern(sentence, input);
        Match expectedMatch = new Match(w("reduced Planck constant", "LNK"), 5, 8);
        assertEquals(expectedMatch, actualMatch);
    }

    @Test
    public void findPattern_noMatch() {
        String identifier = "h";
        List<Word> sentence = Arrays.asList(w("p", "LNK"), w("is", "VBZ"), w("the", "DT"),
                w("wave function", "LNK"), w(",", ","), w("ħ", "NN"), w("is", "VBZ"), w("the", "DT"),
                w("reduced Planck constant", "LNK"));
        Pattern input = Pattern.of(identifier, "is", "the", Pattern.DEFINITION);
        Match actualMatch = PatternMatcherMapper.findPattern(sentence, input);

        assertNull(actualMatch);
    }

    public static Word w(String word, String tag) {
        return new Word(word, tag);
    }

}
