package mlp.contracts;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

import mlp.Config;
import mlp.RelationFinder;
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

        Formula formula = randomElement(shroedingerOut.getFormulas());
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
        InputStream stream = RelationFinder.class.getResourceAsStream(testFile);
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

    
}
