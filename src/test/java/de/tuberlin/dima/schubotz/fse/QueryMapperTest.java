package de.tuberlin.dima.schubotz.fse;

import eu.stratosphere.util.Collector;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;

public class QueryMapperTest {
    @Test
    public void testFlatMap() throws Exception {
        QueryMapper queryMapper = new QueryMapper();
        final ArrayList<Query> queries = new ArrayList<>();
        Collector<Query> col = new Collector<Query>() {
            @Override
            public void collect(Query record) {
                // System.out.println(record.name);
                queries.add(record);
            }

            @Override
            public void close() {

            }
        };
        queryMapper.flatMap(TestUtils.getTestQueryString(), col);
        Query query10 = queries.get(9);
        assertEquals("NTCIR11-Math-10", query10.name);
        assertEquals(1, query10.formulae.size());
        assertEquals(4, query10.keywords.size());
        assertEquals(true, query10.keywords.containsKey("w1.3"));
        assertEquals("code", query10.keywords.get("w1.3"));
        assertEquals(true, query10.formulae.containsKey("f1.0"));
        Document testDoc = query10.formulae.get("f1.0");
        Node texExpression = XMLHelper.getElementB(testDoc, "/math/semantics/annotation");
        assertEquals("f(\\qvar{x})=\\frac{1}{\\sigma\\sqrt{2\\pi}}\\qvar{z}", texExpression.getTextContent().trim());
    }
}