package de.tuberlin.dima.schubotz.fse;

import java.util.Iterator;

import eu.stratosphere.api.java.functions.GroupReduceFunction;
import eu.stratosphere.api.java.tuple.Tuple3;
import eu.stratosphere.api.java.tuple.Tuple6;
import eu.stratosphere.util.Collector;

public class OutputSimple extends GroupReduceFunction<Tuple3<String,String,Integer>, Tuple6<String,Integer,String,Integer,Integer,String>> {
	@Override
	public void reduce(Iterator<Tuple3<String,String,Integer>> in, Collector<Tuple6<String,Integer,String,Integer,Integer,String>> out) {
		int current = 0;
		Tuple6<String,Integer,String,Integer,Integer,String> result;
		Tuple3<String,String,Integer> curTup;
		// for each element in group
		while(in.hasNext() && current < MainProgram.QUERYLIMIT) {
			curTup = in.next();
			result = new Tuple6<String,Integer,String,Integer,Integer,String>(curTup.f0,new Integer(1),
																			  curTup.f1,new Integer(current+1),
																			  curTup.f2,MainProgram.RUNTAG_LATEX);
			out.collect(result);
			current++;
		}
	}
}
