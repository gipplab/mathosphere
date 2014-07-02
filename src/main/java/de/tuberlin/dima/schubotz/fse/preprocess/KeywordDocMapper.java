package de.tuberlin.dima.schubotz.fse.preprocess;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;

import de.tuberlin.dima.schubotz.fse.QueryTuple;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.util.Collector;

public class KeywordDocMapper extends FlatMapFunction<String, Tuple2<String,Integer>> {
	HashSet<String> keywords;
	
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
		//Takes in document, outputs tuple of <keyword,1> for every keyword in document contained in set of query keywords
		//Extract plaintext from article
		String plainText = Jsoup.parse(value).text();
		String[] tokens = plainText.toLowerCase().split( "\\W+" ); //split on repeating non-word characters
		Set<String> tokenSet = new HashSet<String>(Arrays.asList(tokens)); //remove repeats
		
		//Loop through and output
		for (String token : tokenSet) {
			if (keywords.contains(token)) {
				out.collect(new Tuple2<String,Integer>(token, 1));
			}
		}
	}
}
