package de.tuberlin.dima.schubotz.fse;

import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.api.java.tuple.Tuple3;
import eu.stratosphere.util.Collector;

/**
 * Created by Moritz on 25.06.2014.
 */
public class ExtractExplictDatasetMapper extends FlatMapFunction<Tuple2<SectionNameTuple, explicitDataSet>, Tuple3<String, SectionNameTuple, ResultTuple>> {
	/**
	 * The core method of the FlatMapFunction. Takes an element from the input data set and transforms
	 * it into zero, one, or more elements.
	 *
	 * @param value The input value.
	 * @param out   The collector for for emitting result values.
	 * @throws Exception This method may throw exceptions. Throwing an exception will cause the operation
	 *                   to fail and may trigger recovery.
	 */
	@Override
	public void flatMap (Tuple2<SectionNameTuple, explicitDataSet> value, Collector<Tuple3<String, SectionNameTuple, ResultTuple>> out) throws Exception {
		if ( value.f1 == null )
			return;
		for ( Object o : value.f1 ) {
			ResultTuple result = (ResultTuple) o;
			out.collect( new Tuple3<String, SectionNameTuple, ResultTuple>( result.getFor(), value.f0, result ) );
		}
		;

	}
}
