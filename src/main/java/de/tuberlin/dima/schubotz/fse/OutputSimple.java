package de.tuberlin.dima.schubotz.fse;

import java.util.Iterator;

import eu.stratosphere.api.java.functions.GroupReduceFunction;
import eu.stratosphere.api.java.tuple.Tuple3;
import eu.stratosphere.util.Collector;

/**
 * Adds rank and runtag. Outputs 1000 results per query.
 * @param ResultTuple
 * @return OutputSimpleTuple
 *
 */
public class OutputSimple extends GroupReduceFunction<ResultTuple, OutputSimpleTuple> {
	@Override
	public void reduce(Iterator<ResultTuple> in, Collector<OutputSimpleTuple> out) {
		int current = 0;
		Tuple3<String,String,Integer> curTup;
		// for each element in group
		while(in.hasNext() && current < MainProgram.QUERYLIMIT) {
			curTup = in.next();
			out.collect(new OutputSimpleTuple(curTup.f0,curTup.f1,new Integer(current+1),curTup.f2));
			current++;
		}
	}
}
