package de.tuberlin.dima.schubotz.fse;

import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;


public class QueryInputFormatTest {

    @Test
    public void testStringToNodePrincipal() throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {
        NodeList nodeList = XMLHelper.String2NodeList(TestUtils.getTestQueryString(), "/topics/topic");

        assertEquals(nodeList.getLength(), 50);
        final XPathExpression xNum = XMLHelper.compileX("./num");
        final XPathExpression xTQ = XMLHelper.compileX("./query/formula/math/semantics/annotation");///m:annotation");

        for (int i = 0, len = nodeList.getLength(); i < len; i++) {
            Node node = nodeList.item(i);
            final Node main = XMLHelper.getElementB(node, xNum);
            final String TopicName = main.getTextContent();
            assertEquals("NTCIR11-Math-" + (i + 1), TopicName);
            final Node tquery = XMLHelper.getElementB(node, xTQ);
            if (tquery != null)
                System.out.println(tquery.getTextContent());

        }
    }
}
