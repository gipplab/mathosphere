package de.tuberlin.dima.schubotz.fse;

import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.api.java.io.TextInputFormat;
import eu.stratosphere.api.java.operators.DataSource;
import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.api.java.typeutils.BasicTypeInfo;
import eu.stratosphere.core.fs.Path;
import eu.stratosphere.util.Collector;
import junit.framework.TestCase;

import java.util.ArrayList;

public class ArticleMapperTest extends TestCase {
    public void testMap() throws Exception {
        ArticleMapper articleMapper = new ArticleMapper();
        final ArrayList<HitTuple> hits = new ArrayList<>();

        Collector<HitTuple> col = new Collector<HitTuple>() {
            @Override
            public void collect(HitTuple hit) {
                hits.add(hit);
            }

            @Override
            public void close() {

            }
        };
        
        //Demo function test
        Tuple2<String, Integer> articleNum = articleMapper.map(TestUtils.getTestFile10());
        System.out.println(articleNum);
        
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        
        articleMapper.flatMap(TestUtils.getTestFile1(), col);
        
        HitTuple hitsample = hits.get(0);
        
        System.out.println(hitsample.toString());

        /*
        articleMapper.flatMap(TestUtils.getTestFile1(), col);
        Article sample = articles.get(0);
        System.out.println(sample.content);
        System.out.println("collected");
        //System.out.println(ArticleMapper.getPlainText(new ByteArrayInputStream((TestUtils.getTestFile1()).getBytes(StandardCharsets.UTF_8))));
        // */
    }
}