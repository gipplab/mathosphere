package mlp.types;

import static org.junit.Assert.*;

import java.io.InputStream;

import mlp.utils.StringUtils;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WikiDocumentTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(WikiDocumentTest.class);

    @Test
    public void mathTagsGetReplaced() throws Exception {
        String text = "This is <math>m^2</math> a test <math>\\Phi</math> for the math replacer.";

        WikiDocument wikiDocument = new WikiDocument();
        LOGGER.debug("processing math...");
        wikiDocument.setText(text);
        LOGGER.debug("done processing math");

        String expectedText = "This is FORMULA_7f299a150feca7b2a93ea7a0b8367d53 a test "
                + "FORMULA_4b0d6ec86d70a39b0575b1159c15c3f1 for the math replacer.";
        assertEquals(expectedText, wikiDocument.getText().replace("\\s+", " "));

        System.out.println(wikiDocument.getFormulas());
        System.out.println(wikiDocument.getKnownIdentifiers());
    }

    @Test
    public void mathProcessingTest() throws Exception {
        InputStream stream = WikiDocument.class.getResourceAsStream("escaped.txt");
        String text = IOUtils.toString(stream);

        WikiDocument wikiDocument = new WikiDocument();
        LOGGER.debug("processing math...");
        wikiDocument.setText(text);
        LOGGER.debug("done processing math");
        assertFalse(text.equals(wikiDocument.getText()));

        LOGGER.debug("Formulas: {}", wikiDocument.getFormulas());
        LOGGER.debug("Extracted identifiers: {}", wikiDocument.getKnownIdentifiers());
    }

    @Test
    public void performanceTest() throws Exception {
        InputStream stream = WikiDocument.class.getResourceAsStream("schrodingerineq.txt");
        String rawText = IOUtils.toString(stream);

        LOGGER.debug("done reading the stream");
        LOGGER.debug("unescaping...");

        String text = StringUtils.unescapeEntities(rawText);
        LOGGER.debug("done unescaping");

        WikiDocument wikiDocument = new WikiDocument();
        LOGGER.debug("processing math...");
        wikiDocument.setText(text);
        LOGGER.debug("done processing math");

        LOGGER.debug("Extracted identifiers: {}", wikiDocument.getKnownIdentifiers());
        assertTrue(wikiDocument.getKnownIdentifiers().size() > 50);
    }

}
