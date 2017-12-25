package com.formulasearchengine.mathosphere.pomlp.pom;

import com.formulasearchengine.mathosphere.pomlp.convertor.MathParser;
import com.formulasearchengine.mathosphere.pomlp.gouldi.JsonGouldiBean;
import com.formulasearchengine.mathosphere.pomlp.GoldStandardLoader;
import com.formulasearchengine.mathosphere.pomlp.util.Utility;
import com.formulasearchengine.mathosphere.pomlp.xml.MathMLDocumentReader;
import it.unibz.inf.rted.distance.RTED_InfoTree_Opt;
import it.unibz.inf.rted.util.LblTree;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.IntStream;

public class GouldiAutomaticComparisonTest {
    public static final String MAVEN_WRITE_RESULTS_KEY = "maven.writeresults";
    public static final String MAVEN_LOG_LEVEL = "maven.loglevel";

    private static final Logger LOG = LogManager.getLogger( GouldiAutomaticComparisonTest.class.getName() );

    public static final String CSV_SEP = ",";

    private static GoldStandardLoader goldLoader;

    private static ArrayList<Result> results;

    public static final Path RESULT_PATH = Paths.get("results");

    public static final String NL = System.lineSeparator();

    private static int max;

    @BeforeAll
    public static void init() {
        goldLoader = GoldStandardLoader.getInstance();
        max = goldLoader.initLocally();
        results = new ArrayList<>(max);

        try {
            String mavenLog = System.getProperty( MAVEN_LOG_LEVEL );
            if ( mavenLog != null ){
                Configurator.setAllLevels(
                        "com.formulasearchengine.mathosphere.pomlp.convertor.MathParser",
                        Level.getLevel(mavenLog)
                );
                Configurator.setRootLevel(Level.getLevel(mavenLog));
            }
        } catch ( Exception e ){
            LOG.trace("No log level specified by maven. DebugMSG: " + e.getMessage() );
        }
    }

    @AfterAll
    static void finish() throws IOException {
        try {
            String maven = System.getProperty( MAVEN_WRITE_RESULTS_KEY );
            if ( maven != null ){
                boolean mavenWrite = Boolean.parseBoolean(maven);
                if ( !mavenWrite ){
                    LOG.info("Maven test deactivated writing results. Process finish!");
                    return;
                }
            }
        } catch( Exception e ){
            LOG.trace("Error while reading maven property. Ignoring the error and write results...");
        }

        Collections.sort( results );
        LOG.info( "Finish all tests. Here the sorted results: " + System.lineSeparator() + results.toString());

        Path filePath = RESULT_PATH.resolve("latest.csv");
        LOG.info("Write results to " + filePath.toAbsolutePath());
        if (!Files.exists(filePath)) Files.createFile(filePath);
        try ( OutputStream out = Files.newOutputStream(filePath) ){
            out.write( (Result.CSV_HEADER+NL).getBytes() );
            results.stream()
                    .map( Result::toString )
                    .map( s -> s+NL )
                    .map( String::getBytes )
                    .forEach( b -> {
                        try{ out.write(b); }
                        catch(IOException e){ LOG.error("Cannot write line " + new String(b)); }
                    });
        } catch ( IOException ioe ){
            LOG.error("Cannot write result file.", ioe);
        }
    }

    private static IntStream gouldiRange(){
        return IntStream.range(1, max+1);
    }

    @ParameterizedTest
    @MethodSource( "gouldiRange" )
    void treeComparison( int number ){
        try {
            LOG.info("Start test: " + number + ".json");
            LOG.debug("Load MML...");
            JsonGouldiBean goldBean = goldLoader.getGouldiJson(number);
            String tex = goldBean.getOriginalTex();
            String mml = goldBean.getMml();

            LOG.debug("Parse MML to Documents...");
            MathMLDocumentReader mmlReader = new MathMLDocumentReader( mml );

            Node pmml = mmlReader.getPresentationNode();
            Node cmml = mmlReader.getContentNode();
            //LOG.trace("Extracted PMML: " + MathMLDocumentReader.debugToString( pmml ));
            //LOG.trace("Extracted CMML: " + MathMLDocumentReader.debugToString( cmml ));

            // parse tex with pom:
            LOG.debug("Parse math TeX by POM-Tagger...");
            MathParser parser = new MathParser();

            LOG.debug("Parse POM tree to XML...");
            tex = Utility.latexPreProcessing(tex);
            Document pomDocument = parser.parseLatexMathToDOM( tex );

            LOG.debug("Init tree comparison.");
            Node pomT = pomDocument.getDocumentElement();

            // compare trees
            RTED_InfoTree_Opt rted = new RTED_InfoTree_Opt(
                    1.0, // insertion costs
                    1.0, // deletion costs
                    0.0  // renaming costs
            );

            LOG.debug("Start tree comparison.");
            double distCont = rted.nonNormalizedTreeDist( LblTree.fromXML(pomT), LblTree.fromXML(cmml) );
            double distPres = rted.nonNormalizedTreeDist( LblTree.fromXML(pomT), LblTree.fromXML(pmml) );

            // results to file
            LOG.info("Content-Distance:      " + distCont);
            LOG.info("Presentation-Distance: " + distPres);
            results.add( new Result( number, (int)distCont, (int)distPres ) );
        } catch( Exception e ){
            LOG.error("Error in comparison test.", e);
            Assertions.fail("Error occurred: " + number);
        }
    }

    private class Result implements Comparable<Result>{
        private final int index;
        private final int contDist, presDist;
        public boolean CONTENT_MODE = true;

        public static final String CSV_HEADER = "JSON"+CSV_SEP+"ContentDistance"+CSV_SEP+"PresentationDistance";

        public Result( int index, int contDist, int presDist ){
            this.index = index;
            this.contDist = contDist;
            this.presDist = presDist;
        }

        @Override
        public int compareTo(Result o) {
            if ( CONTENT_MODE ) return contDist - o.contDist;
            else return presDist - o.presDist;
        }

        @Override
        public String toString(){
            return index + CSV_SEP + contDist + CSV_SEP + presDist;
        }
    }

    /* TODO: TestCase-Scenario
        1) Load GitHub Gouldi
        2) Extract TeX
        3) ParseTex with POM
        4) POM-XML
        5) Extract Content-Presentation from Gouldi
        6) Compare XML Trees

        -> Fails if error
        -> Collect Distances (in File)
     */

}
