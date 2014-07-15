package de.tuberlin.dima.schubotz.wiki.mappers;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.HashMultiset;

import de.tuberlin.dima.schubotz.fse.types.ResultTuple;
import de.tuberlin.dima.schubotz.utils.TFIDFHelper;
import de.tuberlin.dima.schubotz.wiki.types.WikiQueryTuple;
import de.tuberlin.dima.schubotz.wiki.types.WikiTuple;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.util.Collector;

@SuppressWarnings("serial")
public class QueryWikiMatcher extends FlatMapFunction<WikiTuple, ResultTuple> {
	String STR_SPLIT;
	final HashMultiset<String> latexWikiMultiset;
	Collection<WikiQueryTuple> queries;
	int numWiki;
	boolean debug;
	
	Log LOG = LogFactory.getLog(QueryWikiMatcher.class);
	
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
