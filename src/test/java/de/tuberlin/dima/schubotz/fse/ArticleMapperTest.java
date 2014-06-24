package de.tuberlin.dima.schubotz.fse;

import java.util.ArrayList;

import junit.framework.TestCase;
import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.util.Collector;

public class ArticleMapperTest extends TestCase {
    public void testMap() throws Exception {
        ArticleMapper articleMapper = new ArticleMapper();
    	final ArrayList<Article> articles = new ArrayList<>();
    	
    	Collector<Article> col = new Collector<Article>() {
            @Override
            public void collect(Article article) {
                articles.add(article);
            }

            @Override
            public void close() {

            }
        };
        
        Tuple2<String, Integer> articleNum = articleMapper.map(TestUtils.getTestFile1());
        System.out.println(articleNum);
        articleMapper.flatMap(TestUtils.getTestFile1(), col);
        Article sample = articles.get(0);
        System.out.println(sample.content);
        System.out.println("collected");
        //System.out.println(ArticleMapper.getPlainText(new ByteArrayInputStream((TestUtils.getTestFile1()).getBytes(StandardCharsets.UTF_8))));
    }
}