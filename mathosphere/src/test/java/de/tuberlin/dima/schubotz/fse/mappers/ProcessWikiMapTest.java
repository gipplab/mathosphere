package de.tuberlin.dima.schubotz.fse.mappers;

/**
 * Used to test WikiMapper on the de.tuberlin.dima.schubotz.fse.wiki sets with dependency on wikiQuery.expected files.
 * If running tests on full de.tuberlin.dima.schubotz.fse.wiki set, the others should be @Ignore annotated.
 * TODO parameterize this
 */
public class ProcessWikiMapTest extends WikiAbstractSubprocessTest {
    /**
     * Test on first two wikis.
     */
    /*
    @Ignore
    public void testQuickDataset() throws Exception {
        String inputFilename = "de/tuberlin/dima/schubotz/wiki/mappers/wikiQuickDump.xml";
        String expectedOutputFilename = "de/tuberlin/dima/schubotz/wiki/mappers/wikiQuickDump.expected.csv";
        DataSet<String> data = (DataSet<String>) getCleanedData(inputFilename);
        DataSet<WikiTuple> outputSet = data.flatMap(new ProcessWikiMapper(STR_SPLIT));
        testDataMap(outputSet, expectedOutputFilename);

    }
    /**
     * Test on full de.tuberlin.dima.schubotz.fse.wiki set. Make sure to change inputFilename to wherever dataset is located.
     */
    /*
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
    /*
    @Ignore
    public void testSpecialDataset() throws Exception {
        String inputFilename = "de/tuberlin/dima/schubotz/wiki/mappers/wikiSpecialDump.xml";
        String expectedOutputFilename = "de/tuberlin/dima/schubotz/wiki/mappers/wikiSpecialDump.expected.csv";
        DataSet<String> data = (DataSet<String>) getCleanedData(inputFilename);
        DataSet<WikiTuple> outputSet = data.flatMap(new ProcessWikiMapper(STR_SPLIT));
        testDataMap(outputSet, expectedOutputFilename);
    }
    /**
     * Test on training de.tuberlin.dima.schubotz.fse.wiki (first 50 of dump, used to evaluate current progress)
     */
    /*
    @Test
    public void testTrainingDataset() throws Exception {
        String inputFilename = "de/tuberlin/dima/schubotz/wiki/mappers/wikiTrainingDump.xml";
        String expectedOutputFilename = "de/tuberlin/dima/schubotz/wiki/mappers/wikiTrainingDump.expected.csv";
        DataSet<String> data = (DataSet<String>) getCleanedData(inputFilename);
        DataSet<WikiTuple> outputSet = data.flatMap(new ProcessWikiMapper(STR_SPLIT));
        testDataMap(outputSet, expectedOutputFilename);
    }*/
}
