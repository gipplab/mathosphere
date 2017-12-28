package com.formulasearchengine.mathosphere.pomlp.pom;

import com.formulasearchengine.mathosphere.pomlp.GoldStandardLoader;
import com.formulasearchengine.mathosphere.pomlp.comparison.CSVResultWriter;
import com.formulasearchengine.mathosphere.pomlp.comparison.ComparisonError;
import com.formulasearchengine.mathosphere.pomlp.comparison.ComparisonResult;
import com.formulasearchengine.mathosphere.pomlp.comparison.RTEDTreeComparator;
import com.formulasearchengine.mathosphere.pomlp.convertor.Canonicalizable;
import com.formulasearchengine.mathosphere.pomlp.convertor.Converters;
import com.formulasearchengine.mathosphere.pomlp.gouldi.JsonGouldiBean;
import com.formulasearchengine.mathosphere.pomlp.util.config.ConfigLoader;
import com.formulasearchengine.mathosphere.pomlp.xml.MathMLDocumentReader;
import com.formulasearchengine.mathosphere.pomlp.xml.XmlDocumentReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Node;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * This class should be excluded in the maven tests because
 * it automatically runs tree comparisons over the entire data set.
 *
 * @author Andre Greiner-Petter
 */
@TestInstance( TestInstance.Lifecycle.PER_CLASS )
public class GoldstandardAutomaticComparisonTest {
    // Logger
    private static final Logger LOG = LogManager.getLogger( GoldstandardAutomaticComparisonTest.class.getName() );

    // Gold Loader
    private GoldStandardLoader goldLoader;

    private CSVResultWriter resultsWriter;

    private RTEDTreeComparator RTED;

    private static int max;

    @BeforeAll
    void init() {
        LOG.info("Load gold standard and initialize result handler.");
        goldLoader = GoldStandardLoader.getInstance();
        max = goldLoader.initLocally();
        resultsWriter = new CSVResultWriter(max);
        String goldPath = ConfigLoader.CONFIG.getProperty( ConfigLoader.GOULDI_LOCAL_PATH );
        Path goldLibPath = Paths.get(goldPath);
        for ( Converters conv : Converters.values() )
            conv.initSubPath( goldLibPath );

        RTED = new RTEDTreeComparator();
    }

    @AfterAll
    void finish() throws IOException {
        LOG.info("Finished all comparisons. Write results to CSV file.");
        Path resultsPath = Paths.get("results");
        resultsWriter.writeToFile( resultsPath );
    }

    /**
     * Defines the range of the tests
     * @return
     */
    private static IntStream gouldiRange(){
        return IntStream.range(1, max+1);
    }

    @ParameterizedTest
    @MethodSource( "gouldiRange" )
    void treeComparison( int number ) throws IOException {
        LOG.info("Start [TEST: " + number + "]");
        LOG.debug("Load Goldstandard MML.");
        JsonGouldiBean goldBean = goldLoader.getGouldiJson(number);
        String goldmmlStr = goldBean.getMml();
        MathMLDocumentReader gold = new MathMLDocumentReader(goldmmlStr);
        Node goldPMML = gold.getPresentationNode();
        Node goldCMML = gold.getContentNode();

        MathMLDocumentReader reader;
        Node xmlN;
        boolean failed = false;

        for ( Converters converter : Converters.values() ){
            ComparisonResult result = new ComparisonResult( number, converter );
            Path fileP = converter.getFile(number);
            LOG.info("Load saved " + converter.name() + " file with number " + number + ".");
            try {
                if ( converter.isMML() ){
                    reader = new MathMLDocumentReader( fileP );
                    if ( converter.getParser() instanceof Canonicalizable ){
                        LOG.debug("Canonicalize MML: " + fileP);
                        reader.canonicalize();
                    }

                    LOG.trace( "Content: " + MathMLDocumentReader.debugToString(reader.getContentNode()) );
                    LOG.trace( "Present: " + MathMLDocumentReader.debugToString(reader.getPresentationNode()) );

                    if ( reader.getContentNode() != null ){
                        result.setContentDistance(
                                RTED.computeDistance(
                                        reader.getContentNode(),
                                        goldCMML
                                )
                        );
                    } else {
                        LOG.debug("File " + fileP + " has no content MML.");
                    }
                    if ( reader.getPresentationNode() != null ){
                        result.setPresentationDistance(
                                RTED.computeDistance(
                                        reader.getPresentationNode(),
                                        goldPMML
                                )
                        );
                    } else {
                        LOG.debug("File " + fileP + " has no presentation MML.");
                    }
                } else {
                    // just xml
                    xmlN = XmlDocumentReader.getNodeFromXML( fileP );
                    result.setPresentationDistance(RTED.computeDistance( xmlN, goldPMML ));
                    result.setContentDistance(RTED.computeDistance( xmlN, goldCMML ));
                }
                LOG.debug("Comparison finished: " + result.toString());
                resultsWriter.addResult( result );
            } catch ( Exception e ){
                LOG.error("Cannot compare " + fileP, e);
                resultsWriter.addError(new ComparisonError(converter, number, e));
                failed = true;
            }
        }

        if ( failed ) fail("Comparison throws an exception. See logs for details!");
    }
}
