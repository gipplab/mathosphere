package com.formulasearchengine.mathosphere.pomlp.convertor;

import com.formulasearchengine.mathosphere.pomlp.convertor.extensions.CommandExecutor;
import com.formulasearchengine.mathosphere.pomlp.convertor.extensions.NativeResponse;
import com.formulasearchengine.mathosphere.pomlp.util.Utility;
import com.formulasearchengine.mathosphere.pomlp.xml.XmlDocumentReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * @author Andre Greiner-Petter
 */
public class MathematicalRubyConverter implements Canonicalizable, Parser {

    private static final Logger LOG = LogManager.getLogger( MathematicalRubyConverter.class.getName() );

    private static final String NAME = "Ruby-Mathematical";
    private static final String CMD = "ruby";

    private static final String[] BASIC_ARGS = new String[]{
            CMD,
            Paths.get( "lib", "mathematical.rb" ).toString()
    };

    private LinkedList<String> arguments;

    public MathematicalRubyConverter(){}

    @Override
    public void init(){
        arguments = new LinkedList<>(Arrays.asList(BASIC_ARGS));
    }

    @Override
    public Document parse(String latex) {
        return parseInternal( latex );
    }

    @Override
    public void parseToFile(String latex, Path outputFile) throws IOException {
        Document mmld = parseInternal(latex);
        if ( mmld == null ){
            LOG.warn(NAME + " - Not able to parse: " + latex);
            return;
        }
        LOG.debug("Successfully parsed expression with " + NAME + ". Start file writing.");
        String prettyPrint = Utility.documentToString( mmld, true );
        Files.write( outputFile, prettyPrint.getBytes() );
        LOG.debug("Writing file " + outputFile + " successful.");
    }

    @Override
    public String getNativeCommand() {
        return CMD;
    }

    private Document parseInternal(String latex ) {
        arguments.addLast("$" + latex + "$");
        LOG.debug("Create command executor for ruby.");
        CommandExecutor executor = new CommandExecutor( NAME, arguments );
        NativeResponse response = executor.exec( CommandExecutor.DEFAULT_TIMEOUT );
        if ( handleResponseCode( response, NAME, LOG ) != 0 ){
            arguments.removeLast();
            return null;
        }

        arguments.removeLast();

        // post-processing &alpha; HTML unescape
        String res = response.getResult();
        res = Utility.safeUnescape(res);
        return XmlDocumentReader.getDocumentFromXMLString( res );
    }
}
