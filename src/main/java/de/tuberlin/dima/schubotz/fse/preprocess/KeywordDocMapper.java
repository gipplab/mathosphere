package de.tuberlin.dima.schubotz.fse.preprocess;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;

import de.tuberlin.dima.schubotz.fse.MainProgram;
import de.tuberlin.dima.schubotz.fse.QueryTuple;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.util.Collector;

public class KeywordDocMapper extends FlatMapFunction<String, Tuple2<String,Integer>> {
	HashSet<String> keywords;
	Pattern WORD_SPLIT = MainProgram.WORD_SPLIT;
	
	@Override
	public void open(Configuration parameters) {
		//Get keywords from queries
		keywords = new HashSet<String>();
		Collection<QueryTuple> queries = getRuntimeContext().getBroadcastVariable( "Queries" );
		for (QueryTuple query : queries) {
			String[] tokens = query.getKeywords().split( "<S>" ); //get list of keywords
			for ( String token : tokens ) {
				if (!token.equals("")) {
					keywords.add(token);
				}
			}
		}
	}
	
	@Override
	public void flatMap(String value, Collector<Tuple2<String,Integer>> out) {
		//Takes in document, outputs tuple of <keyword,1> for every document containing keyword contained in set of query keywords
		//Extract plaintext from article
		String plainText = "";
		try {
			plainText = Jsoup.parse(value).text();
		} catch (Exception e){
			System.out.println("JSoup could not parse document (KeywordDocMapper)");
			e.printStackTrace();
			return;
		}
		String[] tokens = WORD_SPLIT.split(plainText.toLowerCase()); 
		Set<String> tokenSet = new HashSet<String>(Arrays.asList(tokens)); //remove repeats (only want number of documents)

		//Loop through and output
		for (String token : tokenSet) {
			if (keywords.contains(token)) {
				out.collect(new Tuple2<String,Integer>(token, 1));
			}
		}
	}
}
