package de.tuberlin.dima.schubotz.fse;

import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.util.Collector;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;


public class QueryMapper extends FlatMapFunction<String, Query> implements Serializable {


	private Query currentQuery;


	/**
	 * The core method of the FlatMapFunction. Takes an element from the input data set and transforms
	 * it into zero, one, or more elements.
	 *
	 * @param value The input value.
	 * @param out   The collector for for emitting result values.
	 * @throws Exception This method may throw exceptions. Throwing an exception will cause the operation
	 *                   to fail and may trigger recovery.
	 */
	@Override
	public void flatMap (String value, Collector<Query> out) throws Exception {
		if ( value.trim().length() == 0 ) return;
		if ( (!value.endsWith( "</topics>" )) ) {
			value += "</topics>";
		}
		NodeList nodeList = XMLHelper.String2NodeList( value, "/topics/topic" );

		for ( int i = 0, len = nodeList.getLength(); i < len; i++ ) {
			Node node = nodeList.item( i );
			final XPathExpression xName;
			xName = XMLHelper.compileX( "./num" );
			final Node main = XMLHelper.getElementB( node, xName );
			currentQuery = new Query();
			currentQuery.name = main.getTextContent();

			setFormulae( node );

			setKeywords( node );

			out.collect( currentQuery );
		}
	}

	private void setKeywords (Node node) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		final XPathExpression xKeywords;
		xKeywords = XMLHelper.compileX( "./query/keyword" );
		final NodeList keywords = XMLHelper.getElementsB( node, xKeywords );
		if ( keywords == null ) {
			currentQuery.keywords = null;
		} else {
			currentQuery.keywords = new HashMap<>( 1 );
			for ( int j = 0, len2 = keywords.getLength(); j < len2; j++ ) {
				final String keywordValue = keywords.item( j ).getTextContent().trim();
				final String keywordID = keywords.item( j ).getAttributes().getNamedItem( "id" ).getNodeValue();
				currentQuery.keywords.put( keywordID, keywordValue );
			}
		}
	}

	private void setFormulae (Node node) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, TransformerException {
		final XPathExpression xFormulae;
		final DocumentBuilder builder;
		final DocumentBuilderFactory dbf;

		dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating( true );
		dbf.setNamespaceAware( true );
		dbf.setIgnoringElementContentWhitespace( true );
		builder = dbf.newDocumentBuilder();
		xFormulae = XMLHelper.compileX( "./query/formula" );
		final NodeList formulae = XMLHelper.getElementsB( node, xFormulae );
		if ( formulae == null ) {
			currentQuery.formulae = null;
		} else {
			currentQuery.formulae = new HashMap<>( 1 );
			for ( int j = 0, len2 = formulae.getLength(); j < len2; j++ ) {
				Document mathML = builder.newDocument();
				Node mathMLElement = XMLHelper.getElementB( formulae.item( j ), "./math" );
				Node importedNode = mathML.importNode( mathMLElement, true );
				XPathFactory xpathFactory = XPathFactory.newInstance();
				// XPath to find empty text nodes.
				XPathExpression xpathExp = xpathFactory.newXPath().compile(
					"//text()[normalize-space(.) = '']");
				NodeList emptyTextNodes = (NodeList)
					xpathExp.evaluate(mathML, XPathConstants.NODESET);
				// Remove each empty text node from document.
				for (int i = 0; i < emptyTextNodes.getLength(); i++) {
					Node emptyTextNode = emptyTextNodes.item(i);
					emptyTextNode.getParentNode().removeChild(emptyTextNode);
				}
				mathML.appendChild( importedNode );
				final String formulaID = formulae.item( j ).getAttributes().getNamedItem( "id" ).getNodeValue();
				currentQuery.formulae.put( formulaID, XMLHelper.printDocument( mathML ) );
			}
		}
	}
}
