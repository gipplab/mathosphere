package de.tuberlin.dima.schubotz.wiki.mappers;

import de.tuberlin.dima.schubotz.wiki.types.WikiQueryTuple;
import eu.stratosphere.api.java.DataSet;
import org.junit.Test;


/**
 * Used to test WikiQueryMapper
 */
public class WikiQueryMapTest extends WikiAbstractSubprocessTest {
    /*@Test
    public void testFullQuery() throws Exception {
        String inputFilename = "de/tuberlin/dima/schubotz/wiki/mappers/wikiQuery.xml";
        String expectedOutputFilename = "de/tuberlin/dima/schubotz/wiki/mappers/wikiQuery.expected.xml";
        DataSet<String> data = getCleanedData(inputFilename);
        DataSet<WikiQueryTuple> outputSet = data.flatMap(new WikiQueryMapper(STR_SPLIT));
        testDataMap(outputSet, expectedOutputFilename);
    }*/
    @Test
    public void testSpecialQuery() throws Exception {
        String inputFilename = "de/tuberlin/dima/schubotz/wiki/mappers/wikiQuerySpecial.xml";
        String expectedOutputFilename = "de/tuberlin/dima/schubotz/wiki/mappers/wikiQuerySpecial.expected.xml";
        DataSet<String> data = getCleanedData(inputFilename);
        DataSet<WikiQueryTuple> outputSet = data.flatMap(new WikiQueryMapper(STR_SPLIT));
        testDataMap(outputSet, expectedOutputFilename);
    }
}
