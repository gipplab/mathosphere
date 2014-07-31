package de.tuberlin.dima.schubotz.common.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

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
		BufferedReader br = new BufferedReader(new FileReader(in));
        String line = "";
        while ((line = br.readLine()) != null) {
        	String[] parts = line.split(String.valueOf(CSV_FIELD_SEPARATOR));
        	try {
        		out.add(parts[0], Integer.valueOf(parts[1]).intValue());
        	} catch (NullPointerException e) {
        		if (LOG.isWarnEnabled()) {
        			LOG.warn("Non integer in CSV!");
        		}
        		continue;
        	}
        }
        br.close();
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
