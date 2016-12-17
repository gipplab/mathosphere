package com.formulasearchengine.mathosphere.mathpd.pojos;

import com.formulasearchengine.mathmlquerygenerator.xmlhelper.NonWhitespaceNodeList;
import com.formulasearchengine.mathmlquerygenerator.xmlhelper.XMLHelper;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.List;

public class ArxivDocument {

    public String title;
    public String text;

    // optional
    public String name;
    public String page;

    public ArxivDocument() {
    }

    public ArxivDocument(String title, String text) {
        this.title = title;
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public Document getDoc() {
        return XMLHelper.String2Doc(text, true);
    }

    public NonWhitespaceNodeList getMathTags() throws XPathExpressionException {
        return new NonWhitespaceNodeList(XMLHelper.getElementsB(getDoc(), "//*:math"));
    }

    public Multiset<String> getCElements() throws XPathExpressionException {
        final Multiset<String> identifiersFromCmml = HashMultiset.create();
        for (Node n : getMathTags()) {
            identifiersFromCmml.addAll(XMLHelper.getIdentifiersFromCmml(n));
        }
        return identifiersFromCmml;
    }

    /**
     * Returns an ordered list of all Content-Element leaf nodes of this document.
     *
     * @return
     * @throws XPathExpressionException
     */
    public List<Node> getCElementLeafNodes() throws XPathExpressionException {
        final List<Node> leafNodes = new ArrayList<>();
        for (Node n : getMathTags()) {
            final NodeList tmpLeafNodes = XMLHelper.getLeafNodesFromCmml(n);
            for (int i = 0; i < tmpLeafNodes.getLength(); i++) {
                leafNodes.add(tmpLeafNodes.item(i));
            }
        }
        return leafNodes;
    }

    @Override
    public String toString() {
        return "[title=" + title + ", text=" + StringUtils.abbreviate(text, 100) + "]";
    }

}
