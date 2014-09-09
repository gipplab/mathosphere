package de.tuberlin.dima.schubotz.fse.mappers.preprocess;

import de.tuberlin.dima.schubotz.fse.mappers.DataPreprocessTemplate;
import de.tuberlin.dima.schubotz.fse.types.DataTuple;
import de.tuberlin.dima.schubotz.utils.TestUtils;
import junit.framework.TestCase;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.util.Collector;

public class DataPreprocessTemplateTest extends TestCase {


    public void testSetDoc() throws Exception {
    final String testdata= TestUtils.getFileContents("de/tuberlin/dima/schubotz/utils/xmlFailTestEq.xml");
        DataPreprocessTemplate<DataTuple,Tuple1<String>> dp = new DataPreprocessTemplate<DataTuple, Tuple1<String>>() {
	        @Override
	        public void flatMap (DataTuple value, Collector<Tuple1<String>> out) throws Exception {
		        return;
	        }
        };
     dp.data=testdata;
        dp.setDoc();
        dp.setMath();
        System.out.println(dp.mathElements.toString());
    }

    public void testSetMath() throws Exception {

    }
}