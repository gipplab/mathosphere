package com.formulasearchengine.mathosphere.mathpd;

import com.formulasearchengine.mathmltools.xmlhelper.NonWhitespaceNodeList;
import com.formulasearchengine.mathmltools.xmlhelper.XMLHelper;
import com.formulasearchengine.mathosphere.mathpd.distances.earthmover.EarthMoverDistanceWrapper;
import com.formulasearchengine.mathosphere.mathpd.distances.earthmover.JFastEMD;
import com.formulasearchengine.mathosphere.mathpd.distances.earthmover.Signature;
import com.formulasearchengine.mathosphere.mathpd.pojos.ArxivDocument;
import com.formulasearchengine.mathosphere.mathpd.pojos.ExtractedMathPDDocument;
import com.formulasearchengine.mathmltools.mml.CMMLInfo;
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
 * Created by Felix Hamborg <felixhamborg@gmail.com> on 05.12.16.
 */
public class Distances {
    private static final Log LOG = LogFactory.getLog(Distances.class);

    private static final DecimalFormat decimalFormat = new DecimalFormat("#.###");

    /**
     * probably only makes sense to compute this on CI
     *
     * @param h1
     * @param h2
     * @return
     */
    public static double computeEarthMoverAbsoluteDistance(Map<String, Integer> h1, Map<String, Integer> h2) {
        Signature s1 = EarthMoverDistanceWrapper.histogramToSignature(h1);
        Signature s2 = EarthMoverDistanceWrapper.histogramToSignature(h2);

        return JFastEMD.distance(s1, s2, 0.0);
    }

    public static double computeRelativeDistance(Map<String, Integer> h1, Map<String, Integer> h2) {
        final Set<String> keySet = new HashSet();
        keySet.addAll(h1.keySet());
        keySet.addAll(h2.keySet());
        final double numberOfUniqueElements = keySet.size();
        if (numberOfUniqueElements == 0.0) {
            return 0.0;
        }

        final double absoluteDistance = computeAbsoluteDistance(h1, h2);

        return absoluteDistance / numberOfUniqueElements;
    }


