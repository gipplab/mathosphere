package de.tuberlin.dima.schubotz.fse.mappers;

import de.tuberlin.dima.schubotz.fse.types.OutputSimpleTuple;
import de.tuberlin.dima.schubotz.fse.types.ResultTuple;
import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.util.Collector;

/**
 * Adds rank and runtag. Outputs 1000 results per query.
 */
public class OutputSimple implements GroupReduceFunction<ResultTuple, OutputSimpleTuple> {
	private final int queryLimit;
	private final String runtag;
	public OutputSimple(int queryLimit, String runtag) {
		this.queryLimit = queryLimit;
		this.runtag = runtag;
	}


	/**
	 * The reduce method. The function receives one call per group of elements.
	 *
	 * @param in     All records that belong to the given input key.
	 * @param out    The collector to hand results to.
	 * @throws Exception This method may throw exceptions. Throwing an exception will cause the operation
	 *                   to fail and may trigger recovery.
	 */
	@Override
	public void reduce (Iterable<ResultTuple> in, Collector<OutputSimpleTuple> out) throws Exception {
		int current = 0;
		// for each element in group

		while(in.iterator().hasNext() && current < queryLimit) {
			final ResultTuple curTup = in.iterator().next();
			out.collect(new OutputSimpleTuple(curTup.f0,curTup.f1,current+1,curTup.f3,runtag));
			current++;
		}
	}
}
