package mlp.text;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import mlp.Config;
import mlp.RelationFinder;
import mlp.contracts.TextAnnotatorMapper;
import mlp.pojos.Formula;
import mlp.pojos.Sentence;
import mlp.pojos.Word;
import mlp.text.WikiTextUtils.MathTag;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PosTaggerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(PosTaggerTest.class);
    private static final String MODEL = Config.test().getModel();

    @Test
    public void annotation() throws Exception {
        PosTagger nlpProcessor = PosTagger.create(MODEL);
        String text = readText("escaped.txt");

        List<MathTag> mathTags = WikiTextUtils.findMathTags(text);
        List<Formula> formulas = TextAnnotatorMapper.toFormulas(mathTags);

        String newText = WikiTextUtils.replaceAllFormulas(text, mathTags);
        String cleanText = WikiTextUtils.extractPlainText(newText);

        List<Sentence> result = nlpProcessor.process(cleanText, formulas);

        List<Word> expected = Arrays.asList(w("where", "WRB"), w("Ψ", "LNK"), w("is", "VBZ"), w("the", "DT"),
                w("wave function", "LNK"), w("of", "IN"), w("the", "DT"), w("quantum system", "NN+"),
                w(",", ","), w("i", "FW"), w("is", "VBZ"), w("the", "DT"), w("imaginary unit", "LNK"),
                w(",", ","), w("ħ", "NN"), w("is", "VBZ"), w("the", "DT"),
                w("reduced Planck constant", "LNK"));

        List<Word> sentence = result.get(0).getWords();
        assertEquals(expected, sentence.subList(0, expected.size()));
        LOGGER.debug("full result: {}", result);
    }

    @Test
    public void joinLinks_withLinks() {
        List<Word> in = Arrays.asList(w("Since", "IN"), w("``", "``"), w("energy", "NN"), w("''", "''"),
                w("and", "CC"), w("``", "``"), w("momentum", "NN"), w("''", "''"), w("are", "VBP"),
                w("related", "VBN"));

        String tag = "LNK";
        List<Word> expected = Arrays.asList(w("Since", "IN"), w("energy", tag), w("and", "CC"),
                w("momentum", tag), w("are", "VBP"), w("related", "VBN"));

        List<Word> actual = PosTagger.concatenateLinks(in, tag);
        assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void joinLinks_noClosingLink_exception() {
        List<Word> in = Arrays.asList(w("Since", "IN"), w("``", "``"), w("energy", "NN"), w("''", "''"),
                w("and", "CC"), w("``", "``"), w("momentum", "NN"), w("are", "VBP"), w("related", "VBN"));
        PosTagger.concatenateLinks(in, "LNK");
    }

    @Test
    public void joinLinks_noLinks() {
        List<Word> in = Arrays.asList(w("Since", "IN"), w("energy", "NN"), w("and", "CC"),
                w("momentum", "NN"), w("are", "VBP"), w("related", "VBN"));
        List<Word> expected = in;
        List<Word> actual = PosTagger.concatenateLinks(in, "LNK");
        assertEquals(expected, actual);
    }

    @Test
    public void concatenate_inside() {
        List<Word> in = Arrays.asList(w("Since", "IN"), w("energy", "NN"), w("momentum", "NN"),
                w("related", "VBN"));
        List<Word> expected = Arrays.asList(w("Since", "IN"), w("energy momentum", "NN+"),
                w("related", "VBN"));
        List<Word> actual = PosTagger.concatenateSuccessive(in, "NN", "NN+");
        assertEquals(expected, actual);
    }

    @Test
    public void concatenate_noSucc() {
        List<Word> in = Arrays.asList(w("Since", "IN"), w("energy", "NN"), w("and", "CC"),
                w("momentum", "NN"), w("are", "VBP"), w("related", "VBN"));
        List<Word> expected = in;
        List<Word> actual = PosTagger.concatenateSuccessive(in, "NN", "NN+");
        assertEquals(expected, actual);
    }

    @Test
    public void concatenateJJtoNP() {
        List<Word> in = Arrays.asList(w("to", "TO"), w("be", "VB"), w("the", "DT"), w("same", "JJ"),
                w("type", "NN"));
        List<Word> expected = Arrays.asList(w("to", "TO"), w("be", "VB"), w("the", "DT"),
                w("same type", "NP"));
        List<Word> actual = PosTagger.contatenateSuccessiveBy2Tags(in, "JJ", "NN", "NP");
        assertEquals(expected, actual);
    }

    @Test
    public void concatenateJJtoNP_notFollowed() {
        List<Word> in = Arrays.asList(w("be", "VB"), w("the", "DT"), w("same", "JJ"), w("to", "TO"));
        List<Word> expected = in;
        List<Word> actual = PosTagger.contatenateSuccessiveBy2Tags(in, "JJ", "NN", "NP");
        assertEquals(expected, actual);
    }

    @Test
    public void concatenateJJtoNP_JPLast() {
        List<Word> in = Arrays.asList(w("to", "TO"), w("be", "VB"), w("the", "DT"), w("same", "JJ"));
        List<Word> expected = in;
        List<Word> actual = PosTagger.contatenateSuccessiveBy2Tags(in, "JJ", "NN", "NP");
        assertEquals(expected, actual);
    }

    public static Word w(String word, String tag) {
        return new Word(word, tag);
    }

    public static String readText(String name) throws IOException {
        InputStream inputStream = RelationFinder.class.getResourceAsStream(name);
        return IOUtils.toString(inputStream);
    }

}
