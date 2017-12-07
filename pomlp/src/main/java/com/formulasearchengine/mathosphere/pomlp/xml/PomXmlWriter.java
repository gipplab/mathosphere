package com.formulasearchengine.mathosphere.pomlp.xml;

import com.formulasearchengine.mathosphere.pomlp.util.PomlpConstants;
import com.formulasearchengine.mathosphere.pomlp.util.PomlpInternalPaths;
import gov.nist.drmf.interpreter.common.GlobalPaths;
import mlp.MathTerm;
import mlp.ParseException;
import mlp.PomParser;
import mlp.PomTaggedExpression;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import static com.formulasearchengine.mathosphere.pomlp.util.PomXmlConstants.*;

/**
 * A class to parse
 */
public class PomXmlWriter {

    private static final Logger LOG = LogManager.getLogger( PomXmlWriter.class.getName() );

    public static final String GENERIC_FILE_NAME = "pom-export.xml";

    private static boolean overwrite = true;

    private Path outputFile;

    public PomXmlWriter() throws IOException {
        outputFile = PomlpInternalPaths.ResourceXMLExportsDir.resolve( GENERIC_FILE_NAME );
        initOverwriteCheck();
    }

    public PomXmlWriter( Path outputFilePath ) throws IOException {
        outputFile = outputFilePath;
        initOverwriteCheck();
    }

    private void initOverwriteCheck() throws IOException {
        if ( Files.exists(outputFile) && !overwrite ){
            String[] tmp = GENERIC_FILE_NAME.split("\\.");
            String newFileName = tmp[0] + LocalDateTime.now().format(PomlpConstants.DATE_FORMAT);
            outputFile = PomlpInternalPaths.ResourceXMLExportsDir.resolve(
                    newFileName + "." + tmp[1]
            );
            LOG.info("Overwrite mode is off -> create different File: " + outputFile.getFileName());
        } else if ( !Files.exists(outputFile) ){
            Files.createFile( outputFile.toAbsolutePath() );
            LOG.info("Create file: " + outputFile.toAbsolutePath());
        } else {
            LOG.info("Overwrite mode is on -> overwrite existing file " + outputFile.getFileName());
        }
    }

    public void writeStraightXML( PomTaggedExpression root )
            throws FileNotFoundException, XMLStreamException
    {
        writeStraightXML( root, new FileOutputStream( outputFile.toString() ) );
    }

    public static void writeStraightXML( PomTaggedExpression root, OutputStream outputStream )
            throws XMLStreamException
    {
        LOG.debug("Start XML writing process...");
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = factory.createXMLStreamWriter(outputStream);

        LOG.trace("Start document writing..");
        // start
        writer.writeStartDocument();
        writer.writeCharacters( NL );
        writer.writeStartElement("math");
        writer.writeAttribute("type", "pom-exported");
        writer.writeCharacters( NL );

        LOG.trace("Start recursive writing..");
        recursiveInnerXML( writer, root, 1 );
        // write inner elements

        LOG.trace("Close document.");
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();
        LOG.info("XML writing successful!");
    }

    private static void recursiveInnerXML( XMLStreamWriter writer, PomTaggedExpression root, int tabLevel )
            throws XMLStreamException
    {
        writer.writeCharacters( generateTab(tabLevel) );
        List<PomTaggedExpression> comps = root.getComponents();
        MathTerm mathTerm = root.getRoot();
        String pTag = root.getTag();
        List<String> secTags = root.getSecondaryTags();
        boolean termMode = false;

        if ( mathTerm != null && !mathTerm.isEmpty() ){
            pTag = mathTerm.getTag();
            secTags = mathTerm.getSecondaryTags();
            termMode = true;
        }

        if ( comps.isEmpty() ){ // recursive anchor
            if ( termMode ) writer.writeStartElement( TERM_NODE );
            else writer.writeEmptyElement( EXPR_NODE );

            writer.writeAttribute( ATTR_PRIME_TAG, pTag );
            if ( !secTags.isEmpty() )
                writer.writeAttribute( ATTR_SEC_TAG, createListString(secTags) );

            if ( termMode ){
                writer.writeCharacters( mathTerm.getTermText() );
                writer.writeEndElement();
            }

            writer.writeCharacters( NL );
            return;
        }

        writer.writeStartElement( EXPR_NODE );
        writer.writeAttribute( ATTR_PRIME_TAG, pTag );
        if ( !secTags.isEmpty() )
            writer.writeAttribute( ATTR_SEC_TAG, createListString(secTags) );
        writer.writeCharacters( NL );

        for ( PomTaggedExpression exp : comps ){
            recursiveInnerXML( writer, exp, tabLevel+1 );
        }

        writer.writeCharacters( generateTab(tabLevel) );
        writer.writeEndElement();
        writer.writeCharacters( NL );
    }

    public static String createListString( List<String> list ){
        if ( list.isEmpty() ) return "";
        String out = "";
        for ( String s : list )
            out += s + ", ";
        return out.substring(0, out.length()-2);
    }

    public static String generateTab( int level ){
        String str = "";
        for ( int i = 1; i <= level; i++ )
            str += TAB;
        return str;
    }

    public static void switchOverwriteModeOn(){
        overwrite = true;
    }

    public static void switchOverwriteModeOff(){
        overwrite = false;
    }

    public static void main(String[] args) throws IOException {
        String eq = "P_n^{\\left(\\alpha,\\beta\\right)}\\left(\\cos\\left(a\\Theta\\right)\\right)";
        //eq = "P_n^{(\\alpha,\\beta)}(\\cos(a\\Theta))";

        PomXmlWriter writer = new PomXmlWriter(Paths.get("xml/pom-genericP.xml"));

        Path refPath = Paths
                .get("")
                .toAbsolutePath()
                .getParent()
                .resolve( PomlpInternalPaths
                        .LatexGrammarBaseDir
                        .resolve(GlobalPaths.PATH_REFERENCE_DATA)
                );

        LOG.debug("Path to POM references: " + refPath.toAbsolutePath());

        PomParser parser = new PomParser( refPath.toAbsolutePath().toString() );
        try {
            PomTaggedExpression pte = parser.parse(eq);
            writer.writeStraightXML( pte );
            LOG.info("Done, successfully created XML document.");
        } catch ( ParseException | FileNotFoundException | XMLStreamException e ){
            LOG.error("Cannot parse a+b or cannot write XML!", e);
        }
    }
}
