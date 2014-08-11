package de.tuberlin.dima.schubotz.fse.wiki.mappers;

import com.google.common.collect.HashMultiset;
import de.tuberlin.dima.schubotz.fse.common.utils.ComparisonHelper;
import de.tuberlin.dima.schubotz.fse.common.utils.SafeLogWrapper;
import de.tuberlin.dima.schubotz.fse.types.QueryTuple;
import de.tuberlin.dima.schubotz.fse.types.ResultTuple;

import de.tuberlin.dima.schubotz.fse.types.WikiTuple;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.util.Collector;


import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Takes in {@link de.tuberlin.dima.schubotz.wiki.types.WikiTuple},
 * broadcast variable "Queries", maps each de.tuberlin.dima.schubotz.fse.wiki to each query
 * and outputs a score. 
 */
@SuppressWarnings("serial")
public class QueryWikiMatcher extends FlatMapFunction<WikiTuple, ResultTuple> {
	/**
	 * See {@link de.tuberlin.dima.schubotz.wiki.WikiProgram#STR_SPLIT}
	 */
	String STR_SPLIT;
    private Pattern STR_SPLIT_PATTERN;
	/**
	 * See {@link de.tuberlin.dima.schubotz.wiki.WikiProgram#latexWikiMultiset}
	 */
	final HashMultiset<String> latexWikiMultiset;
	/**
	 * See {@link de.tuberlin.dima.schubotz.wiki.WikiProgram#numWiki}
	 */
	int numWiki;
	/**
	 * See {@link de.tuberlin.dima.schubotz.wiki.WikiProgram#debug}
	 */
	boolean debug;
	
	/**
	 * Query tuples from broadcast variable; assigned in {@link QueryWikiMatcher#open}
	 */
	Collection<QueryTuple> queries;
	
	private static final SafeLogWrapper LOG = new SafeLogWrapper(QueryWikiMatcher.class);
	
	/**
	 * @param STR_SPLIT {@link de.tuberlin.dima.schubotz.wiki.WikiProgram#STR_SPLIT} passed in to ensure serializability
	 * @param latexWikiMultiset {@link de.tuberlin.dima.schubotz.wiki.WikiProgram#latexWikiMultiset} passed in to ensure serializability
	 * @param numWiki {@link de.tuberlin.dima.schubotz.wiki.WikiProgram#numWiki} passed in to ensure serializability
	 */
	@SuppressWarnings("hiding")
	public QueryWikiMatcher(String STR_SPLIT, HashMultiset<String> latexWikiMultiset, int numWiki) {
		this.STR_SPLIT = STR_SPLIT;
		this.latexWikiMultiset = latexWikiMultiset;
		this.numWiki = numWiki;
        STR_SPLIT_PATTERN = Pattern.compile(this.STR_SPLIT);
	}
	
	@Override
	public void open(Configuration parameters) {
		queries = getRuntimeContext().getBroadcastVariable("Queries"); 
	}
	
	/**
	 * @param in
	 * @param out
	 * @throws Exception
	 */
	@Override
	public void flatMap(WikiTuple in, Collector<ResultTuple> out) throws Exception {
		HashMultiset<String> sectionLatex = HashMultiset.create(Arrays.asList(in.getLatex().split(STR_SPLIT)));
		


        for (final QueryTuple query : queries) {
            HashMultiset<String> queryLatex;
            //double latexScore;
            LOG.debug(query);
            LOG.debug(in);
            LOG.debug(Arrays.asList(in.getLatex().split(STR_SPLIT)));
            double latexScore = 0.;

            if (sectionLatex.isEmpty()) {
            } else {
                queryLatex = HashMultiset.create(Arrays.asList(STR_SPLIT_PATTERN.split(query.getLatex())));
                latexScore = ComparisonHelper.calculateTFIDFScore(queryLatex, sectionLatex, latexWikiMultiset, numWiki);
            }
			
            if( Double.isNaN(latexScore)) {
                LOG.warn("NaN hit! Latex: " + Double.toString(latexScore));
                latexScore = 0.;
            }

            final double mmlScore = ComparisonHelper.calculateMMLScore(in.getMML(), query.getMML()) / 30.0;
            final double pmmlScore = ComparisonHelper.calculatePMMLScore(in.getPMML(), query.getPMML());
            final double finalScore = latexScore + mmlScore + pmmlScore;

            out.collect(new ResultTuple(query.getID(), in.getID(), finalScore));
		}
	}
}
