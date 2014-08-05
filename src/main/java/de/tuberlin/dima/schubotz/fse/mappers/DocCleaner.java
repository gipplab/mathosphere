package de.tuberlin.dima.schubotz.fse.mappers;

import de.tuberlin.dima.schubotz.common.utils.SafeLogWrapper;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.util.Collector;


/**
 * Cleans documents TODO find if better way of doing this in stratosphere exists
 * Required due to Stratosphere split on {@link de.tuberlin.dima.schubotz.fse.MainProgram#DOCUMENT_SEPARATOR}
 */
public class DocCleaner extends FlatMapFunction<String, String> {
    private static final SafeLogWrapper LOG = new SafeLogWrapper(DocCleaner.class);

	public void flatMap(String in, Collector<String> out) {
		String[] lines = in.trim().split( "\\n", 2 );
		if (lines.length < 2) {
			LOG.warn("Null document: " + in);
			return;
		}
		out.collect(in);
	}
}
