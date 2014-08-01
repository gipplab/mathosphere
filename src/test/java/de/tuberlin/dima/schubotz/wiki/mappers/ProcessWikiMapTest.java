package de.tuberlin.dima.schubotz.wiki.mappers;

import de.tuberlin.dima.schubotz.wiki.preprocess.ProcessWikiMapper;
import de.tuberlin.dima.schubotz.wiki.types.WikiTuple;
import eu.stratosphere.api.java.DataSet;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Used to test WikiMapper on the wiki sets with dependency on wikiQuery.expected files.
 * If running tests on full wiki set, the others should be @Ignore annotated.
 * TODO parameterize this
 */
public class ProcessWikiMapTest extends WikiAbstractSubprocessTest {
    /**
     * Test on first two wikis.
     */
    @Ignore
    public void testQuickDataset() throws Exception {
        String inputFilename = "de/tuberlin/dima/schubotz/wiki/mappers/wikiQuickDump.xml";
        String expectedOutputFilename = "de/tuberlin/dima/schubotz/wiki/mappers/wikiQuickDump.expected.csv";
        DataSet<String> data = (DataSet<String>) getCleanedData(inputFilename);
        DataSet<WikiTuple> outputSet = data.flatMap(new ProcessWikiMapper(STR_SPLIT));
        testDataMap(outputSet, expectedOutputFilename);

    }
    /**
     * Test on full wiki set. Make sure to change inputFilename to wherever dataset is located.
     */
    @Ignore
    public void testFullDataset() throws Exception {
        String inputFilename = "/home/jjl4/wikiAugmentedDump.xml";
        String expectedOutputFilename = "de/tuberlin/dima/schubotz/wiki/mappers/wikiAugmentedDump.expected.csv";
        DataSet<String> data = (DataSet<String>) getCleanedData(inputFilename);
        DataSet<WikiTuple> outputSet = data.flatMap(new ProcessWikiMapper(STR_SPLIT));
        testDataMap(outputSet, expectedOutputFilename);
    }
    /**
     * Test on known problem wikis.
     */
    @Test
    public void testSpecialDataset() throws Exception {
        String inputFilename = "de/tuberlin/dima/schubotz/wiki/mappers/wikiSpecialDump.xml";
        String expectedOutputFilename = "de/tuberlin/dima/schubotz/wiki/mappers/wikiSpecialDump.expected.csv";
        DataSet<String> data = (DataSet<String>) getCleanedData(inputFilename);
        DataSet<WikiTuple> outputSet = data.flatMap(new ProcessWikiMapper(STR_SPLIT));
        testDataMap(outputSet, expectedOutputFilename);
    }
}
