package com.formulasearchengine.mathosphere.mathpd;

import com.formulasearchengine.mathosphere.mathpd.pojos.ArxivDocument;
import com.formulasearchengine.mathosphere.mml.CMMLInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by felix on 05.12.16.
 */
public class Distances {
    private static final Log LOG = LogFactory.getLog(Distances.class);

    public static float computeAbsoluteDistance(Map<String, Integer> h1, Map<String, Integer> h2) {
        float distance = 0;

        Set<String> keySet = new HashSet();

        keySet.addAll(h1.keySet());
        keySet.addAll(h2.keySet());

        for (String key : keySet) {
            float v1 = h1.get(key) == null ? 0 : h1.get(key);
            float v2 = h2.get(key) == null ? 0 : h2.get(key);

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
    protected static Map<String, Integer> contentElementsToHistogram(NodeList nodes) {
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
    protected static Map<String, Integer> histogramPlus(Map<String, Integer> h1, Map<String, Integer> h2) {
        final Set<String> mergedKeys = new HashSet<>(h1.keySet());
        mergedKeys.addAll(h2.keySet());
        final Map<String, Integer> mergedHistogram = new HashMap<>();

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
    public static Map<String, Integer> getDocumentHistogram(ArxivDocument d, String tagName) throws XPathExpressionException, ParserConfigurationException, TransformerException, IOException {
        Map<String, Integer> mergedHistogram = new HashMap<>();
        System.out.println("in getDocumentHistogram");
        for (int i = 0; i < d.getMathTags().getLength(); i++) {
            LOG.info("next math tag " + i + " of " + d.getMathTags().getLength());
            final Node mathTag = d.getMathTags().item(i);
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
    private static Map<String, Integer> strictCmmlInfoToHistogram(CMMLInfo strictCmml, String tagName) {
        final NodeList elements = strictCmml.getElementsByTagName(tagName);
        return contentElementsToHistogram(elements);
    }

    /**
     * compares frequencies of cn (numbers) and co (operators) on document level
     *
     * @param d1
     * @param d2
     * @throws TransformerException
     * @throws ParserConfigurationException
     */
    public static void testdist(ArxivDocument d1, ArxivDocument d2) throws TransformerException, ParserConfigurationException, XPathExpressionException, IOException {
        final String tagname = "cn";

        System.out.println("h1 start");
        Map<String, Integer> histogramDoc1 = getDocumentHistogram(d1, tagname);
        System.out.println("h2 start");
        Map<String, Integer> histogramDoc2 = getDocumentHistogram(d2, tagname);

        float absDist = computeAbsoluteDistance(histogramDoc1, histogramDoc2);
        System.out.println("absDist " + tagname + ": " + absDist);


        /*final CMMLInfo strictCmmlDoc1 = new CMMLInfo(d1).toStrictCmml();
        final CMMLInfo strictCmmlDoc2 = new CMMLInfo(d2).toStrictCmml();

        final NodeList contentNumberElements1 = strictCmmlDoc1.getElementsByTagName("cn");
        final NodeList contentNumberElements2 = strictCmmlDoc2.getElementsByTagName("cn");
        final NodeList contentOperatorElements1 = strictCmmlDoc1.getElementsByTagName("co");
        final NodeList contentOperatorElements2 = strictCmmlDoc2.getElementsByTagName("co");
        final NodeList contentIdentifierElements1 = strictCmmlDoc1.getElementsByTagName("ci");
        final NodeList contentIdentifierElements2 = strictCmmlDoc2.getElementsByTagName("ci");

        final Map<String, Integer> contentNumberHistogram1 = contentElementsToHistogram(contentNumberElements1);
        final Map<String, Integer> contentNumberHistogram2 = contentElementsToHistogram(contentNumberElements2);
        final Map<String, Integer> contentOperatorHistogram1 = contentElementsToHistogram(contentOperatorElements1);
        final Map<String, Integer> contentOperatorHistogram2 = contentElementsToHistogram(contentOperatorElements2);
        final Map<String, Integer> contentIdentifierHistogram1 = contentElementsToHistogram(contentIdentifierElements1);
        final Map<String, Integer> contentIdentifierHistogram2 = contentElementsToHistogram(contentIdentifierElements2);

        float absoluteDistanceContentNumbers = computeAbsoluteDistance(contentNumberHistogram1, contentNumberHistogram2);
        float absoluteDistanceContentOperators = computeAbsoluteDistance(contentOperatorHistogram1, contentOperatorHistogram2);
        float absoluteDistanceContentIdentifiers = computeAbsoluteDistance(contentIdentifierHistogram1, contentIdentifierHistogram2);

        LOG.info("numbers     = " + absoluteDistanceContentNumbers);
        LOG.info("operators   = " + absoluteDistanceContentOperators);
        LOG.info("identifiers = " + absoluteDistanceContentIdentifiers);*/
    }
}
