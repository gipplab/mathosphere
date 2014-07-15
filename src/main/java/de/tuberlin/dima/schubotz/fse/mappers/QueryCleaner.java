package de.tuberlin.dima.schubotz.fse.mappers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.util.Collector;

/**
 * Cleans main task queries. TODO find way to do this using stratosphere built in data input?
 * Required due to Stratosphere split on {@link de.tuberlin.dima.schubotz.fse.MainProgram#QUERY_SEPARATOR}
 */
public class QueryCleaner extends FlatMapFunction<String, String> {
	Log LOG = LogFactory.getLog(QueryCleaner.class);
	
	@Override
	public void flatMap(String in, Collector<String> out) throws Exception {
		if (in.trim().length() == 0 || in.startsWith("\r\n</topics>")) {
			if (LOG.isWarnEnabled()) {
				LOG.warn("Corrupt query " + in); 
			}
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