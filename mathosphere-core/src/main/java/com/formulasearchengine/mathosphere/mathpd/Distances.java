package com.formulasearchengine.mathosphere.mathpd;

import com.formulasearchengine.mathmltools.helper.XMLHelper;
import com.formulasearchengine.mathmltools.mml.CMMLInfo;
import com.formulasearchengine.mathmltools.xml.NonWhitespaceNodeList;
import com.formulasearchengine.mathosphere.mathpd.distances.earthmover.EarthMoverDistanceWrapper;
import com.formulasearchengine.mathosphere.mathpd.distances.earthmover.JFastEMD;
import com.formulasearchengine.mathosphere.mathpd.distances.earthmover.Signature;
import com.formulasearchengine.mathosphere.mathpd.pojos.ArxivDocument;
import com.formulasearchengine.mathosphere.mathpd.pojos.ExtractedMathPDDocument;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

//import com.formulasearchengine.mathmltools.xmlhelper.NonWhitespaceNodeList;
//import com.formulasearchengine.mathmltools.xmlhelper.XMLHelper;

/**
 * Created by Felix Hamborg <felixhamborg@gmail.com> on 05.12.16.
 */
public class Distances {
    private static final Logger LOG = LogManager.getLogger(Distances.class.getName());

    private static final DecimalFormat decimalFormat = new DecimalFormat("#.###");

    /**
     * probably only makes sense to compute this on CI
     *
     * @param h1
     * @param h2
     * @return
     */
    public static double computeEarthMoverAbsoluteDistance(Map<String, Double> h1, Map<String, Double> h2) {
        Signature s1 = EarthMoverDistanceWrapper.histogramToSignature(h1);
        Signature s2 = EarthMoverDistanceWrapper.histogramToSignature(h2);

        return JFastEMD.distance(s1, s2, 0.0);
    }

    public static double computeRelativeDistance(Map<String, Double> h1, Map<String, Double> h2) {
        int totalNumberOfElements = 0;
        for (Double frequency : h1.values()) {
            totalNumberOfElements += frequency;
        }
        for (Double frequency : h2.values()) {
            totalNumberOfElements += frequency;
        }
        if (totalNumberOfElements == 0) {
            return 0.0;
        }

        final double absoluteDistance = computeAbsoluteDistance(h1, h2);

        return absoluteDistance / totalNumberOfElements;
    }


    /**
     * compares two histograms and returns the accumulated number of differences (absolute)
     *
     * @param h1
     * @param h2
     * @return
     */
    public static double computeAbsoluteDistance(Map<String, Double> h1, Map<String, Double> h2) {
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
    protected static HashMap<String, Double> contentElementsToHistogram(NodeList nodes) {
        final HashMap<String, Double> histogram = new HashMap<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            String contentElementName = node.getTextContent().trim();
            // increment frequency by 1
            histogram.put(contentElementName, histogram.getOrDefault(contentElementName, 0.0) + 1.0);
        }

        return histogram;
    }

    /**
     * Adds all elements from all histogram
     *
     * @return
     */
    public static Map<String, Double> histogramsPlus(List<Map<String, Double>> histograms) {
        return histogramsPlus(histograms.toArray(new HashMap[histograms.size()]));
    }

