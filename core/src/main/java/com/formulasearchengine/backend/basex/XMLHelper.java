package com.formulasearchengine.backend.basex;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.StringReader;

public final class XMLHelper {
	/**
	 * Helper program: Transforms a String to a XML Document.
	 *
	 * @param InputXMLString the input xml string
	 * @return parsed document
	 * @throws javax.xml.parsers.ParserConfigurationException the parser configuration exception
	 * @throws java.io.IOException                            Signals that an I/O exception has occurred.
	 */
	public static Document String2Doc( String InputXMLString ) {
		try {
			DocumentBuilder builder = getDocumentBuilder();
			InputSource is = new InputSource( new StringReader( InputXMLString ) );
			is.setEncoding( "UTF-8" );
			return builder.parse( is );
		} catch ( Exception e ) {
			System.out.println( "cannot parse following content\\n\\n" + InputXMLString );
			e.printStackTrace();
			return null;
		}
	}

	private static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory domFactory = DocumentBuilderFactory
			.newInstance();
		domFactory.setNamespaceAware( true );
		// Unfortunately we can not ignore whitespaces without a schema.
		// So we use the NdLst workaround for now.
		domFactory.setAttribute(
			"http://apache.org/xml/features/dom/include-ignorable-whitespace",
			Boolean.FALSE );
		return domFactory.newDocumentBuilder();
	}
}
