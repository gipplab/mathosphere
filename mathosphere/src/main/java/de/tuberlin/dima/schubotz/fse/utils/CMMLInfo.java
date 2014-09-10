package de.tuberlin.dima.schubotz.fse.utils;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import de.tuberlin.dima.schubotz.mathmlquerygenerator.XQueryGenerator;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryExecutable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static de.tuberlin.dima.schubotz.fse.utils.XMLHelper.getElementsB;

public class CMMLInfo {
    protected static final SafeLogWrapper LOG = new SafeLogWrapper(CMMLInfo.class);
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
	private final static List formulaIndicators = Arrays.asList(
		"eq",
		"neq",
		"le",
		"ge",
		"leq",
		"geq",
		"equivalent"
	);
	private XQueryExecutable xQueryExecutable = null;
	private boolean isStrict;


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
	public CMMLInfo(CMMLInfo other){
		this.cmmlDoc = (Document) other.cmmlDoc.cloneNode( true );
	}
	public CMMLInfo clone(){
		return new CMMLInfo( this );
	}
    public static CMMLInfo newFromSnippet(String snippet) throws IOException, ParserConfigurationException {
        return new CMMLInfo(MathHeader + snippet + MathFooter);
    }

    public CMMLInfo toStrictCmml() {
        try {
            cmmlDoc = XMLHelper.XslTransform(cmmlDoc, "de/tuberlin/dima/schubotz/utils/RobertMinerC2s.xsl");
	        isStrict = true;
        } catch (TransformerException | ParserConfigurationException e) {
            e.printStackTrace();
        }
        return this;
    }

    public boolean isEquation() throws ParserConfigurationException, SAXException, XPathExpressionException, IOException, TransformerException {
        Node cmmlMain = XQueryGenerator.getMainElement(cmmlDoc);
        XPath xpath = XMLHelper.namespaceAwareXpath("m", "http://www.w3.org/1998/Math/MathML");
        XPathExpression xEquation = xpath.compile("./m:apply/*");
        NdLst elementsB = new NdLst(  getElementsB( cmmlMain, xEquation ) );
	    if(elementsB.getLength() > 0){
		    String name = elementsB.item( 0 ).getLocalName();
		    if ( formulaIndicators.contains( name )){
			    return true;
		    }
	    }
        return false;
    }
	public com.google.common.collect.Multiset<String> getElements (){
		try {
			Multiset<String> list = HashMultiset.create();
			XPath xpath = XMLHelper.namespaceAwareXpath("m", "http://www.w3.org/1998/Math/MathML");
			XPathExpression xEquation = xpath.compile("*//m:ci|*//m:co|*//m:cn");
			NdLst identifiers = new NdLst( (NodeList) xEquation.evaluate( cmmlDoc ,XPathConstants.NODESET ) );
			for ( Node identifier : identifiers ) {
				list.add(identifier.getTextContent().trim());
			}
			return list;
		} catch ( XPathExpressionException e ) {
			e.printStackTrace();
		}
		return HashMultiset.create();
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

    public Boolean isMatch(XQueryExecutable query ) {
	    Document doc = null;
	    try {
		    doc = XMLHelper.runXQuery( query, toString() );
		    final NodeList elementsB = doc.getElementsByTagName("p");
		    if( elementsB.getLength() ==0) {
			    return false;
		    } else {
			    return true;
		    }
	    } catch ( SaxonApiException e ) {
		    e.printStackTrace();
	    } catch ( ParserConfigurationException e ) {
		    e.printStackTrace();
	    }
        return null;
    }

    public Integer getDepth( XQueryExecutable query ) {
	    Document doc = null;
	    try {
		    doc = XMLHelper.runXQuery( query, toString() );
	    } catch ( Exception e ) {
            LOG.error("Problem during document preparation for depth processing" ,e );
		    return null;
	    }
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
	    if(xQueryExecutable == null) {
		    final String queryString = getXQueryString();
		    if ( queryString == null )
			    return null;
		    xQueryExecutable = XMLHelper.compileXQuerySting( queryString );
	    }
	    return xQueryExecutable;
    }

    public String getXQueryString() {
        final String queryString;
        XQueryGenerator gen = new XQueryGenerator(cmmlDoc);
        gen.setHeader(XQUERY_HEADER);
        gen.setFooter(XQUERY_FOOTER);
        queryString = gen.toString();
        return queryString;
    }

	public CMMLInfo toDataCmml () {
	try {
		cmmlDoc = XMLHelper.XslTransform(cmmlDoc, "de/tuberlin/dima/schubotz/utils/RobertMinerC2s.xsl");
	} catch (TransformerException | ParserConfigurationException e) {
		e.printStackTrace();
	}
	return this;
	}

	public Double getCoverage (Multiset queryTokens) {
		Multiset<String> our = getElements();
		if (our.contains( queryTokens ) ){
			return 1.;
		} else {
			return 0.;
		}
	}
}
