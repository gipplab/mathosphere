package de.tuberlin.dima.schubotz.fse;

import java.util.Collection;
import java.util.regex.Matcher;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.api.java.tuple.Tuple5;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.util.Collector;

public class QueryArticleMatch extends FlatMapFunction<String,Tuple5<String,String,String,Integer,Integer>>{ 	
    @Override
    public void flatMap(String value, Collector<Tuple5<String,String,String,Integer,Integer>> out) throws Exception {
    	//inputs: html plaintext
    	//returns Tuple5<query_number, filename, keywordID, 0/1..., pos-in-text>
    	Collection<Query> queries = getRuntimeContext().getBroadcastVariable("Queries");
    	
		// normalize and split the line
		String[] tokens = value.toLowerCase().split("\\W+");
		
		
    }

}
