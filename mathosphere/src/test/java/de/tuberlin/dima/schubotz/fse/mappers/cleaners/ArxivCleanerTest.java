package de.tuberlin.dima.schubotz.fse.mappers.cleaners;

import de.tuberlin.dima.schubotz.fse.mappers.cleaners.ArxivCleaner;
import de.tuberlin.dima.schubotz.fse.types.RawDataTuple;
import de.tuberlin.dima.schubotz.utils.GenericCollector;
import org.apache.flink.util.Collector;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Created by Jimmy on 9/3/2014.
 */
public class ArxivCleanerTest {
    @Test
    public void testFilenames() {
        final List<String> testFiletags = new ArrayList<>();
        testFiletags.add("<ARXIVFILESPLIT\\nFilename=\"./1/0704.0097/0704.0097_1_10.xhtml\"><?xml asdf?><html>asdf</html>");
        testFiletags.add("<ARXIVFILESPLIT Filename=\"./7/1006.1022/1006.1022_1_11.xhtml\"><?xml asdf?><html>asdf</html>");
        testFiletags.add("<ARXIVFILESPLIT Filename=\"./10/gr-qc9710100/gr-qc9710100_1_6.xhtml\"><?xml asdf?><html>asdf</html>");
        testFiletags.add("<ARXIVFILESPLIT Filename=\"./23/1243.4583/1243.2312_3_2.xhtml\">");
        testFiletags.add("<ARXIVFILESPLIT Filename=\"./14/4321.2957/9876.2376_5_2.xhtml\"><?xml asdf?>");
        testFiletags.add("<ARXIVFILESPLIT Filename=\"./21/8576.2353/5603.1203_1_3.xhtml\"></html>");

        final Collection<RawDataTuple> expectedData = new ArrayList<>();
        expectedData.add(new RawDataTuple("0704.0097_1_10", "<?xml asdf?><html>asdf</html>"));
        expectedData.add(new RawDataTuple("1006.1022_1_11", "<?xml asdf?><html>asdf</html>"));
        expectedData.add(new RawDataTuple("gr-qc9710100_1_6", "<?xml asdf?><html>asdf</html>"));

        final GenericCollector<RawDataTuple> dummyCollector = new GenericCollector<>();
        final ArxivCleaner ac = new ArxivCleaner();

        for (final String test : testFiletags) {
            ac.flatMap(test, dummyCollector);
        }
        final Collection<RawDataTuple> retrievedData = new ArrayList<>();
        for (final RawDataTuple tuple : dummyCollector.getDatalist()) {
            retrievedData.add(tuple);
        }
        assertArrayEquals(expectedData.toArray(), retrievedData.toArray());

    }
    @Test
    public void getDelimiterTest(){
		final ArxivCleaner arxivCleaner = new ArxivCleaner();
		assertEquals( "</ARXIVFILESPLIT>",arxivCleaner.getDelimiter() );
	}
}
