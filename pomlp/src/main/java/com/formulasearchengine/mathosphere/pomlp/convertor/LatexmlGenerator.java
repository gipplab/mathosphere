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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.formulasearchengine.mathosphere.pomlp.util.config.LatexMLConfig.*;

public class LatexmlGenerator implements Parser, Canonicalizable{

    private static Logger LOG = LogManager.getLogger(LatexmlGenerator.class.getName());

    private static final String NAME = "LaTeXML";

    private NativeResponse response;
    private Path redirect;

    public LatexmlGenerator(){}

    @Override
    public void init() {
        redirect = null;
    }

    public void redirectLatex( Path path ){
        redirect = path;
    }

    public String parseToString( List<String> arguments, String latex ){
        latex = preLatexmlFixes( latex );
        LOG.info("Start parsing process of installed latexml version! " + latex);
        CommandExecutor executor = new CommandExecutor( NAME, arguments);
        if ( redirect != null ) executor.setWorkingDirectoryForProcess( redirect );
        response = executor.exec( CommandExecutor.DEFAULT_TIMEOUT, Level.TRACE );
        if ( handleResponseCode(response, NAME, LOG) != 0 ) return null;
        LOG.info(NAME + " conversion successful.");
        return response.getResult();
    }

    public String parseToString( String latex ){
        return parseToString( buildArguments(latex), latex );
    }

    @Override
    public Document parse(String latex) {
        latex = preLatexmlFixes( latex );
        String result = parseToString( latex );
        if ( result != null ) return XmlDocumentReader.getDocumentFromXMLString( result );
        else return null;
    }

    @Override
    public void parseToFile(String latex, Path outputFile) {
        latex = preLatexmlFixes( latex );
        LOG.info("Call native latexmlc for " + latex);
        CommandExecutor executor = new CommandExecutor(NAME, buildArguments(latex, outputFile));
        if ( redirect != null ) executor.setWorkingDirectoryForProcess( redirect );
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

    private static Pattern LTXML_PATTERN = Pattern.compile("^\\\\math.+");

    public static String preLatexmlFixes( String rawTex ){
        Matcher matcher = LTXML_PATTERN.matcher(rawTex);
        if ( matcher.find() ){
            rawTex = "{" + rawTex + "}";
        }
        return rawTex;
    }
}
