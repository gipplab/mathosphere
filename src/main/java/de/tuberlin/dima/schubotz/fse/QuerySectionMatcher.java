package de.tuberlin.dima.schubotz.fse;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

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
		HashSet<String> queryLatex;
		HashSet<String> queryKeywords;
		HashSet<String> a;
		HashSet<String> b;
		int latexHits = 0;
		int keywordHits = 0;
		//Takes in sectionID, latex string tokenized split by <S>, outputs QueryID,DocID,NumlatexHits
		HashSet<String> sectionLatex = new HashSet<String>(Arrays.asList(in.getLatex().split(SPLIT))); 
		HashSet<String> sectionKeywords = new HashSet<String>(Arrays.asList(in.getKeywords().split(SPLIT))); 
		Collection<QueryTuple> queries = getRuntimeContext().getBroadcastVariable("Queries");
		boolean sectionLarger;
		//Loop through queries
		for (QueryTuple query : queries) {
			queryLatex = new HashSet<String>(Arrays.asList(query.getLatex().split(SPLIT))); 
			
			//Latex token match
			sectionLarger = (sectionLatex.size() > queryLatex.size()) ? true : false;
			//loop through smaller set
			if (sectionLarger) {
				a = queryLatex;
				b = sectionLatex;
			}else {
				b = queryLatex;
				a = sectionLatex;
			}
			for (String e : a) {
				if (b.contains(e)) {
					latexHits++;
				}
			}
			
			//Keyword match
			queryKeywords = new HashSet<String>(Arrays.asList(query.getKeywords().split(SPLIT))); 
			
			sectionLarger = (sectionKeywords.size() > queryKeywords.size()) ? true : false;
			//loop through smaller set
			if (sectionLarger) {
				a = queryKeywords;
				b = sectionKeywords;
			}else {
				b = queryKeywords;
				a = sectionKeywords;
			}
			for (String e : a) {
				if (b.contains(e)) {
					keywordHits++;
				}
			}
			
			int score = calculateScore(latexHits, keywordHits);
			out.collect(new ResultTuple(query.getID(),in.getID(),score));
			latexHits = 0;
			keywordHits = 0;
		}
	}
	/**
	 * @param latexHits
	 * @param keywordHits
	 * @return
	 */
	private int calculateScore(int latexHits, int keywordHits) {
		return keywordHits*KEYWORDSCORE + latexHits*LATEXSCORE;
	}
}
