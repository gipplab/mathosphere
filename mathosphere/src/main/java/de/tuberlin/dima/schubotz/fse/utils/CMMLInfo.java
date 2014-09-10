package de.tuberlin.dima.schubotz.fse.utils;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import de.tuberlin.dima.schubotz.mathmlquerygenerator.XQueryGenerator;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryExecutable;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.*;

import static de.tuberlin.dima.schubotz.fse.utils.XMLHelper.getElementsB;

public class CMMLInfo implements Document {
	protected static final SafeLogWrapper LOG = new SafeLogWrapper( CMMLInfo.class );
	private final static String FN_PATH_FROM_ROOT = "declare namespace functx = \"http://www.functx.com\";\n" +
		"declare function functx:path-to-node\n" +
		"  ( $nodes as node()* )  as xs:string* {\n" +
		"\n" +
		"$nodes/string-join(ancestor-or-self::*/name(.), '/')\n" +
		" } ;";
	private static final String XQUERY_HEADER = "declare default element namespace \"http://www.w3.org/1998/Math/MathML\";\n" +
		FN_PATH_FROM_ROOT +
		"<result>{\n" +
		"let $m := .";
	// from http://x-query.com/pipermail/talk/2005-May/000607.html
	private final static String FN_PATH_FROM_ROOT2 = "declare function path-from-root($x as node()) {\n" +
		" if ($x/parent::*) then\n" +
		" concat( path-from-root($x/parent::*), \"/\", node-name($x) )\n" +
		" else\n" +
		" concat( \"/\", node-name($x) )\n" +
		" };\n";
	private static final String XQUERY_FOOTER = "<element><x>{$x}</x><p>{data(functx:path-to-node($x))}</p></element>}\n" +
		"</result>";
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
	private Document cmmlDoc;
	private XQueryExecutable xQueryExecutable = null;
	private boolean isStrict;

	public CMMLInfo (Document cmml) {
		constructor( cmml, true, false );
	}


	public CMMLInfo (String s) throws IOException, ParserConfigurationException {
		Document cmml = XMLHelper.String2Doc( s, true );
		constructor( cmml, true, false );
	}

	public CMMLInfo (CMMLInfo other) {
		this.cmmlDoc = (Document) other.cmmlDoc.cloneNode( true );
	}

	public CMMLInfo (Node f2) throws TransformerException, IOException, ParserConfigurationException {
		//TODO: Improve performance here
		Document cmml = XMLHelper.String2Doc( XMLHelper.printDocument( f2 ), true );
		constructor( cmml,true,false );
	}

	public static CMMLInfo newFromSnippet (String snippet) throws IOException, ParserConfigurationException {
		return new CMMLInfo( MathHeader + snippet + MathFooter );
	}

	public Document getDoc () {
		return cmmlDoc;
	}

	private void fixNamespaces () {
		Node math = (new NonWhitespaceNodeList( cmmlDoc.getElementsByTagNameNS( "*", "math" ) )).getFirstElement();
		;
		if ( math == null ) {
			try {
				LOG.error( "No mathml element found in", XMLHelper.printDocument( cmmlDoc ) );
			} catch ( TransformerException e ) {
				LOG.error( "No mathml element found in unpritnabel input." );
			}
			return;
		}
		try {
            math.getAttributes().removeNamedItem( "xmlns" );
        } catch (final DOMException e) {
            //Remove if it exists, ignore any errors thrown if it does not exist
        }
		new XmlNamespaceTranslator()
			.setDefaultNamespace(ExtractHelper.NAMESPACE)
			.addTranslation( "m", ExtractHelper.NAMESPACE)
			.addTranslation( "mws", "http://search.mathweb.org/ns" )
				//TODO: make option to keep it
			.addUnwantedAttribute( "xml:id" )
			.translateNamespaces( cmmlDoc );
		try {
			math.getAttributes().removeNamedItem( "xmlns:m" );
		} catch (final DOMException e) {
            //Ignore any errors thrown if element does not exist
        }
	}

