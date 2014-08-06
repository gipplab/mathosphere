package de.tuberlin.dima.schubotz.common.utils;

import com.google.common.collect.HashMultiset;
import de.tuberlin.dima.schubotz.wiki.WikiProgram;
import de.tuberlin.dima.schubotz.wiki.types.WikiTuple;
import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.api.java.io.CsvReader;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Helper for preprocessed generated CSV files
 */
public class CSVHelper {
    private static final SafeLogWrapper LOG = new SafeLogWrapper(CSVHelper.class);
    public static final String CSV_LINE_SEPARATOR = WikiProgram.CSV_LINE_SEPARATOR;
    public static final char CSV_FIELD_SEPARATOR = WikiProgram.CSV_FIELD_SEPARATOR.charAt(0);
    private static final Pattern CSV_LINE_SPLIT = Pattern.compile(CSV_LINE_SEPARATOR);
    private static final Pattern CSV_FIELD_SPLIT = Pattern.compile(String.valueOf(CSV_FIELD_SEPARATOR));
	/**
	 * @param in path + name of file to read from
	 * @return HashMultiset, where index is first CSV field, count is second CSV field
	 * @throws IOException
	 */
	public static HashMultiset<String> csvToMultiset(String in) throws IOException {
		final HashMultiset<String> out = HashMultiset.create();
        try (final InputStream is = new FileInputStream(in)) {
            final Scanner s = new Scanner(is, "UTF-8");
            s.useDelimiter("\\A");
            //TODO more hacks, make this more efficient?
            final String[] lines = CSV_LINE_SPLIT.split(s.next());
            for (final String line : lines) {
                final String[] parts = CSV_FIELD_SPLIT.split(line);
                try {
                    out.add(parts[0], Integer.valueOf(parts[1]).intValue());
                } catch (ArrayIndexOutOfBoundsException e) {
                    LOG.warn("Bad format in CSV!");
                    LOG.warn("Line: " + line);
                }
            }
        }
        return out;
	}

    /**
     * Must be run in stratosphere environment
     * @param in filename
     * @return dataset
     */
    public static DataSet<WikiTuple> csvToWikiTuple(ExecutionEnvironment env, String in) {
        CsvReader reader = env.readCsvFile(in);
        reader.fieldDelimiter(CSV_FIELD_SEPARATOR);
        reader.lineDelimiter(CSV_LINE_SEPARATOR);
        return reader.tupleType(WikiTuple.class);
    }
}
