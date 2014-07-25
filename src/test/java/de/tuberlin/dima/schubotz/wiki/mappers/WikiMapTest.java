package de.tuberlin.dima.schubotz.wiki.mappers;

import de.tuberlin.dima.schubotz.wiki.types.WikiTuple;
import eu.stratosphere.api.java.DataSet;
import org.junit.Test;

/**
 * Used to test WikiMapper on the wiki sets with dependency on wikiQuery.expected files.
 * If running tests on full wiki set, the others should be @Ignore annotated.
 */
public class WikiMapTest extends WikiAbstractSubprocessTest {
    @Test
    /**
     * Test on first two wikis.
     */
    public void testQuickDatset() throws Exception {
        String inputFilename = "de/tuberlin/dima/schubotz/wiki/mappers/wikiQuickDump.xml";
        String queryFilename = "de/tubelrin/dima/schubotz/wiki/mappers/wikiQuickQuery.expected.xml";
        String expectedOutputFilename = "de/tuberlin/dima/schubotz/wiki/mappers/wikiQuickDump.expected.xml";
        DataSet<String> data = getCleanedData(inputFilename);
        DataSet<String> queries = getCleanedData(queryFilename);
        DataSet<WikiTuple> outputSet = data.flatMap(new WikiMapper(STR_SPLIT))
                                          .withBroadcastSet(queries, "Queries");
        testDataMap(outputSet, expectedOutputFilename);

    }
}
