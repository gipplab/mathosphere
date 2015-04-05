package mlp.contracts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

import mlp.Config;
import mlp.PatternMatchingRelationFinder;
import mlp.flink.ListCollector;
import mlp.pojos.Formula;
import mlp.pojos.Sentence;
import mlp.pojos.WikiDocument;
import mlp.pojos.WikiDocumentText;
import mlp.pojos.Word;
import mlp.text.PosTag;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.google.common.base.Throwables;

public class TextAnnotatorMapperTest {

    private static final Random RND = new Random();

    public static final TextAnnotatorMapper TEST_INSTANCE = createTestInstance();

    @Test
    public void test() throws Exception {
        List<WikiDocumentText> docs = readWikiTextDocuments("augmentendwikitext.xml");
        WikiDocumentText schroedingerIn = docs.get(0);

        WikiDocument shroedingerOut = TEST_INSTANCE.map(schroedingerIn);

        Set<String> identifiers = shroedingerOut.getIdentifiers();
        assertTrue(identifiers.containsAll(Arrays.asList("Ψ", "V", "h", "λ", "ρ", "τ")));

        List<Formula> formulas = shroedingerOut.getFormulas();
        Formula formula = null;
        for (Formula f : formulas) {
            if ("FORMULA_435442ec26d51f5503b96a94c0d7389f".equals(f.getKey())) {
                formula = f;
            }
        }

        assertNotNull(formula);
        assertTrue(contains(formula, shroedingerOut.getSentences()));
    }

    private static boolean contains(Formula formula, List<Sentence> sentences) {
        Word mathWord = new Word(formula.getKey(), PosTag.MATH);
        for (Sentence sentence : sentences) {
            List<Word> words = sentence.getWords();
            if (words.contains(mathWord)) {
                return true;
            }
        }
        return false;
    }

    public static Formula randomElement(List<Formula> formulas) {
        int idx = RND.nextInt(formulas.size());
        return formulas.get(idx);
    }

    public static List<WikiDocumentText> readWikiTextDocuments(String testFile) throws Exception {
        InputStream stream = PatternMatchingRelationFinder.class.getResourceAsStream(testFile);
        String rawImput = IOUtils.toString(stream);
        String[] pages = rawImput.split("</page>");
        TextExtractorMapper textExtractor = new TextExtractorMapper();

        ListCollector<WikiDocumentText> out = new ListCollector<>();
        for (String page : pages) {
            textExtractor.flatMap(page, out);
        }

        return out.getList();
    }

    private static TextAnnotatorMapper createTestInstance() {
        try {
            TextAnnotatorMapper textAnnotator = new TextAnnotatorMapper(Config.test().getModel());
            textAnnotator.open(null);
            return textAnnotator;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Test
    public void tokenization_formulaSuffexed() throws Exception {
        String text = "The <math>x</math>-axis shows...";
        WikiDocumentText doc = new WikiDocumentText("some doc", 1, text);
        WikiDocument result = TEST_INSTANCE.map(doc);

        List<Formula> formulas = result.getFormulas();
        assertEquals(1, formulas.size());

        Sentence sentence = result.getSentences().get(0);

        List<Word> expected = Arrays.asList(new Word("The", "DT"), new Word("x", "ID"), new Word("-axis",
                "-SUF"), new Word("shows", "VBZ"), new Word("...", ":"));
        assertEquals(expected, sentence.getWords());
    }

}
