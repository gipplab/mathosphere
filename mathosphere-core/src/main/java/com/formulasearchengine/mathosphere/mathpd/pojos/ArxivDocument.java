package com.formulasearchengine.mathosphere.mathpd.pojos;

import com.formulasearchengine.mathmlquerygenerator.xmlhelper.NonWhitespaceNodeList;
import com.formulasearchengine.mathmlquerygenerator.xmlhelper.XMLHelper;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathExpressionException;

public class ArxivDocument {

    public String title;
    public String text;

    public ArxivDocument() {
    }

    public ArxivDocument(String title, String text) {
        this.title = title;
        this.text = text;
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
             identifiersFromCmml.addAll( XMLHelper.getIdentifiersFromCmml(n));
        }
        return identifiersFromCmml;
    }

    @Override
    public String toString() {
        return "[title=" + title + ", text=" + StringUtils.abbreviate(text, 100) + "]";
    }

}