    /**
     * compares two histograms and returns the accumulated number of differences (absolute)
     *
     * @param h1
     * @param h2
     * @return
     */
    public static double computeAbsoluteDistance(Map<String, Integer> h1, Map<String, Integer> h2) {
        double distance = 0;

        final Set<String> keySet = new HashSet();
        keySet.addAll(h1.keySet());
        keySet.addAll(h2.keySet());

        for (String key : keySet) {
            double v1 = 0.0;
            double v2 = 0.0;
            if (h1.get(key) != null) {
                v1 = h1.get(key);
            }
            if (h2.get(key) != null) {
                v2 = h2.get(key);
            }

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
        LOG.debug("getDocumentHistogram(" + d.title + ", " + tagName + ")");
        HashMap<String, Integer> mergedHistogram = new HashMap<>();
        final NonWhitespaceNodeList allMathTags = d.getMathTags();
        for (int i = 0; i < allMathTags.getLength(); i++) {
            final Node mathTag = allMathTags.item(i);

            // this hack is necessary, as the converter that generates StrictCMML does not work correctly for CN, e.g., the number 3 is converted into a cn 10 as a base and a cs 3 as the actual number.
            if (tagName.equals("cn")) {
                mergedHistogram = histogramPlus(mergedHistogram, cmmlNodeToHistrogram(mathTag, tagName));
            } else {
                final CMMLInfo curStrictCmml = new CMMLInfo(mathTag).toStrictCmml();
                LOG.trace(curStrictCmml.toString());

                mergedHistogram = histogramPlus(mergedHistogram, strictCmmlInfoToHistogram(curStrictCmml, tagName));
            }
        }

        // cleanup
        cleanupHistogram(tagName, mergedHistogram);

        LOG.debug("getDocumentHistogram(" + d.title + ", " + tagName + "): " + mergedHistogram);

        return mergedHistogram;
    }

    /**
     * converts strict content math ml to a histogram for the given tagname, e.g., ci
     *
     * @param strictCmml
     * @param tagName
     * @return
     */
    private static HashMap<String, Integer> strictCmmlInfoToHistogram(CMMLInfo strictCmml, String tagName) {
        final NodeList elements = strictCmml.getElementsByTagName(tagName);
        return contentElementsToHistogram(elements);
    }

    /**
     * converts content math ml to a histogram for the given tagname, e.g., cn
     *
     * @param node
     * @param tagName
     * @return
     */
    private static HashMap<String, Integer> cmmlNodeToHistrogram(Node node, String tagName) throws XPathExpressionException {
        final NodeList elements = XMLHelper.getElementsB(node, "*//*:" + tagName);
        return contentElementsToHistogram(elements);
    }

    public static double distanceAbsoluteAllFeatures(ExtractedMathPDDocument f0, ExtractedMathPDDocument f1) {
        final double absoluteDistanceContentNumbers = computeAbsoluteDistance(f0.getHistogramCn(), f1.getHistogramCn());
        final double absoluteDistanceContentOperators = computeAbsoluteDistance(f0.getHistogramCsymbol(), f1.getHistogramCsymbol());
        final double absoluteDistanceContentIdentifiers = computeAbsoluteDistance(f0.getHistogramCi(), f1.getHistogramCi());
        final double absoluteDistanceBoundVariables = computeAbsoluteDistance(f0.getHistogramBvar(), f1.getHistogramBvar());

        LOG.debug("the following distances should all be 0");
        LOG.debug(getDocDescription(f0, f1) + "CN " + decimalFormat.format(absoluteDistanceContentNumbers));
        LOG.debug(getDocDescription(f0, f1) + "CSYMBOL " + decimalFormat.format(absoluteDistanceContentOperators));
        LOG.debug(getDocDescription(f0, f1) + "CI " + decimalFormat.format(absoluteDistanceContentIdentifiers));
        LOG.debug(getDocDescription(f0, f1) + "BVAR " + decimalFormat.format(absoluteDistanceBoundVariables));

        return absoluteDistanceContentNumbers + absoluteDistanceContentOperators + absoluteDistanceContentIdentifiers + absoluteDistanceBoundVariables;
    }

    public static double distanceRelativeAllFeatures(ExtractedMathPDDocument f0, ExtractedMathPDDocument f1) {
        final double relativeDistanceContentNumbers = computeRelativeDistance(f0.getHistogramCn(), f1.getHistogramCn());
        final double relativeDistanceContentOperators = computeRelativeDistance(f0.getHistogramCsymbol(), f1.getHistogramCsymbol());
        final double relativeDistanceContentIdentifiers = computeRelativeDistance(f0.getHistogramCi(), f1.getHistogramCi());
        final double relativeDistanceBoundVariables = computeRelativeDistance(f0.getHistogramBvar(), f1.getHistogramBvar());

        LOG.debug("the following distances should all be 0");
        LOG.debug(getDocDescription(f0, f1) + "CN " + decimalFormat.format(relativeDistanceContentNumbers));
        LOG.debug(getDocDescription(f0, f1) + "CSYMBOL " + decimalFormat.format(relativeDistanceContentOperators));
        LOG.debug(getDocDescription(f0, f1) + "CI " + decimalFormat.format(relativeDistanceContentIdentifiers));
        LOG.debug(getDocDescription(f0, f1) + "BVAR " + decimalFormat.format(relativeDistanceBoundVariables));


        return relativeDistanceContentNumbers + relativeDistanceContentOperators + relativeDistanceContentIdentifiers + relativeDistanceBoundVariables;
    }

    private static String getDocDescription(ExtractedMathPDDocument f0, ExtractedMathPDDocument f1) {
        return "{" + f0.getTitle() + "; " + f1.getTitle() + "} ";
    }

    /**
     * this cleanup is necessary due to errors in the xslt conversion script (contentmathmml to strict cmml)
     *
     * @param tagName
     * @param histogram
     */
    private static void cleanupHistogram(String tagName, HashMap<String, Integer> histogram) {
        switch (tagName) {
            case "csymbol":
                histogram.remove("based_integer");
                break;
            case "ci":
                histogram.remove("integer");
                break;
        }
    }

}
