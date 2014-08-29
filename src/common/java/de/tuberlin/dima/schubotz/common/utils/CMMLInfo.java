package de.tuberlin.dima.schubotz.common.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

public class CMMLInfo {

    private Document cmmlDoc;

    private final static String MathHeader = "<?xml version=\"1.0\" ?>\n" +
            "<math xmlns=\"http://www.w3.org/1998/Math/MathML\">\n" +
            "<semantics>\n";
    private final static String MathFooter = "</semantics>\n" +
            "</math>";

    @SuppressWarnings("UnusedDeclaration")
    public CMMLInfo(Document cmml) {
        cmmlDoc = cmml;
    }

    public CMMLInfo(String cmml) throws IOException, ParserConfigurationException {
        cmmlDoc = XMLHelper.String2Doc(cmml, true);
    }

    public static CMMLInfo newFromSnippet(String snippet) throws IOException, ParserConfigurationException {
        return new CMMLInfo(MathHeader + snippet + MathFooter);
    }

    public CMMLInfo toStrictCmml() {
        try {
            cmmlDoc = XMLHelper.XslTransform(cmmlDoc, "de/tuberlin/dima/schubotz/common/utils/RobertMinerC2s.xsl");
        } catch (TransformerException | ParserConfigurationException e) {
            e.printStackTrace();
        }
        return this;
    }

    public boolean isEquation() throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {
        NodeList elementsB = XMLHelper.getElementsB(cmmlDoc, "/annotation-xml/apply/eq");
        return elementsB.getLength() > 0;
    }

    private void abstractNodeCD(Node node) {
        if (node.hasChildNodes()) {
            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                abstractNodeCD(childNodes.item(i));
            }
        } else {
            node.setTextContent("");
            return;
        }
        String cd;
        try {
            cd = node.getAttributes().getNamedItem("cd").getNodeValue();
        } catch (Exception e) {
            //TODO: Implement CD fallback
            cd = "na";
        }
        if (cd.equals("na"
        )) {
            return;
        }
        try {
            cmmlDoc.renameNode(node, null, cd);
        } catch (Error e) {
            e.printStackTrace();
            return;
        }
        node.setTextContent("");
    }


    public CMMLInfo abstract2CDs() {
        toStrictCmml();
        abstractNodeCD(cmmlDoc);
        return this;
    }

    public String toString() {
        try {
            return XMLHelper.printDocument(cmmlDoc);
        } catch (Exception e) {
            e.printStackTrace();
            return "cmml not printable";
        }
    }

    public Integer getMatchDepth(CMMLInfo other) {
        return Integer.MAX_VALUE;
    }
}
