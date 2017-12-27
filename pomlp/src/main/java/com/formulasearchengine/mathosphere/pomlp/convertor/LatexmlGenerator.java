package com.formulasearchengine.mathosphere.pomlp.convertor;

import com.formulasearchengine.mathmlconverters.latexml.LaTeXMLConverter;
import com.formulasearchengine.mathmlconverters.latexml.LaTeXMLServiceResponse;
import com.formulasearchengine.mathosphere.pomlp.convertor.extensions.LatexMLCommandExecutor;
import com.formulasearchengine.mathosphere.pomlp.util.Utility;
import com.formulasearchengine.mathosphere.pomlp.xml.MathMLDocumentReader;
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

    private static final String[] DEFAULT_CMD = new String[]{"latexmlc",
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
    private LaTeXMLServiceResponse response;

    public LatexmlGenerator(){}

    @Override
    public void init() {
        // we only need latexmlc -> no configuration needed
        converter = new LaTeXMLConverter(null);
    }

    @Override
    public Document parse(String latex) {
        LOG.info("Start parsing process of installed latexml version! " + latex);
        LatexMLCommandExecutor executor = new LatexMLCommandExecutor(buildArguments(latex));
        response = executor.exec( LatexMLCommandExecutor.DEFAULT_TIMEOUT, Level.TRACE );
        if ( response.getStatusCode() != 0 ){
            LOG.warn("Error in latexml conversion. " + response.getStatus() + System.lineSeparator() +
                    response.getLog()
            );
            return null;
        }
        LOG.info("Conversion successful.");
        return MathMLDocumentReader.getDocumentFromXMLString( response.getResult() );
    }

    @Override
    public void parseToFile(String latex, Path outputFile) {
        LOG.info("Call native latexmlc for " + latex);
        LatexMLCommandExecutor executor = new LatexMLCommandExecutor(buildArguments(latex, outputFile));
        response = executor.exec( LatexMLCommandExecutor.DEFAULT_TIMEOUT, Level.TRACE );
        LOG.info("Response: " + response.getResult());
        if ( response.getStatusCode() != 0 ){
            LOG.error("Cannot write parse expression via latexmlc.");
        } else {
            LOG.info("Successfully write parsed expression to " + outputFile);
        }
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
