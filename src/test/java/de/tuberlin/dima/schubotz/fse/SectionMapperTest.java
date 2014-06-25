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

public class SectionMapperTest extends TestCase {
    public void testMap() throws Exception {
        SectionMapper sectionMapper = new SectionMapper();
        final ArrayList<SectionTuple> articles = new ArrayList<>();

        Collector<SectionTuple> col = new Collector<SectionTuple>() {
            @Override
            public void collect(SectionTuple article) {
                articles.add(article);
            }

            @Override
            public void close() {

            }
        };
        
        /*
        Tuple2<String, Integer> articleNum = sectionMapper.map(TestUtils.getTestFile1());
        System.out.println(articleNum);/*
        articleMapper.flatMap(TestUtils.getTestFile1(), col);
        Article sample = articles.get(0);
        System.out.println(sample.content);
        System.out.println("collected");
        //System.out.println(ArticleMapper.getPlainText(new ByteArrayInputStream((TestUtils.getTestFile1()).getBytes(StandardCharsets.UTF_8))));
        // */
    }
}