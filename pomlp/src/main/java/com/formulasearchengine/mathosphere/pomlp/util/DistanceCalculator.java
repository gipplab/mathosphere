package com.formulasearchengine.mathosphere.pomlp.util;

import com.formulasearchengine.mathmltools.xmlhelper.XMLHelper;
import com.formulasearchengine.mathosphere.mathpd.Distances;
import com.formulasearchengine.mathosphere.pomlp.comparison.ComparisonResult;
import com.formulasearchengine.mathosphere.pomlp.convertor.Converters;
import com.formulasearchengine.mathosphere.pomlp.xml.MathMLDocumentReader;
import it.unibz.inf.rted.convenience.RTED;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.util.HashMap;

/**
 * @author Andre Greiner-Petter
 */
public class DistanceCalculator {

    private static final Logger LOG = LogManager.getLogger(DistanceCalculator.class.getName());

    private Node goldTree, goldContent, goldPresentation;
    private Document goldDoc;
    private String goldDocString;
    private HashMap<String, Double> goldContentHistogramm;
    private HashMap<String, Double> goldPresentationHistogramm;

    private Exception e;

    public DistanceCalculator(String goldMML, boolean canonicalized) {
        MathMLDocumentReader mmlDocReader = new MathMLDocumentReader(goldMML);
        if (canonicalized) {
            mmlDocReader.canonicalize();
        }
        goldDoc = mmlDocReader.getDocument();
        goldTree = goldDoc.getDocumentElement();
        goldContent = mmlDocReader.getContentNode();
        goldPresentation = mmlDocReader.getPresentationNode();

        goldDocString = Utility.documentToString(goldDoc, true);
        try {
            goldContentHistogramm = Distances.contentElementsToHistogram(mmlDocReader.getAllNotApplyNodes());
            goldPresentationHistogramm = Distances.contentElementsToHistogram(mmlDocReader.getAllPresentationNodes());
        } catch (XPathExpressionException e) {
            LOG.warn("Cannot create histogram for non apply elements in content block.");
        }
    }

    public ComparisonResult calculateDistances(Converters converter, int index, MathMLDocumentReader refDoc) {
        e = null;
        ComparisonResult result = new ComparisonResult(index, converter);

        Node refContent = refDoc.getContentNode();
        Node refPres = refDoc.getPresentationNode();

        try {
            double totalDistance = RTED.computeDistance(goldTree, refDoc.getDocument().getDocumentElement());
            result.setStrucTotalDistance(totalDistance);

            if (refContent != null) {
                double contentDistance = RTED.computeDistance(goldContent, refContent);
                result.setStrucContentDistance(contentDistance);
            }
            if (refPres != null) {
                double presentationDistance = RTED.computeDistance(goldPresentation, refPres);
                result.setStrucPresentationDistance(presentationDistance);
            }
        } catch (Exception e) {
        }

        if (refContent == null) {
            return result;
        }



        HashMap<String, Double> contentHistogram = null;
        HashMap<String, Double> presentationHistogram = null;
        try {
            contentHistogram = Distances.contentElementsToHistogram(refDoc.getAllNotApplyNodes());
            presentationHistogram = Distances.contentElementsToHistogram(refDoc.getAllPresentationNodes());
        } catch ( Exception e ){
        }

        try {
            double absoluteDist = Distances.computeAbsoluteDistance(contentHistogram, goldContentHistogramm);
            result.setConthistoAbsoluteDist(absoluteDist);
        } catch (Exception e) {
        }
        try {
            double relativeDist = Distances.computeRelativeDistance(contentHistogram, goldContentHistogramm);
            result.setConthistoRelativeDist(relativeDist);
        } catch (Exception e) {
        }
        try {
            double cosineDist = Distances.computeCosineDistance(contentHistogram, goldContentHistogramm);
            result.setConthistoCosineDist(cosineDist);
        } catch (Exception e) {
        }
        try {
            double earthMoverDist = Distances.computeEarthMoverAbsoluteDistance(contentHistogram, goldContentHistogramm);
            result.setConthistoEarthMoverDist(earthMoverDist);
        } catch (Exception e) {
        }

        try {
            double absoluteDist = Distances.computeAbsoluteDistance(presentationHistogram, goldPresentationHistogramm);
            result.setHistoAbsoluteDist(absoluteDist);
        } catch (Exception e) {
        }
        try {
            double relativeDist = Distances.computeRelativeDistance(presentationHistogram, goldPresentationHistogramm);
            result.setHistoRelativeDist(relativeDist);
        } catch (Exception e) {
        }
        try {
            double cosineDist = Distances.computeCosineDistance(presentationHistogram, goldPresentationHistogramm);
            result.setHistoCosineDist(cosineDist);
        } catch (Exception e) {
        }
        try {
            double earthMoverDist = Distances.computeEarthMoverAbsoluteDistance(presentationHistogram, goldPresentationHistogramm);
            result.setHistoEarthMoverDist(earthMoverDist);
        } catch (Exception e) {
        }

        return result;
    }

    public Exception getException() {
        return e;
    }
}
