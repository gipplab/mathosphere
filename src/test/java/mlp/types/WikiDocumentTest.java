package mlp.types;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import mlp.utils.StringUtils;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WikiDocumentTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(WikiDocumentTest.class);

    @Test
    public void mathProcessingTest() throws Exception {
        InputStream stream = WikiDocument.class.getResourceAsStream("escaped.txt");
        String text = IOUtils.toString(stream);

        WikiDocument wikiDocument = new WikiDocument();
        LOGGER.debug("processing math...");
        wikiDocument.setText(text);
        LOGGER.debug("done processing math");

        System.out.println(wikiDocument.getText().replaceAll("\\s+", " "));
        System.out.println(wikiDocument.getFormulas());
        System.out.println(wikiDocument.getKnownIdentifiers());
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
    }

}
