package de.tuberlin.dima.schubotz.wiki.mappers;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.HashMultiset;

import de.tuberlin.dima.schubotz.common.utils.TFIDFHelper;
import de.tuberlin.dima.schubotz.fse.types.ResultTuple;
import de.tuberlin.dima.schubotz.wiki.types.WikiQueryTuple;
import de.tuberlin.dima.schubotz.wiki.types.WikiTuple;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.util.Collector;

/**
 * Takes in {@link de.tuberlin.dima.schubotz.wiki.types.WikiTuple},
 * broadcast variable "Queries", maps each wiki to each query
 * and outputs a score. 
 */
@SuppressWarnings("serial")
public class QueryWikiMatcher extends FlatMapFunction<WikiTuple, ResultTuple> {
	/**
	 * See {@link de.tuberlin.dima.schubotz.wiki.WikiProgram#STR_SPLIT}
	 */
	String STR_SPLIT;
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
	Collection<WikiQueryTuple> queries;
	
	Log LOG = LogFactory.getLog(QueryWikiMatcher.class);
	
	/**
	 * @param STR_SPLIT {@link de.tuberlin.dima.schubotz.wiki.WikiProgram#STR_SPLIT} passed in to ensure serializability
	 * @param latexWikiMultiset {@link de.tuberlin.dima.schubotz.wiki.WikiProgram#latexWikiMultiset} passed in to ensure serializability
	 * @param numWiki {@link de.tuberlin.dima.schubotz.wiki.WikiProgram#numWiki} passed in to ensure serializability
	 * @param debug {@link de.tuberlin.dima.schubotz.wiki.WikiProgram#debug} passed in to ensure serializability
	 */
	@SuppressWarnings("hiding")
	public QueryWikiMatcher(String STR_SPLIT, HashMultiset<String> latexWikiMultiset, int numWiki, boolean debug) {
		this.STR_SPLIT = STR_SPLIT;
		this.latexWikiMultiset = latexWikiMultiset;
		this.numWiki = numWiki;
		this.debug = debug;
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
		
		HashMultiset<String> queryLatex;
		double latexScore;
		double finalScore;
		for (WikiQueryTuple query : queries) {
			if (LOG.isDebugEnabled() && debug) {  
				LOG.debug(query.toString());
				LOG.debug(in.toString());
				LOG.debug(Arrays.asList(in.getLatex().split(STR_SPLIT)));
			}
			
			if (!sectionLatex.isEmpty()) {
				queryLatex = HashMultiset.create(Arrays.asList(query.getLatex().split(STR_SPLIT)));
				latexScore = TFIDFHelper.calculateTFIDFScore(queryLatex, sectionLatex, latexWikiMultiset, numWiki, debug);
			} else {
				latexScore = 0.;
			}
			
			 finalScore = latexScore;
			 
			 if( Double.isNaN( finalScore )) {
				 if (LOG.isWarnEnabled() ) {
					 LOG.warn("NaN hit! Latex: " + Double.toString(latexScore));
				 }
				 finalScore = 0;
			}
			out.collect(new ResultTuple(query.getID(), in.getID(), finalScore));			
			
		}

	}

}
