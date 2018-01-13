package com.formulasearchengine.mathosphere.pomlp.convertor;

import com.formulasearchengine.mathmltools.xmlhelper.XmlDocumentReader;
import com.formulasearchengine.mathosphere.pomlp.xml.MathMLDocumentReader;
import com.formulasearchengine.nativetools.CommandExecutor;
import com.formulasearchengine.nativetools.NativeResponse;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.formulasearchengine.mathosphere.pomlp.util.config.LatexMLConfig.*;

public class LatexmlGenerator implements Parser, Canonicalizable{

    private static Logger LOG = LogManager.getLogger(LatexmlGenerator.class.getName());

    private static final String NAME = "LaTeXML";

    private NativeResponse response;

    public LatexmlGenerator(){}

    @Override
    public void init() {}

    @Override
    public Document parse(String latex) {
        LOG.info("Start parsing process of installed latexml version! " + latex);
        CommandExecutor executor = new CommandExecutor( NAME, buildArguments(latex));
        response = executor.exec( CommandExecutor.DEFAULT_TIMEOUT, Level.TRACE );
        if ( handleResponseCode(response, NAME, LOG) != 0 ) return null;
        LOG.info(NAME + " conversion successful.");
        return XmlDocumentReader.getDocumentFromXMLString( response.getResult() );
    }

    @Override
    public void parseToFile(String latex, Path outputFile) {
        LOG.info("Call native latexmlc for " + latex);
        CommandExecutor executor = new CommandExecutor(NAME, buildArguments(latex, outputFile));
        response = executor.exec( CommandExecutor.DEFAULT_TIMEOUT, Level.TRACE );
        if ( handleResponseCode(response, NAME, LOG) == 0 ) {
            LOG.info("Successfully write parsed expression to " + outputFile);
        }
    }

    @Override
    public String getNativeCommand(){
        return NATIVE_COMMAND;
    }

    public ArrayList<String> buildArguments(String latex ){
        ArrayList<String> args = asList( GENERIC_CONFIG );
        args.add( "literal:" + latex );
        return args;
    }

    public List<String> buildArguments(String latex, Path outputFile ){
        ArrayList<String> args = asList( GENERIC_CONFIG );
        args.add( "--dest=" + outputFile.toAbsolutePath().toString() );
        args.add( "literal:" + latex );
        return args;
    }
}
