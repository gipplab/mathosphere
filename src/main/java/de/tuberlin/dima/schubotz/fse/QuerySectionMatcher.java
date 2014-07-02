package de.tuberlin.dima.schubotz.fse;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import com.google.common.collect.HashMultiset;

import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.util.Collector;

public class QuerySectionMatcher extends FlatMapFunction<SectionTuple,ResultTuple> {
	/**
	 * Split for tex and keywords
	 */
	String SPLIT = MainProgram.STR_SPLIT;
	/**
	 * Score for latex
	 */
	int LATEXSCORE = MainProgram.LatexScore;
	/**
	 * Score for keywords
	 */
	int KEYWORDSCORE = MainProgram.KeywordScore;
	/**
	 * The core method of the MapFunction. Takes an element from the input data set and transforms
	 * it into another element.
	 *
	 * @param value The input value.
	 * @return The value produced by the map function from the input value. 
	 * @throws Exception This method may throw exceptions. Throwing an exception will cause the operation
	 *                   to fail and may trigger recovery.
	 */
	@Override
	public void flatMap(SectionTuple in,Collector<ResultTuple> out) {
		HashMultiset<String> queryLatex;
		HashMultiset<String> queryKeywords;
		double latexScore = 0;
		double keywordScore = 0;
		
		//Construct set of term frequencies for latex and keywords
		HashMultiset<String> sectionLatex = HashMultiset.create(Arrays.asList(in.getLatex().split(SPLIT)));
		HashMultiset<String> sectionKeywords = HashMultiset.create(Arrays.asList(in.getKeywords().split(SPLIT)));
		
		//Loop through queries and calculate tfidf scores
		Collection<QueryTuple> queries = getRuntimeContext().getBroadcastVariable("Queries");
		for (QueryTuple query : queries) {
boolean debug = false;
if (in.getID().contains("0705.0473_1_3")) {
System.out.println(query.toString());
System.out.println(in.toString());
System.out.println(Arrays.asList(in.getLatex().split(SPLIT)));
debug = true;
}
			if (!sectionLatex.isEmpty()) {
				queryLatex = HashMultiset.create(Arrays.asList(query.getLatex().split(SPLIT)));
				latexScore = calculateTFIDFScore(queryLatex, sectionLatex, MainProgram.latexDocsMultiset, debug);
			}
			
			if (!sectionKeywords.isEmpty()) {
				queryKeywords = HashMultiset.create(Arrays.asList(query.getKeywords().split(SPLIT)));
				keywordScore = calculateTFIDFScore(queryKeywords, sectionKeywords, MainProgram.keywordDocsMultiset, debug);
			}
			
			out.collect(new ResultTuple(query.getID(),in.getID(),keywordScore + latexScore));
		}
	}
	
	/**
	 * @param queryTokens
	 * @param sectionTokens
	 * @param map - map to use: either keywordDocsMap or latexDocsMap
	 * @return
	 */
	private double calculateTFIDFScore(HashMultiset<String> queryTokens, HashMultiset<String> sectionTokens, HashMultiset<String> map, boolean debug) {
		double termTotal = sectionTokens.size(); //total number of terms in current section
		double termFreqDoc; //frequency in current section
		double termFreqTotal; //number of documents that contain the term
		double docTotal = MainProgram.numDocs; 
		
		//Calculations based on http://tfidf.com/
		double tf = 0d; //term frequency
		double idf = 0d; //inverse document frequency
		double total = 0d;
				
		for (String element : queryTokens.elementSet()) {
			termFreqDoc = sectionTokens.count(element);
			termFreqTotal = map.count(element);
			tf = termFreqDoc / termTotal; //can be zero but not undefined
			idf = Math.log(docTotal / (1d + termFreqTotal)); //will never be undefined due to +1
			total += tf * idf;
if (debug) {
//DEBUG output
System.out.println("Term: " + element);
System.out.println("Freq in Doc: " + termFreqDoc);
System.out.println("Num doc with term: " + termFreqTotal);
System.out.println("tf: " + tf);
System.out.println("idf: " + idf);
System.out.println("total: " + total);
}
		}
		return total;
		
	}
}
