package de.tuberlin.dima.schubotz.fse;

import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.api.java.functions.CrossFunction;
import eu.stratosphere.api.java.functions.GroupReduceFunction;
import eu.stratosphere.api.java.io.TextInputFormat;
import eu.stratosphere.api.java.operators.DataSource;
import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.api.java.typeutils.BasicTypeInfo;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.core.fs.Path;
import eu.stratosphere.util.Collector;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class KeywordMapperTest extends TestCase {
	public static final String docsInput = "file://sampledata.html"; 
	public static final String queryInput = "file://samplequery.html";
	public static final String articleOutput = "sampleoutput.csv";
	public static final String resultsOutput = "sampleresultsoutput.csv";
	
	
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
        String DOCUMENT_SEPARATOR = "</ARXIVFILESPLIT>";
        format.setDelimiter(DOCUMENT_SEPARATOR);
        //rawArticleText format: data set of strings, delimited by <ARXIVFILESPLIT>
        DataSet<String> rawArticleText = new DataSource<>(env, format, BasicTypeInfo.STRING_TYPE_INFO);
        
        //Set up querydataset
        TextInputFormat formatQueries = new TextInputFormat(new Path(queryInput));
        DataSet<String> rawQueryText = new DataSource<>(env, formatQueries, BasicTypeInfo.STRING_TYPE_INFO);
        
        DataSet<Query> queryDataSet= rawQueryText.flatMap(new QueryMapper());
        
        //get dataset of <Document, List<Keyword tokens>>
        DataSet<KeyWordTuple> articleDataSet = rawArticleText.flatMap(new KeywordMapper()).withBroadcastSet(queryDataSet, "Queries");
       
        //DEBUG output
        articleDataSet.writeAsCsv(articleOutput);
        
        //reduce with querydataset where it returns KeyWordTuple.f1.contains(Query.keywords.values());
        DataSet<Tuple2<String,String>> results = articleDataSet.reduceGroup(new sameKeywords());
        
        //DEBUG output
        results.writeAsCsv(resultsOutput);
        
        env.execute("Mathosphere");
    }
    //TODO check if this is combineable: http://stratosphere.eu/docs/0.5/programming_guides/java.html
    //not sure if below annotation works (it's what eclipse recommended, but docs recommend @Combinable)
    @eu.stratosphere.api.java.functions.GroupReduceFunction.Combinable
    private class sameKeywords extends GroupReduceFunction<KeyWordTuple, Tuple2<String,String>> {
    	private Collection<Query> queries;
    	
    	@Override
    	public void reduce(Iterator<KeyWordTuple> in, Collector<Tuple2<String,String>> out) {
    		KeyWordTuple tup = in.next();
    		//check if query and keywordtuple contain same keywords
    		while (in.hasNext()) {
    			for (Query query : queries) {
	    			if (tup.f1.containsAll(query.keywords.values())) {
	    				out.collect(new Tuple2<String,String>(query.name,tup.f0));
	    			}
	    			tup = in.next();
    			}
    		}
    	}
    	/* no idea if this works */
    	@Override
    	public void combine(Iterator<KeyWordTuple> in, Collector<Tuple2<String, String>> out) {
    		// in some cases combine() calls can simply be forwarded to reduce().
    		this.reduce(in, out);
    	}
    	
    	@Override
        public void open(Configuration parameters) throws Exception {
    		queries = getRuntimeContext().getBroadcastVariable("Queries");
    	}
    }
}