package com.formulasearchengine.mathosphere.pomlp.convertor;

import com.formulasearchengine.mathmlconverters.latexml.LaTeXMLConverter;
import com.formulasearchengine.mathosphere.pomlp.xml.MathMLDocumentReader;
import com.formulasearchengine.nativetools.CommandExecutor;
import com.formulasearchengine.nativetools.NativeResponse;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LatexmlGenerator implements Parser, Canonicalizable{

    private static Logger LOG = LogManager.getLogger(LatexmlGenerator.class.getName());

    private static final String NAME = "LaTeXML";
    private static final String COMMAND = "latexmlc";

    private static final String[] DEFAULT_CMD = new String[]{
            COMMAND,
            "--includestyles",
            "--format=xhtml",
            "--whatsin=math",
            "--whatsout=math",
            "--pmml",
            "--cmml",
            "--nodefaultresources",
            "--linelength=90",
            "--preload", "LaTeX.pool",
            "--preload", "article.cls",
            "--preload", "amsmath.sty",
            "--preload", "amsthm.sty",
            "--preload", "amstext.sty",
            "--preload", "amssymb.sty",
            "--preload", "eucal.sty",
            "--preload", "[dvipsnames]xcolor.sty",
            "--preload", "url.sty",
            "--preload", "hyperref.sty",
            "--preload", "[ids]latexml.sty",
            "--preload", "texvc"
    };

    private LaTeXMLConverter converter;
    private NativeResponse response;

    public LatexmlGenerator(){}

    @Override
    public void init() {
        // we only need latexmlc -> no configuration needed
        converter = new LaTeXMLConverter(null);
    }

    @Override
    public Document parse(String latex) {
        LOG.info("Start parsing process of installed latexml version! " + latex);
        CommandExecutor executor = new CommandExecutor( NAME, buildArguments(latex));
        response = executor.exec( CommandExecutor.DEFAULT_TIMEOUT, Level.TRACE );
        if ( handleResponseCode(response, NAME, LOG) != 0 ) return null;
        LOG.info(NAME + " conversion successful.");
        return MathMLDocumentReader.getDocumentFromXMLString( response.getResult() );
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
        return COMMAND;
    }

    public ArrayList<String> buildArguments(String latex ){
        ArrayList<String> args = new ArrayList<>(Arrays.asList(DEFAULT_CMD));
        args.add( "literal:" + latex );
        return args;
    }

    public List<String> buildArguments(String latex, Path outputFile ){
        ArrayList<String> args = new ArrayList<>(Arrays.asList(DEFAULT_CMD));
        args.add( "--dest=" + outputFile.toAbsolutePath().toString() );
        args.add( "literal:" + latex );
        return args;
    }
}
