package de.tuberlin.dima.schubotz.fse.mappers;

import java.util.Iterator;

import de.tuberlin.dima.schubotz.fse.types.OutputSimpleTuple;
import de.tuberlin.dima.schubotz.fse.types.ResultTuple;
import eu.stratosphere.api.java.functions.GroupReduceFunction;
import eu.stratosphere.util.Collector;

/**
 * Adds rank and runtag. Outputs 1000 results per query.
 * @param ResultTuple
 * @return OutputSimpleTuple
 *
 */
public class OutputSimple extends GroupReduceFunction<ResultTuple, OutputSimpleTuple> {
	int queryLimit;
	String runtag;
	public OutputSimple(int queryLimit, String runtag) {
		this.queryLimit = queryLimit;
		this.runtag = runtag;
	}
	
	@Override
	public void reduce(Iterator<ResultTuple> in, Collector<OutputSimpleTuple> out) {
		int current = 0;
		ResultTuple curTup;
		// for each element in group
		while(in.hasNext() && current < queryLimit) {
			curTup = in.next();
			out.collect(new OutputSimpleTuple(curTup.f0,curTup.f1,new Integer(current+1),curTup.f2,runtag));
			current++;
		}
	}
}
