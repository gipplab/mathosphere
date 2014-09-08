package de.tuberlin.dima.schubotz.fse.mappers;

import de.tuberlin.dima.schubotz.fse.mappers.cleaners.ArxivCleaner;
import de.tuberlin.dima.schubotz.fse.types.RawDataTuple;
import org.apache.flink.util.Collector;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

/**
 * Created by Jimmy on 9/3/2014.
 */
public class ArxivCleanerTest {
    public static class DummyCollector implements Collector<RawDataTuple> {
        private final ArrayList<String> data = new ArrayList<>();

        public List<String> getData() {
            return new ArrayList<>(data);
        }

        @Override
        public void collect(RawDataTuple record) {
            data.add(record.getNamedField(RawDataTuple.fields.ID));
        }

        @Override
        public void close() {

        }
    }
    @Test
    public void testFilenames() {
        final List<String> testFiletags = new ArrayList<>();
        testFiletags.add("<ARXIVFILESPLIT\\nFilename=\"./1/0704.0097/0704.0097_1_10.xhtml\"><?xml asdf");
        testFiletags.add("<ARXIVFILESPLIT Filename=\"./7/1006.1022/1006.1022_1_11.xhtml\"><?xml asdf");
        testFiletags.add("<ARXIVFILESPLIT Filename=\"./10/gr-qc9710100/gr-qc9710100_1_6.xhtml\"><?xml asdf");

        final List<String> expectedFilenames = new ArrayList<>();
        expectedFilenames.add("0704.0097_1_10");
        expectedFilenames.add("1006.1022_1_11");
        expectedFilenames.add("gr-qc9710100_1_6");

        final DummyCollector dummyCollector = new DummyCollector();
        final ArxivCleaner ac = new ArxivCleaner();

        for (final String test : testFiletags) {
            ac.flatMap(test, dummyCollector);
        }
        final List<String> retrievedFilenames = dummyCollector.getData();

        assertArrayEquals(expectedFilenames.toArray(), retrievedFilenames.toArray());

    }
}
