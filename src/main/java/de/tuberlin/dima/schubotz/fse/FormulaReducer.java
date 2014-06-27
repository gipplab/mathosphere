package de.tuberlin.dima.schubotz.fse;

import eu.stratosphere.api.java.functions.GroupReduceFunction;
import eu.stratosphere.api.java.tuple.Tuple7;
import eu.stratosphere.types.StringValue;
import eu.stratosphere.util.Collector;

import java.util.Iterator;

/**
 * Created by Moritz on 27.06.2014.
 */
public class FormulaReducer extends GroupReduceFunction<Tuple7<String, Integer, Integer, String, String, Double, StringValue>, String> {
	/**
	 * Core method of the reduce function. It is called one per group of elements. If the reducer
	 * is not grouped, than the entire data set is considered one group.
	 *
	 * @param values The iterator returning the group of values to be reduced.
	 * @param out    The collector to emit the returned values.
	 * @throws Exception This method may throw exceptions. Throwing an exception will cause the operation
	 *                   to fail and may trigger recovery.
	 */
	@Override
	public void reduce (Iterator<Tuple7<String, Integer, Integer, String, String, Double, StringValue>> values, Collector<String> out) {
		int rank  =1;
		if (!values.hasNext()) return;
		Tuple7<String, Integer, Integer, String, String, Double, StringValue> first = values.next();
		String result = "<result for=\"" + first.f3   +"\" runtime=\"100\">\n";
		result+=addHit( first.f6.getValue(),rank );
		while ( values.hasNext() && rank<=100){
			rank++;
			Tuple7<String, Integer, Integer, String, String, Double, StringValue> value = values.next();
			result+=addHit( value.f6.toString(),rank );
		}
		out.collect( result );
	}

	private String addHit(String hit, Integer pos){
		return hit.replace( "unique-rank-string",pos.toString() );


	}
}
