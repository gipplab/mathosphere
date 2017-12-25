package com.formulasearchengine.mathosphere.pomlp.convertor;

import com.formulasearchengine.mathmlconverters.latexml.LaTeXMLConverter;
import com.formulasearchengine.mathmlconverters.latexml.LaTeXMLServiceResponse;
import com.formulasearchengine.mathmlconverters.util.CommandExecutor;
import com.formulasearchengine.mathosphere.pomlp.xml.MathMLDocumentReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import java.nio.file.Path;

public class LatexmlGenerator implements Parser, Canonicalizable{

    private static Logger LOG = LogManager.getLogger(LatexmlGenerator.class.getName());

    private LaTeXMLConverter converter;
    private LaTeXMLServiceResponse response;

    public LatexmlGenerator(){}

    @Override
    public void init() {
        // we only need latexmlc -> no configuration needed
        converter = new LaTeXMLConverter(null);
    }

    @Override
    public Document parse(String latex) throws Exception {
        LOG.info("Start parsing process of installed latexml version! " + latex);
        response = converter.runLatexmlc(latex);
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
    public void parseToFile(String latex, Path outputFile) throws Exception {
        LOG.info("Call native latexmlc for " + latex);
        CommandExecutor executor = new CommandExecutor(buildArguments(latex, outputFile));
        response = new LaTeXMLServiceResponse(executor.exec(0),"Convert via latexmlc...");
        if ( response.getStatusCode() != 0 ){
            LOG.error("Cannot write parse expression via latexmlc.");
        } else {
            LOG.info("Successfully write parsed expression to " + outputFile);
        }
    }

    private static String[] buildArguments( String latex, Path outputFile ){
        return new String[]{"latexmlc",
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
                "--preload", "texvc",
                "--dest="+outputFile.toString(),
                "literal:"+latex
        };
    }
}
