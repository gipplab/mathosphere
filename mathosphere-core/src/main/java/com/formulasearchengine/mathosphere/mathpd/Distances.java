package com.formulasearchengine.mathosphere.mathpd;

import com.formulasearchengine.mathmlquerygenerator.xmlhelper.NonWhitespaceNodeList;
import com.formulasearchengine.mathosphere.mathpd.pojos.ArxivDocument;
import com.formulasearchengine.mathosphere.mathpd.pojos.ExtractedMathPDDocument;
import com.formulasearchengine.mathosphere.mml.CMMLInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by felix on 05.12.16.
 */
public class Distances {
    private static final Log LOG = LogFactory.getLog(Distances.class);

    private static final DecimalFormat decimalFormat = new DecimalFormat("#.###");

    public static double computeAbsoluteSimilarity(Map<String, Integer> h1, Map<String, Integer> h2) {
        double distance = 0;

        Set<String> keySet = new HashSet();

        keySet.addAll(h1.keySet());
        keySet.addAll(h2.keySet());

        for (String key : keySet) {
            double v1 = h1.get(key) == null ? 0 : h1.get(key);
            double v2 = h2.get(key) == null ? 0 : h2.get(key);

            distance += Math.abs(v1 - v2);
        }

        return distance;
    }

    /**
     * Returns a map of the names and their accumulated frequency of the given content-elements (that could be identifiers, numbers, or operators)
     *
     * @param nodes
     * @return
     */
    protected static HashMap<String, Integer> contentElementsToHistogram(NodeList nodes) {
        final HashMap<String, Integer> histogram = new HashMap<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            String contentElementName = node.getTextContent().trim();
            // increment frequency by 1
            histogram.put(contentElementName, histogram.getOrDefault(contentElementName, 0) + 1);
        }

        return histogram;
    }

    /**
     * Adds all elements from one histogram to the other
     *
     * @param h1
     * @param h2
     * @return
     */
    protected static HashMap<String, Integer> histogramPlus(HashMap<String, Integer> h1, HashMap<String, Integer> h2) {
        final Set<String> mergedKeys = new HashSet<>(h1.keySet());
        mergedKeys.addAll(h2.keySet());
        final HashMap<String, Integer> mergedHistogram = new HashMap<>();

        for (String key : mergedKeys) {
            mergedHistogram.put(
                    key,
                    h1.getOrDefault(key, 0)
                            + h2.getOrDefault(key, 0)
            );
        }

        return mergedHistogram;
    }

    /**
     * Returns an absolute histogram of the whole document d with all elements that match tagname. The key in the histogram is the element's name.
     *
     * @param d
     * @param tagName
     * @return
     * @throws XPathExpressionException
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    public static HashMap<String, Integer> getDocumentHistogram(ArxivDocument d, String tagName) throws XPathExpressionException, ParserConfigurationException, TransformerException, IOException {
        HashMap<String, Integer> mergedHistogram = new HashMap<>();
        final NonWhitespaceNodeList allMathTags = d.getMathTags();
        for (int i = 0; i < allMathTags.getLength(); i++) {
            final Node mathTag = allMathTags.item(i);
            final CMMLInfo curStrictCmml = new CMMLInfo(mathTag).toStrictCmml();

            mergedHistogram = histogramPlus(mergedHistogram, strictCmmlInfoToHistogram(curStrictCmml, tagName));
        }

        return mergedHistogram;
    }

    /**
     * converts strict content math ml to a histogram for the given tagname, e.g., cn
     *
     * @param strictCmml
     * @param tagName
     * @return
     */
    private static HashMap<String, Integer> strictCmmlInfoToHistogram(CMMLInfo strictCmml, String tagName) {
        final NodeList elements = strictCmml.getElementsByTagName(tagName);
        return contentElementsToHistogram(elements);
    }

    public static void testdist(ExtractedMathPDDocument f0, ExtractedMathPDDocument f1) {
        final double absoluteDistanceContentNumbers = computeAbsoluteSimilarity(f0.getHistogramCn(), f1.getHistogramCn());
        final double absoluteDistanceContentOperators = computeAbsoluteSimilarity(f0.getHistogramCo(), f1.getHistogramCo());
        final double absoluteDistanceContentIdentifiers = computeAbsoluteSimilarity(f0.getHistogramCi(), f1.getHistogramCo());

        LOG.info(getDocDescription(f0, f1) + "CN " + decimalFormat.format(absoluteDistanceContentNumbers));
        LOG.info(getDocDescription(f0, f1) + "CO " + decimalFormat.format(absoluteDistanceContentOperators));
        LOG.info(getDocDescription(f0, f1) + "CI " + decimalFormat.format(absoluteDistanceContentIdentifiers));
    }

    private static String getDocDescription(ExtractedMathPDDocument f0, ExtractedMathPDDocument f1) {
        return "{" + f0.getTitle() + "; " + f1.getTitle() + "} ";
    }
}
