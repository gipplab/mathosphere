package com.formulasearchengine.mathosphere.pomlp;

import com.formulasearchengine.mathosphere.pomlp.util.PomlpInternalPaths;
import com.formulasearchengine.mathosphere.pomlp.xml.PomXmlWriter;
import com.formulasearchengine.mathosphere.pomlp.xml.XmlDocumentReader;
import gov.nist.drmf.interpreter.common.GlobalPaths;
import it.unibz.inf.rted.distance.RTED_InfoTree_Opt;
import it.unibz.inf.rted.util.LblTree;
import mlp.ParseException;
import mlp.PomParser;
import mlp.PomTaggedExpression;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This class is a wrapper of the MLP (POM-project) class.
 * It can parse mathematical expressions and creates
 * a tagged parse tree. Furthermore, it uses the PomXmlWriter
 * to parse it to XML trees.
 */
public class MathParser {

    private static final Logger LOG = LogManager.getLogger( MathParser.class.getName() );

    private Path referenceDir;
    private PomParser parser;

    public MathParser(){
        referenceDir = Paths
                .get("")            // local path       -> mathosphere/pomlp
                .toAbsolutePath()   // to absolute path
                .getParent()        // parent directory -> mathosphere/
                .resolve( PomlpInternalPaths
                        .LatexGrammarBaseDir // -> mathosphere/lib/latex-grammar
                        .resolve(
                                GlobalPaths.PATH_REFERENCE_DATA
                        )
                );
        parser = new PomParser(referenceDir);
    }

    public MathParser( Path pomReferencePath ){
        referenceDir = pomReferencePath;
        parser = new PomParser(referenceDir);
    }

    public PomTaggedExpression parseLatexMath( String latex )
            throws ParseException {
        return parser.parse(latex);
    }

    public String parseLatexMathToStringXML( String latex )
            throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        LOG.debug("Parse latex expression by POM-Tagger");
        PomTaggedExpression pte = parseLatexMath(latex);

        LOG.debug("Write XML to output stream");
        PomXmlWriter.writeStraightXML( pte, outputStream );

        LOG.debug("Convert output stream to string and close output stream.");
        String out = outputStream.toString();
        outputStream.close();
        return out;
    }

    /**
     *
     * @param latex
     * @return
     * @throws Exception
     */
    public Document parseLatexMathToDOM( String latex )
            throws Exception {
        LOG.info("Parse latex string to document...");
        // we using a trick and use the PomXmlWriter to directly
        // write the output the DocumentBuilder input stream.

        // Piped in and output streams (connect the output to the input stream)
        LOG.trace("Init linked piped in and output streams");
        PipedInputStream inputStream = new PipedInputStream();
        PipedOutputStream outputStream = new PipedOutputStream(inputStream);

        // create a builder to parse input stream to a document
        LOG.trace("Create document builder factory");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        // First step, parse the mathematical expression
        LOG.debug("Parse latex expression by POM-Tagger");
        PomTaggedExpression pte = parseLatexMath( latex );

        // Run the Document building parser in a separate thread
        // it needs work until the writing process of the PomXmlWriter is finished
        LOG.debug("Create and start parallel thread to listen on piped input stream");
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Callable<Document> callable = () -> builder.parse( inputStream );

        // Run the separate thread and get the future object
        Future<Document> future = executorService.submit( callable );

        // Write to the output stream in XML format
        LOG.debug("Starting writing process to connected piped output stream...");
        PomXmlWriter.writeStraightXML( pte, outputStream );

        // flush and finally close the stream => the second thread finished here
        LOG.trace("Done writing to output stream. Close stream");
        outputStream.flush();
        outputStream.close();

        // get the document from the future object and close the input stream
        LOG.debug("Get result from parallel thread (the document object)");
        Document document = future.get();
        inputStream.close();

        // shutdown all services (if its still running)
        LOG.trace("Shutdown parallel service.");
        executorService.shutdown();

        // return final document
        return document;
    }

    public static void main(String[] args) throws Exception {
        String eq = "P_n^{\\left(\\alpha,\\beta\\right)}\\left(\\cos\\left(a\\Theta\\right)\\right)";

        Path contentP = Paths.get("xml").resolve("latexml-P-content.xml");
        Path presentationP = Paths.get("xml").resolve("latexml-P-presentation.xml");

        LOG.debug("ContentP Path: " + contentP.toAbsolutePath());
        LOG.debug("PresentationP Path: " + presentationP.toAbsolutePath());

        MathParser parser = new MathParser();
        Document pomPdoc = parser.parseLatexMathToDOM(eq);

        Node pomP = pomPdoc.getDocumentElement();

        LOG.info("Read contentP...");
        Node contP = XmlDocumentReader.getNodeFromXML( contentP );
        LOG.info("Read presentationP...");
        Node presP = XmlDocumentReader.getNodeFromXML( presentationP );

        LOG.info("Compute distances!");
        // copy computeMapping by Moritz
        RTED_InfoTree_Opt rted = new RTED_InfoTree_Opt(
                1.0, // insertion costs
                1.0, // deletion costs
                0.0  // renaming costs
        );

        double distCont = rted.nonNormalizedTreeDist( LblTree.fromXML(pomP), LblTree.fromXML(contP) );
        //double distPres = rted.nonNormalizedTreeDist( LblTree.fromXML(pomP), LblTree.fromXML(presP) );

        LinkedList<int[]> ops = rted.computeEditMapping();
        String str = "(";
        for ( int[] arr : ops )
            str += Arrays.toString(arr) + "; ";
        str = str.substring(0, str.length()-"; ".length()) + ")";


        LOG.info("Distance to ContentTree: " + distCont);
//        LOG.info("Distance to PresentationTree: " + distPres);
        LOG.info("Min-Operations: (" + ops.size() + ")" + System.lineSeparator() + str);
    }
}
