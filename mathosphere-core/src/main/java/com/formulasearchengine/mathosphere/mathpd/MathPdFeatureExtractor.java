package com.formulasearchengine.mathosphere.mathpd;

import com.formulasearchengine.mathosphere.mathpd.pojos.ArxivDocument;
import org.apache.flink.api.java.tuple.Tuple2;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.List;

/**
 * Extraction of basic features required for Math-PD use cases.
 */
public class MathPdFeatureExtractor {

    /**
     * Gets
     *
     * @param n
     * @return
     */
    private static String getNodeTextContent(Node n) {
        return n.getTextContent().trim();
    }

    /**
     * returns a list consisting of all content-element bigrams (each node represented by its tag name), i.e., always one leaf node that is a content element and its direct parent node
     * example:
     * e=mc^2 --> bigrams: e =; = m; m \times; c ^; ^ 2
     *
     * @param document
     * @return
     */
    public static List<Tuple2<String, String>> getBigramLeaves(ArxivDocument document) throws XPathExpressionException {
        final List<Tuple2<String, String>> bigramLeaves = new ArrayList<>();
        for (Node curLeafNode : document.getCElementLeafNodes()) {
            bigramLeaves.add(new Tuple2<>(getNodeTextContent(curLeafNode), getNodeTextContent(curLeafNode.getParentNode())));
            System.out.print(getNodeTextContent(curLeafNode));
            System.out.print(" ; ");
            System.out.println(getNodeTextContent(curLeafNode.getParentNode()));
        }

        return bigramLeaves;
    }


}
