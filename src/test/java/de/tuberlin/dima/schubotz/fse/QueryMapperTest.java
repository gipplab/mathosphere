package de.tuberlin.dima.schubotz.fse;

import eu.stratosphere.util.Collector;
import org.junit.Test;

import java.util.ArrayList;

public class QueryMapperTest {

    @Test
    public void testFlatMap() throws Exception {
        QueryMapper queryMapper = new QueryMapper();
        final ArrayList<Query> queries = new ArrayList<>();
        Collector<Query> col = new Collector<Query>() {
            @Override
            public void collect(Query record) {
                System.out.println(record.name);
                queries.add(record);
            }

            @Override
            public void close() {

            }
        };
        queryMapper.flatMap(TestUtils.getTestQueryString(), col);

    }
}