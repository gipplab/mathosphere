package de.tuberlin.dima.schubotz.fse.mappers.preprocess;

import de.tuberlin.dima.schubotz.fse.mappers.cleaners.QueryCleanerTest;
import de.tuberlin.dima.schubotz.fse.types.RawDataTuple;
import de.tuberlin.dima.schubotz.fse.utils.CMMLInfo;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.util.Collector;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.fail;

public class ExtractCMMLTest {

    private Collection<RawDataTuple> queryList;

    @Before
    public void setUp() throws Exception {
        queryList = (new QueryCleanerTest()).getQueryList();
    }

    @Test
    public void testGetElement() throws Exception {
        final ExtractCMML extractCMML = new ExtractCMML();

        Collector<Tuple3<Integer,String,String>> out = new Collector<Tuple3<Integer,String,String>>() {
            @Override
            public void collect(Tuple3<Integer,String,String> record) {
                //System.out.println(record.getField(0)+","+record.getField(1)+","+record.getField(2));
                try {
                    // Fail if XQuery string can not be generated
                    CMMLInfo cmml = new CMMLInfo((String) record.getField(2));
                    System.out.println(cmml.getXQueryString());
                } catch (Exception e) {
                    e.printStackTrace();
                    fail();
                }
            }

            @Override
            public void close() {

            }
        };

        for (RawDataTuple tuple : queryList) {
            extractCMML.flatMap(tuple,out);
        }


    }
}