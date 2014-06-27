package de.tuberlin.dima.schubotz.fse;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.api.java.tuple.Tuple3;
import eu.stratosphere.util.Collector;

public class QuerySectionMatcher extends FlatMapFunction<Tuple2<String,String>,Tuple3<String,String,Integer>> {

	String TEX_SPLIT = MainProgram.TEX_SPLIT;
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
	public void flatMap(Tuple2<String,String> in,Collector<Tuple3<String,String,Integer>> out) {
		//System.out.println(in.f0); //DEBUG
		HashSet<String> a;
		HashSet<String> b;
		Integer hits = new Integer(0);
		//Takes in sectionID, latex string tokenized split by <S>, outputs QueryID,DocID,Numhits
		HashSet<String> sectionLatex = new HashSet<String>(Arrays.asList(in.f1.split(TEX_SPLIT))); //split into set
		Collection<Tuple2<String,String>> queries = getRuntimeContext().getBroadcastVariable("Queries");
		boolean sectionLarger;
		//Loop through queries
		for (Tuple2<String,String> query : queries) {
			HashSet<String> queryLatex = new HashSet<String>(Arrays.asList(query.f1.split(TEX_SPLIT))); //split into set
				
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
			out.collect(new Tuple3<String,String,Integer>(query.f0,in.f0,hits));
			hits = 0;
		}
	}

}