	private void removeElementsByName (String name) {
		final NonWhitespaceNodeList nodes = new NonWhitespaceNodeList( cmmlDoc.getElementsByTagNameNS( "*", name ) );
		for (final Node node : nodes ) {
			// be sure not to remove content MathML
			if(! node.getAttributes().getNamedItem( "encoding" ).getTextContent().equals( "MathML-Content" )){
				node.getParentNode().removeChild( node );
			}
		}
	}

	private void removeAnnotations () {
		removeElementsByName( "annotation" );
		removeElementsByName( "annotation-xml" );
	}

	private void constructor (Document cmml, Boolean fixNamespace, Boolean preserveAnnotations) {
		cmmlDoc = cmml;
		if ( fixNamespace ) {
			fixNamespaces();
		}
		if ( !preserveAnnotations ) {
			removeAnnotations();
		}
		removeElementsByName( "id" );
	}

	public CMMLInfo clone () {
		return new CMMLInfo( this );
	}

	private void removeNonCD () {

	}

	public CMMLInfo toStrictCmmlCont () {
		try {
			removeNonCD();
			cmmlDoc = XMLHelper.XslTransform( cmmlDoc, "de/tuberlin/dima/schubotz/utils/RobertMinerC2s.xsl" );
			isStrict = true;
		} catch (final TransformerException | ParserConfigurationException e ) {
            LOG.warn("Unable to convert to strict cmml ", cmmlDoc, e);
		}
		return this;
	}

	public CMMLInfo toStrictCmml () throws TransformerException, ParserConfigurationException {
		cmmlDoc = XMLHelper.XslTransform( cmmlDoc, "de/tuberlin/dima/schubotz/utils/RobertMinerC2s.xsl" );
		return this;
	}

	public boolean isEquation () throws ParserConfigurationException, SAXException, XPathExpressionException, IOException, TransformerException {
		Node cmmlMain = XQueryGenerator.getMainElement( cmmlDoc );
		XPath xpath = XMLHelper.namespaceAwareXpath( "m", "http://www.w3.org/1998/Math/MathML" );
		XPathExpression xEquation = xpath.compile( "./m:apply/*" );
		//XPathExpression xEquation = xpath.compile( "m:math/m:semantics/m:apply/*|m:math/m:semantics/m:annotation-xml/m:apply/*" );

		NonWhitespaceNodeList elementsB = new NonWhitespaceNodeList( getElementsB( cmmlMain, xEquation ) );
		if ( elementsB.getLength() > 0 ) {
			String name = elementsB.item( 0 ).getLocalName();
			if ( formulaIndicators.contains( name ) ) {
				return true;
			}
		}
		return false;
	}

	public Multiset<String> getElements () {
		try {
			Multiset<String> list = HashMultiset.create();
			XPath xpath = XMLHelper.namespaceAwareXpath( "m", "http://www.w3.org/1998/Math/MathML" );
			XPathExpression xEquation = xpath.compile( "*//m:ci|*//m:co|*//m:cn" );
			NonWhitespaceNodeList identifiers = new NonWhitespaceNodeList( (NodeList) xEquation.evaluate( cmmlDoc, XPathConstants.NODESET ) );
			for ( Node identifier : identifiers ) {
				list.add( identifier.getTextContent().trim() );
			}
			return list;
		} catch (final XPathExpressionException e ) {
            LOG.warn("Unable to parse elements: ", cmmlDoc, e);
		}
		return HashMultiset.create();
	}

	private void abstractNodeCD (Node node) {
		if ( node.hasChildNodes() ) {
			final NodeList childNodes = node.getChildNodes();
			for ( int i = 0; i < childNodes.getLength(); i++ ) {
				abstractNodeCD( childNodes.item( i ) );
			}
		} else {
			node.setTextContent( "" );
			return;
		}
		String cd;
		try {
			cd = node.getAttributes().getNamedItem( "cd" ).getNodeValue();
		} catch (final DOMException e ) {
			//TODO: Implement CD fallback
			cd = "";
		}
		if ( cd.equals( ""
		) ) {
			return;
		}
		try {
			cmmlDoc.renameNode( node, "http://formulasearchengine.com/ns/pseudo/gen/cd", cd );
		} catch (final DOMException e) {
			LOG.error( "cannot rename",node.getLocalName(),e,cmmlDoc );
			return;
		}
		node.setTextContent( "" );
	}

