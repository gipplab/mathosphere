package de.tuberlin.dima.schubotz.fse.mappers;

import de.tuberlin.dima.schubotz.fse.common.utils.SafeLogWrapper;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.util.Collector;

import java.util.regex.Pattern;


/**
 * Cleans documents TODO find if better way of doing this in stratosphere exists
 * Required due to Stratosphere split on {@link de.tuberlin.dima.schubotz.fse.MainProgram#DOCUMENT_SEPARATOR}
 */
public class DocCleaner extends FlatMapFunction<String, String> {
    private static final SafeLogWrapper LOG = new SafeLogWrapper(DocCleaner.class);
    private static final Pattern tagSplit = Pattern.compile("\\n");

    @Override
	public void flatMap(String in, Collector<String> out) {
		final String[] lines = tagSplit.split(in.trim(), 2);
		if (lines.length < 2) {
			LOG.warn("Null document: " + in);
			return;
		}
		out.collect(in);
	}
}
