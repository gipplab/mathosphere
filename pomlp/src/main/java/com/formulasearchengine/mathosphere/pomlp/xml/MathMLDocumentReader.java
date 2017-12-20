package com.formulasearchengine.mathosphere.pomlp.xml;

import com.formulasearchengine.mathmlquerygenerator.XQueryGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.nio.file.Path;

public class MathMLDocumentReader extends XmlDocumentReader {

    private static final Logger LOG = LogManager.getLogger( MathMLDocumentReader.class.getName() );

    /* TODO
        XML getContentMML();
        XML getPresentationMML();
        String getBlaBla
     */

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
        // content mml node
        contentNode = XQueryGenerator.getMainElement( mmlDoc );

        // presentation mml node
        Node parentNode = contentNode.getParentNode();
        parentNode.removeChild( contentNode );

        // annotation tags?
        NodeList childs = parentNode.getChildNodes();
        if ( childs != null ){
            Node n;
            for ( int i = 0; i < childs.getLength(); i++ ){
                n = childs.item(i);
                if ( n.getNodeName().equals("annotation") )
                    parentNode.removeChild(n);
            }
        }

        presentationNode = parentNode;
    }

    private static Document createNewDocumentSubtree( Node subtree ) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        factory.setExpandEntityReferences(true);
        try {
            Document copy = factory.newDocumentBuilder().newDocument();
            Node parent = copy.importNode( subtree, true );
            copy.appendChild(parent);
            return copy;
        } catch ( ParserConfigurationException e ){
            LOG.error("Something went wrong when copying subtree from MML.", e);
            return null;
        }
    }

    /**
     * Returns a copy of the presentation MML subtree
     * @return
     */
    public Document getPresentationSubtree(){
        return createNewDocumentSubtree(presentationNode);
    }

    /**
     * Returns a copy of the content MML subtree
     * @return
     */
    public Document getContentSubtree(){
        return createNewDocumentSubtree(contentNode);
    }

    public Node getContentNode(){
        return contentNode;
    }

    public Node getPresentationNode(){
        return presentationNode;
    }

    public static String debugToString( Node node ){
        StringWriter sw = new StringWriter();
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            //t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(node), new StreamResult(sw));
        } catch (TransformerException te) {
            System.out.println("nodeToString Transformer Exception");
        }
        return sw.toString();
    }
}
