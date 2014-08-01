package de.tuberlin.dima.schubotz.wiki.mappers;

import de.tuberlin.dima.schubotz.wiki.preprocess.ProcessLatexWikiMapper;
import de.tuberlin.dima.schubotz.wiki.types.WikiQueryTuple;
import de.tuberlin.dima.schubotz.wiki.types.WikiTuple;
import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.tuple.Tuple2;
import org.junit.Test;

/**
 * Created by jjl4 on 8/1/14.
 */
public class ProcessLatexWikiMapTest extends WikiAbstractSubprocessTest {
    /**
     * Test on training data.
     */
    @Test
    public void testTrainingDataset() throws Exception {
        String inputFilename = "de/tuberlin/dima/schubotz/wiki/mappers/wikiTrainingDump.xml";
        String queryFilename = "de/tuberlin/dima/schubotz/wiki/mappers/wikiQuery.expected.csv";
        String expectedOutputFilename = "de/tuberlin/dima/schubotz/wiki/mappers/wikiTrainingDump.expectedLatex.csv";
        //TODO refactor so typecast is not guessed
        DataSet<String> data = (DataSet<String>) getCleanedData(inputFilename);
        DataSet<WikiQueryTuple> queries = (DataSet<WikiQueryTuple>) getCleanedData(queryFilename);
        DataSet<Tuple2<String, Integer>> outputSet = data.flatMap(new ProcessLatexWikiMapper(STR_SPLIT))
                                                        .withBroadcastSet(queries, "Queries");
        testDataMap(outputSet, expectedOutputFilename);

    }
}
