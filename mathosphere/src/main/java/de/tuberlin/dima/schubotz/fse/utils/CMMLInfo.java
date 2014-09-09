package de.tuberlin.dima.schubotz.fse.utils;

import de.tuberlin.dima.schubotz.mathmlquerygenerator.XQueryGenerator;
import net.sf.saxon.s9api.XQueryExecutable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

public class CMMLInfo {
	private final static String FN_PATH_FROM_ROOT="declare namespace functx = \"http://www.functx.com\";\n" +
                "declare function functx:path-to-node\n" +
                "  ( $nodes as node()* )  as xs:string* {\n" +
                "\n" +
                "$nodes/string-join(ancestor-or-self::*/name(.), '/')\n" +
                " } ;"
        ;
    // from http://x-query.com/pipermail/talk/2005-May/000607.html
    private final static String FN_PATH_FROM_ROOT2 = "declare function path-from-root($x as node()) {\n" +
            " if ($x/parent::*) then\n" +
            " concat( path-from-root($x/parent::*), \"/\", node-name($x) )\n" +
            " else\n" +
            " concat( \"/\", node-name($x) )\n" +
            " };\n";

    private static final String XQUERY_HEADER = "declare default element namespace \"http://www.w3.org/1998/Math/MathML\";\n" +
            FN_PATH_FROM_ROOT+
            "<result>{\n" +
            "let $m := .";
    private static final String XQUERY_FOOTER = "<element><x>{$x}</x><p>{data(functx:path-to-node($x))}</p></element>}\n" +
            "</result>";
    private Document cmmlDoc;

    private final static String MathHeader = "<?xml version=\"1.0\" ?>\n" +
            "<math xmlns=\"http://www.w3.org/1998/Math/MathML\">\n" +
            "<semantics>\n";
    private final static String MathFooter = "</semantics>\n" +
            "</math>";


    @SuppressWarnings("UnusedDeclaration")
    public CMMLInfo(Document cmml) {
        cmmlDoc = cmml;
        new XmlNamespaceTranslator()
                .addTranslation(null, "http://www.w3.org/1998/Math/MathML")
                .translateNamespaces(cmmlDoc);
    }

    public CMMLInfo(String cmml) throws IOException, ParserConfigurationException {
        cmmlDoc = XMLHelper.String2Doc(cmml, true);
        new XmlNamespaceTranslator()
                .addTranslation(null, "http://www.w3.org/1998/Math/MathML")
                .translateNamespaces(cmmlDoc);
    }

    public static CMMLInfo newFromSnippet(String snippet) throws IOException, ParserConfigurationException {
        return new CMMLInfo(MathHeader + snippet + MathFooter);
    }

    public CMMLInfo toStrictCmml() {
        try {
            cmmlDoc = XMLHelper.XslTransform(cmmlDoc, "de/tuberlin/dima/schubotz/utils/RobertMinerC2s.xsl");
        } catch (TransformerException | ParserConfigurationException e) {
            e.printStackTrace();
        }
        return this;
    }

    public boolean isEquation() throws ParserConfigurationException, SAXException, XPathExpressionException, IOException, TransformerException {
        Node cmmlMain = XQueryGenerator.getMainElement(cmmlDoc);
        XPath xpath = XMLHelper.namespaceAwareXpath("m", "http://www.w3.org/1998/Math/MathML");
        XPathExpression xEquation = xpath.compile("./m:apply/m:eq");

        NodeList elementsB = XMLHelper.getElementsB(cmmlMain, xEquation);
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
        //toStrictCmml();
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

    public Integer getDepth( XQueryExecutable query ) throws Exception {
        Document doc = XMLHelper.runXQuery(query, toString());
        final NodeList elementsB = doc.getElementsByTagName("p");
        if( elementsB.getLength() ==0) {
            return null;
        }
        Integer depth = Integer.MAX_VALUE;
        //find the match with lowest depth
        for (int i = 0; i < elementsB.getLength(); i++) {
            String path= elementsB.item(i).getTextContent();
            int currentDepth = path.split("/").length;
            if(currentDepth<depth)
                depth=currentDepth;
        }
        return depth;
    }

    public XQueryExecutable getXQuery() {
        final String queryString;
        XQueryGenerator gen = new XQueryGenerator(cmmlDoc);
        gen.setHeader(XQUERY_HEADER);
        gen.setFooter(XQUERY_FOOTER);
        queryString = gen.toString();
        if ( queryString == null )
            return null;
        return XMLHelper.compileXQuerySting(queryString);
    }
}