	private void abstractNodeDT (Node node, Integer applies) {
		Set<String> levelGenerators = Sets.newHashSet( "apply", "bind" );
		Map<String, Integer> DTa = new HashMap<>();
		Boolean rename = false;
		DTa.put( "cn", 0 );
		DTa.put( "cs", 0 );
		DTa.put( "bvar", 0 );
		DTa.put( "ci", null );
		DTa.put( "csymbol", 1 );
		DTa.put( "share", 5 );

		Integer level = applies;
		final String name = node.getLocalName();
		if ( node.hasChildNodes() ) {
			if ( name != null && levelGenerators.contains( name ) ) {
				applies++;
			} else {
				applies = 0;
			}
			NodeList childNodes = node.getChildNodes();
			for ( int i = 0; i < childNodes.getLength(); i++ ) {
				if ( i == 0 ) {
					abstractNodeDT( childNodes.item( i ), applies );
				} else {
					abstractNodeDT( childNodes.item( i ), 0 );
				}
			}
		} else {
			node.setTextContent( "" );
			return;
		}

		if ( DTa.containsKey( name ) ) {
			if ( DTa.get( name ) != null ) {
				level = DTa.get( name );
			}
			rename = true;
		}
		if ( name != null && rename ) {
			try {
				cmmlDoc.renameNode( node, "http://formulasearchengine.com/ns/pseudo/gen/datatype", "l" + level );
			} catch (final DOMException e) {
                LOG.info( "could not rename node" + name );
				return;
			}
		}
		if ( node.getNodeType() == TEXT_NODE ) {
			node.setTextContent( "" );
		}
	}

	public CMMLInfo abstract2CDs () {
		abstractNodeCD( cmmlDoc );
		fixNamespaces();
		return this;
	}

	public Node abstract2DTs () {
		abstractNodeDT( cmmlDoc, 0 );
		fixNamespaces();
		return this;
	}

	public String toString () {
		try {
			return XMLHelper.printDocument( cmmlDoc );
		} catch (final TransformerException e) {
			return "cmml not printable";
		}
	}

	public Boolean isMatch (XQueryExecutable query) {
		Document doc = null;
		try {
			doc = XMLHelper.runXQuery( query, toString() );
			final NodeList elementsB = doc.getElementsByTagName( "p" );
            return elementsB.getLength() != 0;
		} catch (final SaxonApiException|ParserConfigurationException e ) {
            LOG.warn("Unable to parse if query is a match. ", e);
		}
		return null;
	}

	public Integer getDepth (XQueryExecutable query) {
		Document doc = null;
		try {
			doc = XMLHelper.runXQuery( query, toString() );
		} catch (final SaxonApiException|ParserConfigurationException e) {
			LOG.error( "Problem during document preparation for depth processing", e );
			return null;
		}
		final NodeList elementsB = doc.getElementsByTagName( "p" );
		if ( elementsB.getLength() == 0 ) {
			return null;
		}
		Integer depth = Integer.MAX_VALUE;
		//find the match with lowest depth
		for ( int i = 0; i < elementsB.getLength(); i++ ) {
			final String path = elementsB.item( i ).getTextContent();
			final int currentDepth = path.split( "/" ).length;
			if ( currentDepth < depth ) {
                depth = currentDepth;
            }
		}
		return depth;
	}

	public XQueryExecutable getXQuery (Boolean useCache) {
		if ( xQueryExecutable == null && !useCache ) {
			getXQuery();
		}
		return xQueryExecutable;
	}

	public XQueryExecutable getXQuery () {

		final String queryString = getXQueryString();
		if ( queryString == null ) {
            return null;
        }
		xQueryExecutable = XMLHelper.compileXQuerySting( queryString );
		return xQueryExecutable;
	}

