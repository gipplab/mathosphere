package de.tuberlin.dima.schubotz.fse.mappers;

import com.google.common.collect.HashMultiset;
import de.tuberlin.dima.schubotz.fse.MainProgram;
import de.tuberlin.dima.schubotz.fse.types.DataTuple;
import de.tuberlin.dima.schubotz.fse.types.ResultTuple;
import de.tuberlin.dima.schubotz.fse.utils.ComparisonHelper;
import de.tuberlin.dima.schubotz.fse.utils.SafeLogWrapper;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.util.Collector;

import java.util.Arrays;
import java.util.Collection;

/**
 * Takes in each document, compares it to each query and maps a score in the form of a {@link de.tuberlin.dima.schubotz.fse.types.ResultTuple}
 */
public class QueryDocMatcher extends FlatMapFunction<DataTuple,ResultTuple> {
	//ARGUMENTS 
	private final String STR_SPLIT;
	private final HashMultiset<String> latexDocsMultiset;
	private final HashMultiset<String> keywordDocsMultiset;
	private final int numDocs;
	private double weight;
	/**
	 * QueryTuple dataset taken from broadcast variable in {@link QueryDocMatcher#open}
	 */
    private Collection<DataTuple> queries;
	
	private static final SafeLogWrapper LOG = new SafeLogWrapper(QueryDocMatcher.class);

	/**
	 * @param STR_SPLIT {@link MainProgram#STR_SEPARATOR} sent as parameter to ensure serializability
	 * @param latexDocsMultiset latexDocsMultiset sent as parameter to ensure serializability
	 * @param keywordDocsMultiset keywordDocsMultiset sent as parameter to ensure serializability
	 * @param numDocs numDocs sent as parameter to ensure serializability
	 * @param weight sent as parameter to ensure serializability
	 */
	public QueryDocMatcher(String STR_SPLIT, HashMultiset<String> latexDocsMultiset,
                           HashMultiset<String> keywordDocsMultiset, int numDocs,
                           double weight) {
		this.STR_SPLIT = STR_SPLIT;
		this.latexDocsMultiset = HashMultiset.create(latexDocsMultiset);
		this.keywordDocsMultiset = HashMultiset.create(keywordDocsMultiset);
		this.numDocs = numDocs;
		this.weight = weight;
	}
	
	@Override
	public void open(Configuration parameters) throws Exception {
        super.open(parameters);
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
	public void flatMap(DataTuple in,Collector<ResultTuple> out) {
		//Construct set of term frequencies for latex and keywords
		final HashMultiset<String> docLatex = HashMultiset.create(
                Arrays.asList(in.getNamedField(DataTuple.fields.latex).split(STR_SPLIT)));
		final HashMultiset<String> docKeywords = HashMultiset.create(
                Arrays.asList(in.getNamedField(DataTuple.fields.latex).split(STR_SPLIT)));

		if (docLatex.isEmpty() && docKeywords.isEmpty()) {
			return;
		}
		
		//Loop through queries and calculate tfidf scores
        for (final DataTuple query : queries) {
            LOG.debug(query);
            LOG.debug(in);
            LOG.debug(Arrays.asList(in.getNamedField(DataTuple.fields.latex).split(STR_SPLIT)));
            final double latexScore;
            if (docLatex.isEmpty()) {
                latexScore = 0.0;
            } else {
                final HashMultiset<String> queryLatex = HashMultiset.create(
                        Arrays.asList(query.getNamedField(DataTuple.fields.latex).split(STR_SPLIT)));
                latexScore = ComparisonHelper.calculateTFIDFScore(
                        queryLatex, docLatex, latexDocsMultiset, numDocs);
            }

            final double keywordScore;
            if (docKeywords.isEmpty()) {
                keywordScore = 0.0;
            } else {
                final HashMultiset<String> queryKeywords = HashMultiset.create(
                        Arrays.asList(query.getNamedField(DataTuple.fields.keywords).split(STR_SPLIT)));
                keywordScore = ComparisonHelper.calculateTFIDFScore(
                        queryKeywords, docKeywords, keywordDocsMultiset, numDocs);
            }
            double finalScore = (keywordScore / weight) + latexScore;

            if( Double.isNaN( finalScore )) {
				LOG.warn("NaN hit! Latex: ", latexScore, " Keyword: ", keywordScore);
				finalScore = 0.0;
			}
			out.collect(new ResultTuple(
                    query.getNamedField(DataTuple.fields.docID),
                    in.getNamedField(DataTuple.fields.docID),
                    finalScore));
		}
	}
}
