package de.tuberlin.dima.schubotz.wiki.mappers;

import de.tuberlin.dima.schubotz.wiki.types.WikiQueryTuple;
import eu.stratosphere.api.java.DataSet;
import org.junit.Ignore;
import org.junit.Test;
import de.tuberlin.dima.schubotz.fse.wiki.mappers.WikiQueryMapper;


/**
 * Used to test WikiQueryMapper on the query sets.
 * If running tests on full query set, the others should be @Ignore annotated.
 * TODO parameterize this test
 */
public class WikiQueryMapTest extends WikiAbstractSubprocessTest {
    @Ignore
    public void testFullQuery() throws Exception {
        String inputFilename = "de/tuberlin/dima/schubotz/wiki/mappers/wikiQuery.xml";
        String expectedOutputFilename = "de/tuberlin/dima/schubotz/wiki/mappers/wikiQuery.expected.csv";
        DataSet<String> data = (DataSet<String>) getCleanedData(inputFilename);
        DataSet<WikiQueryTuple> outputSet = data.flatMap(new WikiQueryMapper(STR_SPLIT));
        testDataMap(outputSet, expectedOutputFilename);
    }
    @Test
    /**
     * Tests on queries with known problems with MathMLCan.
     */
    public void testSpecialQuery() throws Exception {
        String inputFilename = "de/tuberlin/dima/schubotz/wiki/mappers/wikiSpecialQuery.xml";
        String expectedOutputFilename = "de/tuberlin/dima/schubotz/wiki/mappers/wikiSpecialQuery.expected.csv";
        DataSet<String> data = (DataSet<String>) getCleanedData(inputFilename);
        DataSet<WikiQueryTuple> outputSet = data.flatMap(new WikiQueryMapper(STR_SPLIT));
        testDataMap(outputSet, expectedOutputFilename);
    }
    @Ignore
    /**
     * Tests on first two queries.
     */
    public void testQuickQuery() throws Exception {
        String inputFilename = "de/tuberlin/dima/schubotz/wiki/mappers/wikiQuickQuery.xml";
        String expectedOutputFilename = "de/tuberlin/dima/schubotz/wiki/mappers/wikiQuickQuery.expected.csv";
        DataSet<String> data = (DataSet<String>) getCleanedData(inputFilename);
        DataSet<WikiQueryTuple> outputSet = data.flatMap(new WikiQueryMapper(STR_SPLIT));
        testDataMap(outputSet, expectedOutputFilename);
    }
}