	public String getXQueryString () {
        final XQueryGenerator gen = new XQueryGenerator( cmmlDoc );
		gen.setHeader( XQUERY_HEADER );
		gen.setFooter( XQUERY_FOOTER );
        final String queryString = gen.toString();
        return queryString;
	}

	public CMMLInfo toDataCmml () {
		try {
			cmmlDoc = XMLHelper.XslTransform( cmmlDoc, "de/tuberlin/dima/schubotz/utils/RobertMinerC2s.xsl" );
		} catch ( TransformerException | ParserConfigurationException e ) {
            LOG.warn("Unable to convert to data cmml", e);
		}
		return this;
	}

	public Double getCoverage (Multiset queryTokens) {
		if (queryTokens.isEmpty()){
			return 1.0;
		}
		final Multiset<String> our = getElements();
		if ( our.contains( queryTokens ) ) {
			return 1.0;
		} else {
			final HashMultiset<String> tmp = HashMultiset.create();
			tmp.addAll( queryTokens );
			tmp.removeAll( our );
			return 1 - Double.valueOf(tmp.size())/Double.valueOf(queryTokens.size()) ;
		}
	}

	public DocumentType getDoctype () {
		return cmmlDoc.getDoctype();
	}

	public EntityReference createEntityReference (String s) throws DOMException {
		return cmmlDoc.createEntityReference( s );
	}

	public void normalizeDocument () {
		cmmlDoc.normalizeDocument();
	}

	public Object getUserData (String s) {
		return cmmlDoc.getUserData( s );
	}

	public Node getNextSibling () {
		return cmmlDoc.getNextSibling();
	}

	public CDATASection createCDATASection (String s) throws DOMException {
		return cmmlDoc.createCDATASection( s );
	}

	public Node getPreviousSibling () {
		return cmmlDoc.getPreviousSibling();
	}

	public boolean isSameNode (Node node) {
		return cmmlDoc.isSameNode( node );
	}

	public Attr createAttributeNS (String s, String s2) throws DOMException {
		return cmmlDoc.createAttributeNS( s, s2 );
	}

	public NodeList getChildNodes () {
		return cmmlDoc.getChildNodes();
	}

	public Node getFirstChild () {
		return cmmlDoc.getFirstChild();
	}

	public Object setUserData (String s, Object o, UserDataHandler userDataHandler) {
		return cmmlDoc.setUserData( s, o, userDataHandler );
	}

	public String getNamespaceURI () {
		return cmmlDoc.getNamespaceURI();
	}

	public Node renameNode (Node node, String s, String s2) throws DOMException {
		return cmmlDoc.renameNode( node, s, s2 );
	}

	public Node insertBefore (Node node, Node node2) throws DOMException {
		return cmmlDoc.insertBefore( node, node2 );
	}

	public String getXmlVersion () {
		return cmmlDoc.getXmlVersion();
	}

	public void setXmlVersion (String s) throws DOMException {
		cmmlDoc.setXmlVersion( s );
	}

	public String getDocumentURI () {
		return cmmlDoc.getDocumentURI();
	}

	public void setDocumentURI (String s) {
		cmmlDoc.setDocumentURI( s );
	}

	public String getInputEncoding () {
		return cmmlDoc.getInputEncoding();
	}

	public NodeList getElementsByTagNameNS (String s, String s2) {
		return cmmlDoc.getElementsByTagNameNS( s, s2 );
	}

	public DocumentFragment createDocumentFragment () {
		return cmmlDoc.createDocumentFragment();
	}

	public String getPrefix () {
		return cmmlDoc.getPrefix();
	}

	public void setPrefix (String s) throws DOMException {
		cmmlDoc.setPrefix( s );
	}

	public String getTextContent () throws DOMException {
		return cmmlDoc.getTextContent();
	}

	public void setTextContent (String s) throws DOMException {
		cmmlDoc.setTextContent( s );
	}

	public void normalize () {
		cmmlDoc.normalize();
	}

	public Node removeChild (Node node) throws DOMException {
		return cmmlDoc.removeChild( node );
	}

	public boolean isSupported (String s, String s2) {
		return cmmlDoc.isSupported( s, s2 );
	}

