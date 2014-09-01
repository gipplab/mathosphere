package de.tuberlin.dima.schubotz.fse.mappers.preprocess;

import de.tuberlin.dima.schubotz.fse.types.DataTuple;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.util.Collector;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Counts occurrence of token in specified field per tuple.
 */
public class FieldCountPreprocess extends FlatMapFunction<DataTuple, Tuple2<String, Integer>> {
    private final String STR_SPLIT;
    private final Pattern WORD_SPLIT;
    private final int ordinal;
    private final Collection<String> queryTokens = new HashSet<>();

    public FieldCountPreprocess(int ordinal, String STR_SPLIT, Pattern WORD_SPLIT) {
        this.ordinal = ordinal;
        this.STR_SPLIT = STR_SPLIT;
        this.WORD_SPLIT = WORD_SPLIT;
    }

    @Override
	public void open(Configuration parameters) throws Exception {
        super.open(parameters);
		//Get keywords from queries
		final Collection<DataTuple> queries = getRuntimeContext().getBroadcastVariable( "Queries" );
		for (final DataTuple query : queries) {
            final String[] tokens = ((String) query.getField(ordinal)).split(STR_SPLIT); //get list of keywords
            for (final String token : tokens) {
                if (!token.isEmpty()) {
                    queryTokens.add(token);
                }
            }
        }
	}

    @Override
    public void flatMap(DataTuple in, Collector<Tuple2<String, Integer>> out) {
        final String[] docTokens = WORD_SPLIT.split(in.getNamedField(DataTuple.fields.keywords).toLowerCase());
        //remove repeats (only want number of documents)
        final Iterable<String> docTokenSet = new HashSet<>(Arrays.asList(docTokens));

        for (final String token : docTokenSet) {
            if (queryTokens.contains(token)) {
                out.collect(new Tuple2<>(token, 1));
            }
        }
    }

}
