package de.tuberlin.dima.schubotz.fse;

import eu.stratosphere.types.Value;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by Moritz on 20.06.2014.
 */
public class DomSerial  //extends com.sun.org.apache.xerces.internal.dom.DocumentImpl
 implements Value {
    public Document document;

    /**
     * Writes the object's internal data to the given data output stream.
     *
     * @param out the output stream to receive the data.
     * @throws java.io.IOException thrown if any error occurs while writing to the output stream
     */
    @Override
    public void write(DataOutput out) throws IOException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            StreamResult jout = new StreamResult((java.io.OutputStream) out);
            transformer.transform(new DOMSource(document), jout);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads the object's internal data from the given data input stream.
     *
     * @param in the input stream to read the data from
     * @throws java.io.IOException thrown if any error occurs while reading from the input stream
     */
    @Override
    public void read(DataInput in) throws IOException {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory
                .newInstance();
        domFactory.setNamespaceAware(true);
        //domFactory.setValidating(true);
        domFactory.setAttribute(
                "http://apache.org/xml/features/dom/include-ignorable-whitespace",
                Boolean.FALSE);
        domFactory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder = null;
        try {
            builder = domFactory.newDocumentBuilder();
           document =  builder.parse((java.io.InputStream) in);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

    }
}
