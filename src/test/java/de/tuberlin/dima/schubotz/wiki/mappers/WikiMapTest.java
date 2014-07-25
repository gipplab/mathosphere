package de.tuberlin.dima.schubotz.wiki.mappers;

import de.tuberlin.dima.schubotz.wiki.types.WikiQueryTuple;
import de.tuberlin.dima.schubotz.wiki.types.WikiTuple;
import eu.stratosphere.api.java.DataSet;
import org.junit.Test;

/**
 * Used to test WikiMapper on the wiki sets with dependency on wikiQuery.expected files.
 * If running tests on full wiki set, the others should be @Ignore annotated.
 * TODO parameterize this
 */
public class WikiMapTest extends WikiAbstractSubprocessTest {
    /**
     * Test on first two wikis.
     */
    @Test
    public void testQuickDataset() throws Exception {
        String inputFilename = "de/tuberlin/dima/schubotz/wiki/mappers/wikiQuickDump.xml";
        String expectedOutputFilename = "de/tuberlin/dima/schubotz/wiki/mappers/wikiQuickDump.expected.csv";
        DataSet<String> data = (DataSet<String>) getCleanedData(inputFilename);
        DataSet<WikiTuple> outputSet = data.flatMap(new WikiMapper(STR_SPLIT));
        testDataMap(outputSet, expectedOutputFilename);

    }
}
