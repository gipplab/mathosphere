package com.formulasearchengine.mathosphere.pomlp.pom;

import com.formulasearchengine.mathosphere.pomlp.util.PomlpPathConstants;
import mlp.PomParser;
import mlp.PomTaggedExpression;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class PomInitTest {

    public static final Logger LOG = LogManager.getLogger( PomInitTest.class.getName() );

    public static final String SIMPLE_LATEX = "a+b";

    @BeforeAll
    public static void init(){
        LoggingConfig.configLog();
    }

    @Test
    public void testMLPPaths(){
        Path globalLex = PomlpPathConstants.LatexGrammarLexiconFolder;
        assertTrue(
                globalLex.resolve("global-lexicon.txt").toFile().exists(),
                "Expected lexicon file for POM in submodule 'latex-grammar'!"
        );
    }

    @Test
    public void testMLPInit(){
        Path refPath =
                PomlpPathConstants.LatexGrammarReferenceDir;

        LOG.debug("Reference directory: " + refPath.toAbsolutePath().toString());
        try {
            PomParser parser = new PomParser(refPath.toAbsolutePath().toString());
            PomTaggedExpression pte = parser.parse(SIMPLE_LATEX);
            assertNotNull( pte.getComponents(), "Root node should not be null!" );
        } catch ( Exception e ){
            LOG.error(e.getMessage(), e);
            fail("Failed to parse simple latex expression.");
        }
    }
}
