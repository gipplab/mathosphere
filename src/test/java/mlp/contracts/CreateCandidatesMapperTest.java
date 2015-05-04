package mlp.contracts;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import mlp.Config;
import mlp.pojos.ParsedWikiDocument;
import mlp.pojos.RawWikiDocument;
import mlp.pojos.Relation;
import mlp.pojos.WikiDocumentOutput;
import mlp.pojos.Word;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class CreateCandidatesMapperTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateCandidatesMapperTest.class);

    final Config config = Config.test();

    @Test
    public void test() throws Exception {
        ParsedWikiDocument doc = read("augmentendwikitext.xml");
        CreateCandidatesMapper candidatesMapper = new CreateCandidatesMapper(config);
        WikiDocumentOutput output = candidatesMapper.map(doc);

        for (Relation relation : output.getRelations()) {
            LOGGER.debug("relation: {}", relation);
        }
    }

    public static ParsedWikiDocument read(String testFile) throws Exception {
        return read(testFile, 0);
    }

    public static ParsedWikiDocument read(String testFile, int docNo) throws Exception {
        RawWikiDocument doc1 = TextAnnotatorMapperTest.readWikiTextDocuments(testFile).get(docNo);
        TextAnnotatorMapper textAnnotator = TextAnnotatorMapperTest.TEST_INSTANCE;
        return textAnnotator.map(doc1);
    }

    @Test
    public void calculateMaxFrequency() {
        Multiset<String> set = HashMultiset.create(Arrays.asList("a", "b", "c", "a", "a", "c"));
        int actual = CreateCandidatesMapper.calculateMax(set);
        assertEquals(3, actual);
    }

    @Test
    public void closestIdentifierPosition() {
        List<Integer> positions = Arrays.asList(0, 10, 25);
        int actual = CreateCandidatesMapper.closestIdentifierPosition(positions, 4);
        assertEquals(0, actual);
    }

    @Test
    public void closestIdentifierPosition_oneElement() {
        List<Integer> positions = Arrays.asList(10);
        int actual = CreateCandidatesMapper.closestIdentifierPosition(positions, 4);
        assertEquals(10, actual);
    }

    @Test
    public void closestIdentifierPosition_lastElement() {
        List<Integer> positions = Arrays.asList(0, 10, 25);
        int actual = CreateCandidatesMapper.closestIdentifierPosition(positions, 20);
        assertEquals(25, actual);
    }

    @Test
    public void identifierPositions() {
        List<Word> sentence = Arrays.asList(w("Ψ", "LNK"), w("is", "VBZ"), w("the", "DT"),
                w("wave function", "LNK"), w(",", ","), w("i", "FW"), w("is", "VBZ"), w("the", "DT"),
                w("imaginary unit", "LNK"), w(",", ","), w("ħ", "NN"), w("is", "VBZ"), w("the", "DT"),
                w("reduced Planck constant", "LNK"));
        List<Integer> identifierPositions = CreateCandidatesMapper.identifierPositions(sentence, "Ψ");
        assertEquals(Arrays.asList(0), identifierPositions);
    }

    @Test
    public void identifierPositions_several() {
        List<Word> sentence = Arrays.asList(w("Ψ", "LNK"), w("is", "VBZ"), w("the", "DT"),
                w("wave function", "LNK"), w(",", ","), w("Ψ", "FW"), w("is", "VBZ"), w("the", "DT"),
                w("imaginary unit", "LNK"), w(",", ","), w("Ψ", "NN"), w("is", "VBZ"), w("the", "DT"),
                w("reduced Planck constant", "LNK"));
        List<Integer> identifierPositions = CreateCandidatesMapper.identifierPositions(sentence, "Ψ");
        assertEquals(Arrays.asList(0, 5, 10), identifierPositions);
    }

    @Test
    public void identifierPositions_none() {
        List<Word> sentence = Arrays.asList(w("p", "LNK"), w("is", "VBZ"), w("the", "DT"),
                w("wave function", "LNK"), w(",", ","), w("i", "FW"), w("is", "VBZ"), w("the", "DT"),
                w("imaginary unit", "LNK"), w(",", ","), w("ħ", "NN"), w("is", "VBZ"), w("the", "DT"),
                w("reduced Planck constant", "LNK"));
        List<Integer> identifierPositions = CreateCandidatesMapper.identifierPositions(sentence, "Ψ");
        assertEquals(Collections.emptyList(), identifierPositions);
    }

    public static Word w(String word, String tag) {
        return new Word(word, tag);
    }
}
