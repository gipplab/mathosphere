package com.formulasearchengine.mathosphere.pomlp.convertor;

import com.formulasearchengine.mathosphere.pomlp.util.Constants;
import com.formulasearchengine.mathosphere.pomlp.util.Utility;
import com.formulasearchengine.mathosphere.pomlp.xml.MathMLDocumentReader;
import com.formulasearchengine.mathosphere.pomlp.xml.XmlDocumentReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import uk.ac.ed.ph.snuggletex.*;
import uk.ac.ed.ph.snuggletex.upconversion.UpConvertingPostProcessor;
import uk.ac.ed.ph.snuggletex.upconversion.internal.UpConversionPackageDefinitions;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class SnuggleTexConverter implements Parser, Canonicalizable {

    private static final Logger LOG = LogManager.getLogger( SnuggleTexConverter.class.getName() );

    // do not multi-thread this object!
    private SnuggleSession session;

    private DOMOutputOptions options;

    private DocumentBuilder builder;

    public SnuggleTexConverter(){}

    @Override
    public void init(){
        LOG.debug("Instantiate Snuggle Session!");
        SessionConfiguration config = new SessionConfiguration();
        config.setExpansionLimit(-1); // deactivate safeguard -> we have monster heap!
        config.setFailingFast(false); // leave me here, keep going without me!

        SnuggleEngine snuggleEngine = new SnuggleEngine();
        snuggleEngine.addPackage(UpConversionPackageDefinitions.getPackage());

        session = snuggleEngine.createSession();

        UpConvertingPostProcessor upProcessor = new UpConvertingPostProcessor();
        options = new DOMOutputOptions();
        options.setAddingMathSourceAnnotations(true);
        options.addDOMPostProcessors(upProcessor);

        DocumentBuilderFactory factory = XmlDocumentReader.FACTORY;
        try {
            builder = factory.newDocumentBuilder();
        } catch ( ParserConfigurationException pe ){
            LOG.error("Cannot create document builder because of invalid configurations. " +
                    "Builder will be null!", pe);
        }
    }

    /**
     *
     * @param latex
     * @throws IOException
     */
    @Override
    public synchronized Document parse( String latex ) throws Exception {
        parseCurrentSession(latex);
        Document doc = createDocument();
        session.reset();
        LOG.debug("Done. Return snuggletexs parsed document.");
        return doc;
    }

    @Override
    public synchronized void parseToFile(String latex, Path outputFile) throws Exception {
        parseCurrentSession(latex);
        Document doc = createDocument();
        String prettyPrint = Utility.documentToString(doc, true);
        if ( !Files.exists(outputFile) ) Files.createFile(outputFile);
        Files.write( outputFile, prettyPrint.getBytes() );
        session.reset();
    }

    private static final String MATH_ENV_SOURROUNDER = "$";

    private void parseCurrentSession( String latex ) throws IOException {
        LOG.info("Parse latex with snuggle: " + latex);
        SnuggleInput input = new SnuggleInput(
                MATH_ENV_SOURROUNDER + latex + MATH_ENV_SOURROUNDER
        );
        boolean success = session.parseInput(input);
        if ( !success ){
            LOG.warn("Snuggle couldn't parse latex...");
            handleErrors( session.getErrors() );
        }

        LOG.debug("Snuggle parsed successfully. Start document export process...");
    }

    private Document createDocument() throws Exception {
        try {
            Document doc = builder.newDocument();

//            NodeList nodeList = session.buildDOMSubtree(options);
            NodeList nodeList = session.buildDOMSubtree();

            doc.appendChild(doc.adoptNode(nodeList.item(0).cloneNode(true)));
            return doc;
        } catch ( SnuggleLogicException e ){
            throw new Exception( e );
        }
    }

    private void handleErrors( List<InputError> errors ){
        String errorMsg = "";
        for ( InputError err : errors ){
            errorMsg += err.toString() + Constants.NL;
        }
        LOG.error("Error occurred while parsing latex: " + errorMsg);
    }

    public static void main(String[] args) throws Exception {
        SnuggleTexConverter c = new SnuggleTexConverter();
        c.init();
        Document d = c.parse("a");
        //d = Utility.getCanonicalizedDocument(d);
        String s = Utility.documentToString(d, true);
        System.out.println( s );

        s = s.replaceAll("\"","\\\"");
        MathMLDocumentReader r1 = new MathMLDocumentReader( s );
        System.out.println( Utility.documentToString(r1.getDocument(),true) );

        /*
        MathMLDocumentReader r = new MathMLDocumentReader(Paths.get("../lib/GoUldI/data/snuggletex/1.mml") );
        System.out.println("NEXT1");
        System.out.println( Utility.documentToString(r.getDocument(),true));
        r.canonicalize();
        System.out.println("NEXT2");
        System.out.println( Utility.documentToString(r.getDocument(),true));
        */
    }
}
