package de.tuberlin.dima.schubotz.fse.mappers.cleaners;

import de.tuberlin.dima.schubotz.fse.types.RawDataTuple;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.LocalCollectionOutputFormat;
import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.operators.FlatMapOperator;
import org.apache.flink.api.java.typeutils.BasicTypeInfo;
import org.apache.flink.core.fs.Path;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QueryCleanerTest {


    @Test
    public void testGetDelimiter() throws Exception {
        assertEquals("</topic>",(new QueryCleaner().getDelimiter()));
    }

    @Test
    public void testFlatMap() throws Exception {
        Collection<RawDataTuple> outData = getQueryList();
        for (RawDataTuple rawDataTuple : outData) {

            String title = rawDataTuple.getField(0).toString();
            final String pattern = "NTCIR11-Math-\\d+";
            if ( title.matches(pattern) ){
                assertTrue(true);
            } else {
                assertEquals("pattern did not match",pattern,title);
            }
        }

    }
    //TODO: figure out how to serialize to speed up testing
/*
    public void storeCsv() throws Exception{
        ArrayList<RawDataTuple> outData = (ArrayList<RawDataTuple>) getQueryList();
        ObjectMapper mapper =  ObjectMapperFactory.create();
        URL url = getClass().getClassLoader().getResource("de/tuberlin/dima/schubotz/fse/processedQueries.json");
        File file = File.createTempFile("processedQueries", ".json");
        Boon.puts("json string", mapper.writeValueAsString(outData));
        mapper.writeValue(file,outData);
    }*/

    public Collection<RawDataTuple> getQueryList() throws Exception {
        final ExecutionEnvironment env = ExecutionEnvironment.createLocalEnvironment();

        final TextInputFormat inputQueries = new TextInputFormat(
                 new Path(getClass().getClassLoader().
                         getResource("de/tuberlin/dima/schubotz/fse/fQuery.xml").toURI()));
        QueryCleaner queryCleaner = new QueryCleaner();
        inputQueries.setDelimiter(queryCleaner.getDelimiter());
        final DataSet<String> input = new DataSource<>(env, inputQueries, BasicTypeInfo.STRING_TYPE_INFO);
        Collection<RawDataTuple> outData = new ArrayList<>();
        final FlatMapOperator<String, RawDataTuple> output = input.flatMap(new QueryCleaner());
        output.output(new LocalCollectionOutputFormat<>(outData));
        env.execute();
        return outData;
    }
}