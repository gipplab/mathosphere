package de.tuberlin.dima.schubotz.common.utils;

import com.google.common.collect.HashMultiset;
import de.tuberlin.dima.schubotz.wiki.WikiProgram;
import de.tuberlin.dima.schubotz.wiki.types.WikiTuple;
import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.api.java.io.CsvReader;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Helper for preprocessed generated CSV files
 */
public class CSVHelper {
    private static final SafeLogWrapper LOG = new SafeLogWrapper(CSVHelper.class);
    public static final String CSV_LINE_SEPARATOR = WikiProgram.CSV_LINE_SEPARATOR;
    public static final char CSV_FIELD_SEPARATOR = WikiProgram.CSV_FIELD_SEPARATOR.charAt(0);
	/**
	 * @param in path + name of file to read from
	 * @return HashMultiset, where index is first CSV field, count is second CSV field
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static HashMultiset<String> csvToMultiset(String in) throws FileNotFoundException, IOException {
		HashMultiset<String> out = HashMultiset.create();
        final InputStream is = new FileInputStream(in);
        try {
            final Scanner s = new Scanner(is, "UTF-8");
            s.useDelimiter("\\A");
            //TODO more hacks, make this more efficient?
            String[] lines = s.next().split(CSV_LINE_SEPARATOR);
            for (String line : lines) {
                String[] parts = line.split(String.valueOf(CSV_FIELD_SEPARATOR));
                try {
                    out.add(parts[0], Integer.valueOf(parts[1]).intValue());
                } catch (ArrayIndexOutOfBoundsException e) {
                    LOG.warn("Bad format in CSV!");
                    LOG.warn("Line: " + line);
                    continue;
                }
            }
        } finally {
            is.close();
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
