package de.tuberlin.dima.schubotz.fse;

import eu.stratosphere.api.java.tuple.Tuple2;
import junit.framework.TestCase;

public class ArticleMapperTest extends TestCase {


    public void testMap() throws Exception {
        ArticleMapper articleMapper = new ArticleMapper();
        Tuple2<String, Integer> stringIntegerTuple2 = articleMapper.map(TestUtils.getTestFile1());
        System.out.println(stringIntegerTuple2);
    }
}