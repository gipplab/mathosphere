package de.tuberlin.dima.schubotz.fse;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.api.java.tuple.Tuple3;
import eu.stratosphere.util.Collector;

public class QuerySectionMatcher extends FlatMapFunction<SectionTuple,ResultTuple> {
	//Splitter for tex
	String TEX_SPLIT = MainProgram.STR_SPLIT;
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
		HashSet<String> a;
		HashSet<String> b;
		Integer hits = new Integer(0);
		//Takes in sectionID, latex string tokenized split by <S>, outputs QueryID,DocID,Numhits
		HashSet<String> sectionLatex = new HashSet<String>(Arrays.asList(in.getLatex().split(TEX_SPLIT))); //split into set
		Collection<QueryTuple> queries = getRuntimeContext().getBroadcastVariable("Queries");
		boolean sectionLarger;
		//Loop through queries
		for (QueryTuple query : queries) {
			HashSet<String> queryLatex = new HashSet<String>(Arrays.asList(query.getLatex().split(TEX_SPLIT))); //split into set
				
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
					hits++;
				}
			}
			out.collect(new ResultTuple(query.getID(),in.getID(),hits));
			hits = 0;
		}
	}

}
