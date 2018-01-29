package com.formulasearchengine.mathosphere.pomlp.pom;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.IntStream;

import com.formulasearchengine.mathmltools.xmlhelper.XmlDocumentReader;
import com.formulasearchengine.mathosphere.pomlp.GoldStandardLoader;
import com.formulasearchengine.mathosphere.pomlp.comparison.CSVResultWriter;
import com.formulasearchengine.mathosphere.pomlp.comparison.ComparisonError;
import com.formulasearchengine.mathosphere.pomlp.comparison.ComparisonResult;
import com.formulasearchengine.mathosphere.pomlp.comparison.RTEDTreeComparator;
import com.formulasearchengine.mathosphere.pomlp.convertor.Canonicalizable;
import com.formulasearchengine.mathosphere.pomlp.convertor.Converters;
import com.formulasearchengine.mathosphere.pomlp.gouldi.JsonGouldiBean;
import com.formulasearchengine.mathosphere.pomlp.util.DistanceCalculator;
import com.formulasearchengine.mathosphere.pomlp.util.config.ConfigLoader;
import com.formulasearchengine.mathosphere.pomlp.xml.MathMLDocumentReader;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Node;

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
        resultsWriter.writeToFile();
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
    void treeComparison( int number ) {
        LOG.info("Start [TEST: " + number + "]");
        LOG.debug("Load Goldstandard MML.");
        JsonGouldiBean goldBean = goldLoader.getGouldiJson(number);
        DistanceCalculator distanceCalculator = new DistanceCalculator( goldBean.getMml(), false );

        for ( Converters converter : Converters.values() ){
            Path fileP = converter.getFile(number);
            LOG.info("Load " + converter.name() + " file with number " + number + ".");
            Exception ie = null;

            try {
                MathMLDocumentReader reader = new MathMLDocumentReader( fileP );

                ComparisonResult result = distanceCalculator.calculateDistances( converter, number, reader );
                resultsWriter.addResult( result );
                ie = distanceCalculator.getException();
            } catch ( Exception e ){
                ComparisonError ce = new ComparisonError( converter, number, e );
                resultsWriter.addError( ce );
            }
            if ( ie != null ){
                ComparisonError ce = new ComparisonError( converter, number, ie );
                resultsWriter.addError( ce );
            }
        }
    }


}