    /**
     * Adds all elements from all histogram
     *
     * @return
     */
    @SafeVarargs
    public static Map<String, Double> histogramsPlus(Map<String, Double>... histograms) {
        switch (histograms.length) {
            case 0:
                throw new IllegalArgumentException("histograms.length=" + histograms.length + "; needs to be >= 2");
                // return null;
            case 1:
                return histograms[0];
        }


        final Set<String> mergedKeys = new HashSet<>();
        for (Map<String, Double> histogram : histograms) {
            mergedKeys.addAll(histogram.keySet());
        }
        final HashMap<String, Double> mergedHistogram = new HashMap<>();

        for (String key : mergedKeys) {
            double value = 0.0;
            for (Map<String, Double> histogram : histograms) {
                value += histogram.getOrDefault(key, 0.0);
            }
            mergedHistogram.put(key, value);
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
    public static Map<String, Double> getDocumentHistogram(ArxivDocument d, String tagName, NonWhitespaceNodeList allMathTagsOfDOc) throws XPathExpressionException, ParserConfigurationException, TransformerException, IOException {
        LOG.debug("getDocumentHistogram(" + d.title + ", " + tagName + ")");
        Map<String, Double> mergedHistogram = new HashMap<>();
        final NonWhitespaceNodeList allMathTags = (allMathTagsOfDOc != null) ? allMathTagsOfDOc : d.getMathTags();
        for (int i = 0; i < allMathTags.getLength(); i++) {
            final Node mathTag = allMathTags.item(i);

            // this hack is necessary, as the converter that generates StrictCMML does not work correctly for CN, e.g., the number 3 is converted into a cn 10 as a base and a cs 3 as the actual number.
            if (tagName.equals("cn")) {
                mergedHistogram = histogramsPlus(mergedHistogram, cmmlNodeToHistrogram(mathTag, tagName));
            } else {
                final CMMLInfo curStrictCmml = new CMMLInfo(mathTag).toStrictCmml();
                LOG.debug(curStrictCmml.toString());

                mergedHistogram = histogramsPlus(mergedHistogram, strictCmmlInfoToHistogram(curStrictCmml, tagName));
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
    private static HashMap<String, Double> strictCmmlInfoToHistogram(CMMLInfo strictCmml, String tagName) {
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
    private static HashMap<String, Double> cmmlNodeToHistrogram(Node node, String tagName) throws XPathExpressionException {
        final NodeList elements = XMLHelper.getElementsB(node, "*//*:" + tagName);
        return contentElementsToHistogram(elements);
    }

    public static Tuple4<Double, Double, Double, Double> distanceAbsoluteAllFeatures(ExtractedMathPDDocument f0, ExtractedMathPDDocument f1) {
        final double absoluteDistanceContentNumbers = computeAbsoluteDistance(f0.getHistogramCn(), f1.getHistogramCn());
        final double absoluteDistanceContentOperators = computeAbsoluteDistance(f0.getHistogramCsymbol(), f1.getHistogramCsymbol());
        final double absoluteDistanceContentIdentifiers = computeAbsoluteDistance(f0.getHistogramCi(), f1.getHistogramCi());
        final double absoluteDistanceBoundVariables = computeAbsoluteDistance(f0.getHistogramBvar(), f1.getHistogramBvar());

        LOG.debug("the following distances should all be 0");
        LOG.debug(getDocDescription(f0, f1) + "CN " + decimalFormat.format(absoluteDistanceContentNumbers));
        LOG.debug(getDocDescription(f0, f1) + "CSYMBOL " + decimalFormat.format(absoluteDistanceContentOperators));
        LOG.debug(getDocDescription(f0, f1) + "CI " + decimalFormat.format(absoluteDistanceContentIdentifiers));
        LOG.debug(getDocDescription(f0, f1) + "BVAR " + decimalFormat.format(absoluteDistanceBoundVariables));

        return new Tuple4<>(absoluteDistanceContentNumbers, absoluteDistanceContentOperators, absoluteDistanceContentIdentifiers, absoluteDistanceBoundVariables);
    }

    public static double computeCosineDistance(Map<String, Double> h1, Map<String, Double> h2) {
        final Set<String> mergedKeys = new HashSet<>(h1.keySet());
        mergedKeys.addAll(h2.keySet());

        // if both histograms are empty, they are same
        if (h1.size() + h2.size() == 0) {
            return -10.0; // tmp value for development, TODO, replace with 1.0 once finished.
        }

        // if at least one histogram is not empty but no keys are shared, the documents will be completely different
        if (mergedKeys.isEmpty()) {
            return 0.0;
        }

        // https://en.wikipedia.org/wiki/Cosine_similarity
        double numerator = 0.0;
        for (String key : mergedKeys) {
            numerator += (h1.getOrDefault(key, 0.0) * h2.getOrDefault(key, 0.0));
        }

        double denominator1 = 0.0;
        for (String key : h1.keySet()) {
            double value = h1.get(key);
            denominator1 += (value * value);
        }
        denominator1 = Math.sqrt(denominator1);

        double denominator2 = 0.0;
        for (String key : h2.keySet()) {
            double value = h2.get(key);
            denominator2 += (value * value);
        }
        denominator2 = Math.sqrt(denominator2);

        return numerator / (denominator1 * denominator2);
    }

    public static Tuple4<Double, Double, Double, Double> distanceCosineAllFeatures(ExtractedMathPDDocument f0, ExtractedMathPDDocument f1) {
        final double cosineDistanceContentNumbers = computeCosineDistance(f0.getHistogramCn(), f1.getHistogramCn());
        final double cosineDistanceContentOperators = computeCosineDistance(f0.getHistogramCsymbol(), f1.getHistogramCsymbol());
        final double cosineDistanceContentIdentifiers = computeCosineDistance(f0.getHistogramCi(), f1.getHistogramCi());
        final double cosineDistanceBoundVariables = computeCosineDistance(f0.getHistogramBvar(), f1.getHistogramBvar());

        LOG.debug(getDocDescription(f0, f1) + "CN " + decimalFormat.format(cosineDistanceContentNumbers));
        LOG.debug(getDocDescription(f0, f1) + "CSYMBOL " + decimalFormat.format(cosineDistanceContentOperators));
        LOG.debug(getDocDescription(f0, f1) + "CI " + decimalFormat.format(cosineDistanceContentIdentifiers));
        LOG.debug(getDocDescription(f0, f1) + "BVAR " + decimalFormat.format(cosineDistanceBoundVariables));

        return new Tuple4<>(cosineDistanceContentNumbers,
                cosineDistanceContentOperators,
                cosineDistanceContentIdentifiers,
                cosineDistanceBoundVariables);
    }

    public static Tuple4<Double, Double, Double, Double> distanceRelativeAllFeatures(ExtractedMathPDDocument f0, ExtractedMathPDDocument f1) {
        final double relativeDistanceContentNumbers = computeRelativeDistance(f0.getHistogramCn(), f1.getHistogramCn());
        final double relativeDistanceContentOperators = computeRelativeDistance(f0.getHistogramCsymbol(), f1.getHistogramCsymbol());
        final double relativeDistanceContentIdentifiers = computeRelativeDistance(f0.getHistogramCi(), f1.getHistogramCi());
        final double relativeDistanceBoundVariables = computeRelativeDistance(f0.getHistogramBvar(), f1.getHistogramBvar());

        LOG.debug(getDocDescription(f0, f1) + "CN " + decimalFormat.format(relativeDistanceContentNumbers));
        LOG.debug(getDocDescription(f0, f1) + "CSYMBOL " + decimalFormat.format(relativeDistanceContentOperators));
        LOG.debug(getDocDescription(f0, f1) + "CI " + decimalFormat.format(relativeDistanceContentIdentifiers));
        LOG.debug(getDocDescription(f0, f1) + "BVAR " + decimalFormat.format(relativeDistanceBoundVariables));

        return new Tuple4<>(relativeDistanceContentNumbers,
                relativeDistanceContentOperators,
                relativeDistanceContentIdentifiers,
                relativeDistanceBoundVariables);
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
    private static void cleanupHistogram(String tagName, Map<String, Double> histogram) {
        switch (tagName) {
            case "csymbol":
                histogram.remove("based_integer");
                for (String key : ValidCSymbols.VALID_CSYMBOLS) {
                    histogram.remove(key);
                }
                break;
            case "ci":
                histogram.remove("integer");
                break;
            case "cn":
                Set<String> toberemovedKeys = new HashSet<>();
                for (String key : histogram.keySet()) {
                    if (!isNumeric(key)) {
                        toberemovedKeys.add(key);
                    }
                }
                // now we can remove the keys
                for (String key : toberemovedKeys) {
                    histogram.remove(key);
                }
                break;
        }
    }

    private static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }
}
