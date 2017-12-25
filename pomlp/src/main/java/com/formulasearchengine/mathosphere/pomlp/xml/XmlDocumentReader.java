package com.formulasearchengine.mathosphere.pomlp.xml;

import com.formulasearchengine.mathosphere.pomlp.util.Utility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Helper class to format XML files to Document and Node types
 */
public class XmlDocumentReader {
    private static final Logger LOG = LogManager.getLogger( XmlDocumentReader.class.getName() );

    public static DocumentBuilderFactory FACTORY = DocumentBuilderFactory.newInstance();

    static {
        FACTORY.setIgnoringComments(true);
        //FACTORY.setIgnoringElementContentWhitespace(true);
        //FACTORY.setValidating(true);
        FACTORY.setExpandEntityReferences(true);
    }

    public static Document getDocumentFromXML( Path xmlF ){
        try {
            LOG.debug("Start reading process from XML file.");
            DocumentBuilder builder = FACTORY.newDocumentBuilder();
            InputStream inputStream = Files.newInputStream( xmlF.toAbsolutePath() );
            Document doc = builder.parse( inputStream );
            LOG.debug("Successfully read from XML file.");
            return doc;
        } catch ( ParserConfigurationException pce ){
            // how could this happen, without any configurations? ---
            LOG.error("Cannot create DocumentBuilder...", pce);
        } catch (SAXException e) {
            LOG.error("Cannot parse XML file: " + xmlF.toString(), e);
        } catch (IOException e) {
            LOG.error("Cannot read file: " + xmlF.toString(), e);
        }
        return null;
    }

    public static Document getDocumentFromXMLString( String xml ){
        try {
            LOG.debug("Start reading process from XML file.");
            DocumentBuilder builder = FACTORY.newDocumentBuilder();
            InputSource input = new InputSource( new StringReader(xml));
            Document doc = builder.parse( input );
            LOG.debug("Successfully read from XML file.");
            return doc;
        } catch ( ParserConfigurationException pce ){
            // how could this happen, without any configurations? ---
            LOG.error("Cannot create DocumentBuilder...", pce);
        } catch (SAXException e) {
            LOG.error("Cannot parse XML file: " + xml, e);
        } catch (IOException e) {
            LOG.error("Cannot read file: " + xml.toString(), e);
        }
        return null;
    }

    public static Node getNodeFromXML( Path xmlF ){
        Document document = getDocumentFromXML( xmlF );
        return document.getDocumentElement();
    }

    public static void main(String[] args){
        String s = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\">\n" +
                "   <mi>a</mi>\n" +
                "</math>";
        MathMLDocumentReader r = new MathMLDocumentReader(s);
        System.out.println(Utility.documentToString(r.getDocument(), true));
    }
}
