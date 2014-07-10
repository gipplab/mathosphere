package de.tuberlin.dima.schubotz.fse.mappers;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.HashMultiset;

import de.tuberlin.dima.schubotz.fse.types.QueryTuple;
import de.tuberlin.dima.schubotz.fse.types.ResultTuple;
import de.tuberlin.dima.schubotz.fse.types.SectionTuple;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.util.Collector;

public class QuerySectionMatcher extends FlatMapFunction<SectionTuple,ResultTuple> {
	/**
	 * Split for tex and keywords
	 */
	final String STR_SPLIT;
	final HashMultiset<String> latexDocsMultiset;
	final HashMultiset<String> keywordDocsMultiset;
	final Integer numDocs;
	double latexScore = 0;
	double keywordScore = 0;
	double finalScore =0.;
	private static Log LOG = LogFactory.getLog(QuerySectionMatcher.class);
	private boolean debug;
	
	public QuerySectionMatcher (String STR_SPLIT, HashMultiset<String> latexDocsMultiset, HashMultiset<String> keywordDocsMultiset, Integer numDocs, boolean debug) {
		this.STR_SPLIT = STR_SPLIT;
		this.latexDocsMultiset = latexDocsMultiset;
		this.keywordDocsMultiset = keywordDocsMultiset;
		this.numDocs = numDocs;
		this.debug = debug;
	}

	/**
	 * The core method of the MapFunction. Takes an element from the input data set and transforms
	 * it into another element.
	 *
	 * @param in The input value.
	 * @return The value produced by the map function from the input value. 
	 * @throws Exception This method may throw exceptions. Throwing an exception will cause the operation
	 *                   to fail and may trigger recovery.
	 */
	@Override
	public void flatMap(SectionTuple in,Collector<ResultTuple> out) {
		HashMultiset<String> queryLatex;
		HashMultiset<String> queryKeywords;

		
		//Construct set of term frequencies for latex and keywords
		HashMultiset<String> sectionLatex = HashMultiset.create(Arrays.asList(in.getLatex().split(STR_SPLIT)));
		HashMultiset<String> sectionKeywords = HashMultiset.create(Arrays.asList(in.getKeywords().split(STR_SPLIT)));
		
		//Loop through queries and calculate tfidf scores
		Collection<QueryTuple> queries = getRuntimeContext().getBroadcastVariable("Queries");
		for (QueryTuple query : queries) {
			if (in.getID().contains("5478_1_6") && query.getID().contains("Math-1")) { //DEBUG changer
				debug = true;
			} else {
				debug = false;
			}
			if (debug) {  
				LOG.info(query.toString());
				LOG.info(in.toString());
				LOG.info(Arrays.asList(in.getLatex().split(STR_SPLIT)));
			}
			if (!sectionLatex.isEmpty()) {
				queryLatex = HashMultiset.create(Arrays.asList(query.getLatex().split(STR_SPLIT)));
				latexScore = calculateTFIDFScore(queryLatex, sectionLatex, latexDocsMultiset);
			} else {
				latexScore = 0.;
			}
			
			if (!sectionKeywords.isEmpty()) {
				queryKeywords = HashMultiset.create(Arrays.asList(query.getKeywords().split(STR_SPLIT)));
				keywordScore = calculateTFIDFScore(queryKeywords, sectionKeywords, keywordDocsMultiset);
			} else {
				keywordScore = 0.;
			}
			finalScore = (keywordScore/6.36) + latexScore; //TODO why is keywordScore and/or latexScore producing NaN?

			if( Double.isNaN( finalScore )  ) { 
				LOG.warn("NaN hit! Latex: " + Double.toString(latexScore) + " Keyword: " + Double.toString(keywordScore)); 
				finalScore = 0;
			}
			out.collect( new ResultTuple( query.getID(), in.getID(), finalScore ) );
		}
	}
	
	/**
	 * @param queryTokens
	 * @param sectionTokens
	 * @param map - map to use: either keywordDocsMap or latexDocsMap
	 * @return
	 */
	private double calculateTFIDFScore(HashMultiset<String> queryTokens, HashMultiset<String> sectionTokens, HashMultiset<String> map) {
		/*
		 * NaN possibilities:
		 * -1) The total number of terms in the document is zero (tf = x/0)- 
		 * -2) The total number of documents that contains the term is -1-
		 * -3) The total number of documents is <= 0-
		 * -4) numDocs is so high that it is NaN- 
		 * -5) count(element) is returning NaN or <= -1-
		 * 6) size() is returning NaN or zero 
		 */
		double termTotal = sectionTokens.size(); //total number of terms in current section
		double termFreqDoc; //frequency in current section
		double termFreqTotal; //number of documents that contain the term
		
		//Calculations based on http://tfidf.com/
		double tf = 0d; //term frequency
		double idf = 0d; //inverse document frequency
		double total = 0d;
				
		for (String element : queryTokens.elementSet()) { //strips duplicates in query due to multiple formulas
			termFreqDoc = sectionTokens.count(element);
			termFreqTotal = map.count(element);
			tf = termFreqDoc / termTotal; //can be zero but not undefined
			idf = Math.log(((double) numDocs) / (1d + termFreqTotal)); //will never be undefined due to +1
			total += tf * idf;
			if (debug) {
				LOG.info("Term: " + element);
				LOG.info("Freq in Doc: " + termFreqDoc);
				LOG.info("Num doc with term: " + termFreqTotal);
				LOG.info("tf: " + tf);
				LOG.info("idf: " + idf);
				LOG.info("total: " + total);
			}
		}
		if (debug) {
			LOG.info("end total: " + total);
			LOG.info("END END END END");
		}
		return total;
		
	}
}