	public ProcessingInstruction createProcessingInstruction (String s, String s2) throws DOMException {
		return cmmlDoc.createProcessingInstruction( s, s2 );
	}

	public short getNodeType () {
		return cmmlDoc.getNodeType();
	}

	public Document getOwnerDocument () {
		return cmmlDoc.getOwnerDocument();
	}

	public Comment createComment (String s) {
		return cmmlDoc.createComment( s );
	}

	public Attr createAttribute (String s) throws DOMException {
		return cmmlDoc.createAttribute( s );
	}

	public boolean getStrictErrorChecking () {
		return cmmlDoc.getStrictErrorChecking();
	}

	public void setStrictErrorChecking (boolean b) {
		cmmlDoc.setStrictErrorChecking( b );
	}

	public NamedNodeMap getAttributes () {
		return cmmlDoc.getAttributes();
	}

	public String getBaseURI () {
		return cmmlDoc.getBaseURI();
	}

	public Element getDocumentElement () {
		return cmmlDoc.getDocumentElement();
	}

	public DOMConfiguration getDomConfig () {
		return cmmlDoc.getDomConfig();
	}

	public DOMImplementation getImplementation () {
		return cmmlDoc.getImplementation();
	}

	public String getNodeValue () throws DOMException {
		return cmmlDoc.getNodeValue();
	}

	public void setNodeValue (String s) throws DOMException {
		cmmlDoc.setNodeValue( s );
	}

	public boolean hasAttributes () {
		return cmmlDoc.hasAttributes();
	}

	public Element createElementNS (String s, String s2) throws DOMException {
		return cmmlDoc.createElementNS( s, s2 );
	}

	public Element createElement (String s) throws DOMException {
		return cmmlDoc.createElement( s );
	}

	public Node importNode (Node node, boolean b) throws DOMException {
		return cmmlDoc.importNode( node, b );
	}

	public Text createTextNode (String s) {
		return cmmlDoc.createTextNode( s );
	}

	public String lookupPrefix (String s) {
		return cmmlDoc.lookupPrefix( s );
	}

	public boolean isEqualNode (Node node) {
		return cmmlDoc.isEqualNode( node );
	}

	public NodeList getElementsByTagName (String s) {
		return cmmlDoc.getElementsByTagName( s );
	}

	public Node getLastChild () {
		return cmmlDoc.getLastChild();
	}

	public Node appendChild (Node node) throws DOMException {
		return cmmlDoc.appendChild( node );
	}

	public short compareDocumentPosition (Node node) throws DOMException {
		return cmmlDoc.compareDocumentPosition( node );
	}

	public Object getFeature (String s, String s2) {
		return cmmlDoc.getFeature( s, s2 );
	}

	public Element getElementById (String s) {
		return cmmlDoc.getElementById( s );
	}

	public boolean isDefaultNamespace (String s) {
		return cmmlDoc.isDefaultNamespace( s );
	}

	public String lookupNamespaceURI (String s) {
		return cmmlDoc.lookupNamespaceURI( s );
	}

	public String getLocalName () {
		return cmmlDoc.getLocalName();
	}

	public String getXmlEncoding () {
		return cmmlDoc.getXmlEncoding();
	}

	public String getNodeName () {
		return cmmlDoc.getNodeName();
	}

	public Node getParentNode () {
		return cmmlDoc.getParentNode();
	}

	public Node cloneNode (boolean b) {
		return cmmlDoc.cloneNode( b );
	}

	public boolean getXmlStandalone () {
		return cmmlDoc.getXmlStandalone();
	}

	public void setXmlStandalone (boolean b) throws DOMException {
		cmmlDoc.setXmlStandalone( b );
	}

	public Node replaceChild (Node node, Node node2) throws DOMException {
		return cmmlDoc.replaceChild( node, node2 );
	}

	public boolean hasChildNodes () {
		return cmmlDoc.hasChildNodes();
	}

	public Node adoptNode (Node node) throws DOMException {
		return cmmlDoc.adoptNode( node );
	}


}
