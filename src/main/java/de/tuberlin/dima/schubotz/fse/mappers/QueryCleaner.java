package de.tuberlin.dima.schubotz.fse.mappers;

import de.tuberlin.dima.schubotz.common.utils.SafeLogWrapper;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.util.Collector;


/**
 * Cleans main task queries. TODO find way to do this using stratosphere built in data input?
 * Required due to Stratosphere split on {@link de.tuberlin.dima.schubotz.fse.MainProgram#QUERY_SEPARATOR}
 */
public class QueryCleaner extends FlatMapFunction<String, String> {
    private static final SafeLogWrapper LOG = new SafeLogWrapper(QueryCleaner.class);
	@Override
	public void flatMap(String in, Collector<String> out) throws Exception {
		if (in.trim().isEmpty() || in.startsWith("\r\n</topics>")) {
            LOG.warn("Corrupt query " + in);
			return;
		}
		if (in.startsWith("<?xml")) {
			in += "</topic></topics>";
		}else if (!in.endsWith( "</topic>" )) {
			in += "</topic>";
		}
		out.collect(in);
	}
}