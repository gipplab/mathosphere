package com.formulasearchengine.mathosphere.mathpd;

import com.formulasearchengine.mathosphere.TestUtils;
import com.formulasearchengine.mathosphere.mathpd.contracts.TextExtractorMapper;
import com.formulasearchengine.mathosphere.mathpd.pojos.ArxivDocument;
import com.formulasearchengine.mathosphere.mathpd.pojos.ExtractedMathPDDocument;
import com.google.common.base.Throwables;
import org.junit.Test;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by Felix on 09.12.2016.
 */
public class DistancesTest {

    private static String decodePath(String urlEncodedPath) {
        try {
            return URLDecoder.decode(urlEncodedPath, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw Throwables.propagate(e);
        }
    }

    private static ExtractedMathPDDocument testResourceToExtractedMathPDDocument(String path) throws IOException, TransformerException, XPathExpressionException, ParserConfigurationException {
        final ArxivDocument arxivDocument = TextExtractorMapper.arxivTextToDocument(TestUtils.getFileContents(path));
        return TextExtractorMapper.convertArxivToExtractedMathPDDocument(arxivDocument);
    }

    private String resourcePath(String resourceName) {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(resourceName);
        return decodePath(resource.getFile());
    }

    @Test
    public void testDistanceSameFile() throws Exception {
        final String resourceSimple = "com/formulasearchengine/mathosphere/mathpd/simple.xhtml";
        final double similarity = Distances.distanceAbsoluteFeatures(testResourceToExtractedMathPDDocument(resourceSimple), testResourceToExtractedMathPDDocument(resourceSimple));

        assertTrue(similarity == 0.0);
    }


}