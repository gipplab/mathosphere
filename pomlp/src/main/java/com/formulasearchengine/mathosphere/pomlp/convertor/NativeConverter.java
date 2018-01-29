package com.formulasearchengine.mathosphere.pomlp.convertor;

import com.formulasearchengine.mathmltools.xmlhelper.XmlDocumentReader;
import com.formulasearchengine.mathosphere.pomlp.util.Utility;
import com.formulasearchengine.nativetools.CommandExecutor;
import com.formulasearchengine.nativetools.NativeResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;

/**
 * @author Andre Greiner-Petter
 */
public abstract class NativeConverter implements Parser {

    private static final Logger LOG = LogManager.getLogger( NativeConverter.class.getName() );

    private String name;
    private LinkedList<String> arguments;

    protected void internalInit(LinkedList<String> arguments, String name ){
        this.name = name;
        this.arguments = arguments;
    }

    @Override
    public Document parse(String latex) {
        return parseInternal(
                arguments,
                latex,
                name
        );
    }

    @Override
    public void parseToFile(String latex, Path outputFile) throws IOException {
        Document mmld = parseInternal(
                arguments,
                latex,
                name
        );
        if ( mmld == null ){
            LOG.warn(name + " - Not able to parse: " + latex);
            return;
        }
        LOG.debug("Successfully parsed expression with " + name + ". Start file writing.");
        String prettyPrint = Utility.documentToString( mmld, true );
        Files.write( outputFile, prettyPrint.getBytes() );
        LOG.debug("Writing file " + outputFile + " successful.");
    }

    protected String parseInternalToString( LinkedList<String> args, String latex, String name ){
        args.addLast( latex );
        LOG.debug("Create command executor for " + name + ".");
        CommandExecutor executor = new CommandExecutor( name, args );
        NativeResponse response = executor.exec( CommandExecutor.DEFAULT_TIMEOUT );
        if ( handleResponseCode( response, name, LOG ) != 0 ){
            args.removeLast();
            return null;
        }

        args.removeLast();

        // post-processing &alpha; HTML unescape
        String res = response.getResult();
        return Utility.safeUnescape(res);
    }

    protected Document parseInternal(LinkedList<String> args, String latex, String name) {
        return XmlDocumentReader.getDocumentFromXMLString( parseInternalToString(args, latex, name) );
    }
}
