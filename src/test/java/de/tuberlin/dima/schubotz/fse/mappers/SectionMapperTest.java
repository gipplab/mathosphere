package de.tuberlin.dima.schubotz.fse.mappers;

import com.google.common.collect.HashMultiset;
import de.tuberlin.dima.schubotz.common.utils.TestUtils;
import de.tuberlin.dima.schubotz.fse.types.SectionTuple;
import eu.stratosphere.util.Collector;
import junit.framework.TestCase;

import java.util.regex.Pattern;

public class SectionMapperTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();

    }
    class DummyCollector implements Collector<SectionTuple>{

        @Override
        public void collect(SectionTuple record) {
            System.out.println( record.toString() );
        }

        @Override
        public void close() {

        }
    }
    public void testFlatMap() throws Exception {
        String test1 = TestUtils.getTestFile1();
        DummyCollector dummyCollector = new DummyCollector();
        HashMultiset<String> dummyKeyWords = HashMultiset.create();
        SectionMapper sm = new SectionMapper(Pattern.compile(""),"",dummyKeyWords);
        sm.flatMap(test1,dummyCollector);
    }
}