package de.tuberlin.dima.schubotz.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.HashMultiset;

/**
 * Helper for preprocessed generated CSV files
 */
public class CSVMultisetHelper {
	public static Log LOG = LogFactory.getLog(CSVMultisetHelper.class);
	/**
	 * @param in path + name of file to read from
	 * @return HashMultiset of csv field 0 : count csv field 1
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static HashMultiset<String> csvToMultiset(String in) throws FileNotFoundException, IOException {
		HashMultiset<String> out = HashMultiset.create();
		BufferedReader br = new BufferedReader(new FileReader(in));
        String line = "";
        while ((line = br.readLine()) != null) {
        	String parts[] = line.split(" ");
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
}
