package com.formulasearchengine.mathosphere.pomlp.xml;

import com.formulasearchengine.mathmlquerygenerator.XQueryGenerator;
import com.formulasearchengine.mathmltools.mml.CMMLInfo;
import com.formulasearchengine.mathmltools.xmlhelper.XMLHelper;
import com.formulasearchengine.mathosphere.pomlp.util.Utility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.nio.file.Path;

import static com.formulasearchengine.mathmltools.xmlhelper.XmlDocumentReader.getDocumentFromXML;
import static com.formulasearchengine.mathmltools.xmlhelper.XmlDocumentReader.getDocumentFromXMLString;

public class MathMLDocumentReader {

    private static final Logger LOG = LogManager.getLogger( MathMLDocumentReader.class.getName() );

    private Document mmlDoc;
    private Node presentationNode, contentNode;

    private String filename;

    public MathMLDocumentReader( Path mmlFile ){
        init(mmlFile);
    }

    public MathMLDocumentReader( String mmlString ){
        init( mmlString );
    }

    private void init( String mmlString ){
        filename = "-StringInput-";
        mmlDoc = getDocumentFromXMLString( mmlString );
        init();
    }

    private void init( Path mmlFile ){
        filename = mmlFile.getFileName().toString();
        mmlDoc = getDocumentFromXML(mmlFile);
        init();
    }

    private void init(){
        // first copy..
        Document copy = (Document) mmlDoc.cloneNode(true);

        try {
            contentNode = XQueryGenerator.getMainElement( copy );
            if ( contentNode.getNodeName().equals("math") ){
                presentationNode = contentNode;
                contentNode = null;
            }
        } catch ( Exception e ){
            LOG.debug("No content node found in " + filename);
        }

        // presentation mml node
        Node parentNode;
        if ( contentNode != null ){
            parentNode = contentNode.getParentNode();
            parentNode.removeChild( contentNode );
        }

        NodeList l = copy.getElementsByTagName( "annotation" );
        for ( int i = 0; i < l.getLength(); i++ )
            l.item(i).getParentNode().removeChild( l.item(i) );
        presentationNode = copy.getDocumentElement();
    }

    public void canonicalize(){
        this.mmlDoc = Utility.getCanonicalizedDocument(mmlDoc);
        this.init();
    }

    public Node getContentNode(){
        return contentNode;
    }

    public Node getPresentationNode(){
        return presentationNode;
    }

    public Document getDocument(){
        return mmlDoc;
    }

    public static String debugToString( Node node ){
        if ( node == null ) return "NULL";
        StringWriter sw = new StringWriter();
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(node), new StreamResult(sw));
        } catch (TransformerException te) {
            System.out.println("nodeToString Transformer Exception");
        }
        return sw.toString();
    }
}
