package de.tuberlin.dima.schubotz.common.utils;

import java.io.*;
import java.util.Scanner;

import de.tuberlin.dima.schubotz.wiki.WikiProgram;
import de.tuberlin.dima.schubotz.wiki.types.WikiTuple;
import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.api.java.io.CsvInputFormat;
import eu.stratosphere.api.java.io.CsvReader;
import eu.stratosphere.api.java.operators.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.HashMultiset;

/**
 * Helper for preprocessed generated CSV files
 */
public class CSVHelper {
	public static Log LOG = LogFactory.getLog(CSVHelper.class);
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
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Bad format in CSV!");
                        LOG.warn("Line: " + line);
                    }
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
