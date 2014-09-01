package de.tuberlin.dima.schubotz.fse.utils;

import com.google.common.collect.HashMultiset;
import de.tuberlin.dima.schubotz.fse.MainProgram;
import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.api.java.io.CsvReader;
import eu.stratosphere.core.fs.FileSystem;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Helper for preprocessed generated CSV files
 */
public class CSVHelper {
    private static final SafeLogWrapper LOG = new SafeLogWrapper(CSVHelper.class);
    public static final String CSV_LINE_SEPARATOR = MainProgram.CSV_LINE_SEPARATOR;
    public static final char CSV_FIELD_SEPARATOR = MainProgram.CSV_FIELD_SEPARATOR.charAt(0);
    private static final Pattern CSV_LINE_SPLIT = Pattern.compile(CSV_LINE_SEPARATOR);
    private static final Pattern CSV_FIELD_SPLIT = Pattern.compile(String.valueOf(CSV_FIELD_SEPARATOR));

    private CSVHelper() {
    }

    /**
     * Attempts to read CSV into multiset. Throws RuntimeExceptions if resource is badly formatted or unavailable.
	 * @param in path + name of file to read from
	 * @return HashMultiset, where index is first CSV field, count is second CSV field
	 */
	public static HashMultiset<String> csvToMultiset(String in) throws IllegalArgumentException {
        if (in == null) {
            throw new IllegalArgumentException("Filename given cannot be null.");
        }
		final HashMultiset<String> out = HashMultiset.create();
        try (final InputStream is = new FileInputStream(in)) {
            final Scanner s = new Scanner(is, "UTF-8");
            s.useDelimiter("\\A");
            //TODO more hacks, make this more efficient?
            final String[] lines = CSV_LINE_SPLIT.split(s.next());
            for (final String line : lines) {
                final String[] parts = CSV_FIELD_SPLIT.split(line);
                if(parts.length < 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
                    LOG.warn("Bad format in CSV: ", in);
                    LOG.warn("Line: ", line);
                }else {
                    try {
                        out.add(parts[0], Integer.valueOf(parts[1]).intValue());
                    } catch (final NumberFormatException ignore) {
                        LOG.warn("Bad number format in CSV: ", in);
                        LOG.warn("Line: ", line);
                    }
                }
            }
        } catch (final FileNotFoundException ignore) {
            throw new IllegalArgumentException("Unable to find CSV file: " + in);
        } catch (final IOException e) {
            LOG.fatal(e.getLocalizedMessage());
            throw new IllegalArgumentException("CSV file: " + in + " is corrupted. Read log for details" +
                    " (FATAL level must be enabled)");
        }
        return out;
	}

    /**
     * Must be run in stratosphere environment. If this function
     * throws no exceptions, guaranteed to return dataset
     * of given class.
     * @param env ExecutionEnvironment
     * @param clazz tuple class to cast to
     * @param in filename
     * @return dataset
     */
    public static DataSet<?> csvToTuple(ExecutionEnvironment env, Class clazz, String in) {
        final CsvReader reader = env.readCsvFile(in);
        reader.fieldDelimiter(CSV_FIELD_SEPARATOR);
        reader.lineDelimiter(CSV_LINE_SEPARATOR);
        return reader.tupleType(clazz);
    }

    /**
     * Output CSV files from dataset.
     * @param in dataset to output
     * @param outputPath path to output
     */
    public static void outputCSV(DataSet<?> in, String outputPath) {
        in.writeAsCsv(outputPath,
                CSV_LINE_SEPARATOR, String.valueOf(CSV_FIELD_SEPARATOR), FileSystem.WriteMode.OVERWRITE);
    }
}
