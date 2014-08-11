package de.tuberlin.dima.schubotz.fse.mappers;

import com.google.common.collect.HashMultiset;
import de.tuberlin.dima.schubotz.fse.common.utils.ComparisonHelper;
import de.tuberlin.dima.schubotz.fse.common.utils.SafeLogWrapper;
import de.tuberlin.dima.schubotz.fse.MainProgram;
import de.tuberlin.dima.schubotz.fse.types.QueryTuple;
import de.tuberlin.dima.schubotz.fse.types.ResultTuple;
import de.tuberlin.dima.schubotz.fse.types.SectionTuple;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.util.Collector;


import java.util.Arrays;
import java.util.Collection;

/**
 * Takes in each document, compares it to each query and maps a score in the form of a {@link de.tuberlin.dima.schubotz.fse.types.ResultTuple}
 */
@SuppressWarnings("serial")
public class QuerySectionMatcher extends FlatMapFunction<SectionTuple,ResultTuple> {
	//ARGUMENTS 
	final String STR_SPLIT;
	final HashMultiset<String> latexDocsMultiset;
	final HashMultiset<String> keywordDocsMultiset;
	final int numDocs;
	/**
	 * 	{@link MainProgram#keywordDivide} 
	 */
	double weight;
	/**
	 * QueryTuple dataset taken from broadcast variable in {@link QuerySectionMatcher#open}
	 */
	Collection<QueryTuple> queries;
	
	private static final SafeLogWrapper LOG = new SafeLogWrapper(QuerySectionMatcher.class);

	/**
	 * @param STR_SPLIT {@link MainProgram#STR_SEPARATOR} sent as parameter to ensure serializability
	 * @param latexDocsMultiset latexDocsMultiset sent as parameter to ensure serializability
	 * @param keywordDocsMultiset keywordDocsMultiset sent as parameter to ensure serializability
	 * @param numDocs numDocs sent as parameter to ensure serializability
	 * @param weight {@link MainProgram#keywordDivide} sent as parameter to ensure serializability
	 */
	@SuppressWarnings("hiding")
	public QuerySectionMatcher (String STR_SPLIT, HashMultiset<String> latexDocsMultiset,
								HashMultiset<String> keywordDocsMultiset, int numDocs,
								double weight) {
		this.STR_SPLIT = STR_SPLIT;
		this.latexDocsMultiset = latexDocsMultiset;
		this.keywordDocsMultiset = keywordDocsMultiset;
		this.numDocs = numDocs;
		this.weight = weight;
	}
	
	@Override
	public void open(Configuration parameters) {
		queries = getRuntimeContext().getBroadcastVariable("Queries"); 
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
		if (sectionLatex.isEmpty() && sectionKeywords.isEmpty()) {
			return;
		}
		
		double latexScore = 0;
		double keywordScore = 0;
		double finalScore =0.;
		//Loop through queries and calculate tfidf scores
		for (QueryTuple query : queries) {
            LOG.debug(query);
            LOG.debug(in);
            LOG.debug(Arrays.asList(in.getLatex().split(STR_SPLIT)));
			if (!sectionLatex.isEmpty()) {
				queryLatex = HashMultiset.create(Arrays.asList(query.getLatex().split(STR_SPLIT)));
				latexScore = ComparisonHelper.calculateTFIDFScore(queryLatex, sectionLatex, latexDocsMultiset, numDocs);
			} else {
				latexScore = 0.;
			}
			
			if (!sectionKeywords.isEmpty()) {
				queryKeywords = HashMultiset.create(Arrays.asList(query.getKeywords().split(STR_SPLIT)));
				keywordScore = ComparisonHelper.calculateTFIDFScore(queryKeywords, sectionKeywords,
                        keywordDocsMultiset, numDocs);
			} else {
				keywordScore = 0.;
			}
			finalScore = (keywordScore/weight) + latexScore; //TODO why is keywordScore and/or latexScore producing NaN?

			if( Double.isNaN( finalScore )) {
				LOG.warn("NaN hit! Latex: ", latexScore, " Keyword: ", keywordScore);
				finalScore = 0;
			}
			out.collect(new ResultTuple(query.getID(), in.getID(), Double.valueOf(finalScore)));
		}
	}
}
