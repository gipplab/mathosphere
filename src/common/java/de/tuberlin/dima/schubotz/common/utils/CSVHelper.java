package de.tuberlin.dima.schubotz.common.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import de.tuberlin.dima.schubotz.wiki.WikiProgram;
import de.tuberlin.dima.schubotz.wiki.types.WikiTuple;
import eu.stratosphere.api.java.DataSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.HashMultiset;

/**
 * Helper for preprocessed generated CSV files
 */
public class CSVHelper {
	public static Log LOG = LogFactory.getLog(CSVHelper.class);
    public static final String CSV_LINE_SEPARATOR = WikiProgram.CSV_LINE_SEPARATOR;
    public static final String CSV_FIELD_SEPARATOR = WikiProgram.CSV_FIELD_SEPARATOR;
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
        	String[] parts = line.split(CSV_FIELD_SEPARATOR);
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

    public static DataSet<WikiTuple> csvToWikiTuple(String in) {
        return null;
    }
}
