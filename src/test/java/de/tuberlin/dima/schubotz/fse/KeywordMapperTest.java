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

public class KeywordMapperTest extends TestCase {
	public static final String docsInput = "sampledata.html"; 
	public static final String queryInput = "samplequery.html";
	public static final String articleOutput = "sampleoutput.csv";
	
	
    public void testMap() throws Exception {
        KeywordMapper keywordMapper = new KeywordMapper();
        final ArrayList<KeyWordTuple> keywords = new ArrayList<>();

        Collector<KeyWordTuple> col = new Collector<KeyWordTuple>() {
            @Override
            public void collect(KeyWordTuple keyword) {
                keywords.add(keyword);
            }

            @Override
            public void close() {

            }
        };
        
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        
        //Set up articleDataSet
        TextInputFormat format = new TextInputFormat(new Path(docsInput));
        //rawArticleText format: data set of strings, delimited by <ARXIFFILESPLIT>
        DataSet<String> rawArticleText = new DataSource<>(env, format, BasicTypeInfo.STRING_TYPE_INFO);
        
        //Set up querydataset
        TextInputFormat formatQueries = new TextInputFormat(new Path(queryInput));
        DataSet<String> rawQueryText = new DataSource<>(env, formatQueries, BasicTypeInfo.STRING_TYPE_INFO);
        
        DataSet<Query> queryDataSet= rawQueryText.flatMap(new QueryMapper());
        
        //testing keyword search
        DataSet<KeyWordTuple> articleDataSet = rawArticleText.flatMap(new KeywordMapper()).withBroadcastSet(queryDataSet, "Queries");
        
        articleDataSet.writeAsCsv(articleOutput);
    }
}