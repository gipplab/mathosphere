package com.formulasearchengine.mathosphere.pomlp.xml;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Helper class to format XML files to Document and Node types
 */
public class XmlDocumentReader {
    private static final Logger LOG = LogManager.getLogger( XmlDocumentReader.class.getName() );

    public static Document getDocumentFromXML( Path xmlF ){
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            LOG.debug("Start reading process from XML file.");
            DocumentBuilder builder = factory.newDocumentBuilder();
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

    public static Node getNodeFromXML( Path xmlF ){
        Document document = getDocumentFromXML( xmlF );
        return document.getDocumentElement();
    }
}
