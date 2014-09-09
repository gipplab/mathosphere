package de.tuberlin.dima.schubotz.utils;

import org.apache.flink.api.common.io.OutputFormat;
import org.apache.flink.configuration.Configuration;

import java.io.IOException;

/**
 * Created by Moritz on 09.09.2014.
 */
public class CommandLineOutputFormat implements OutputFormat {
	/**
	 * Configures this output format. Since output formats are instantiated generically and hence parameterless,
	 * this method is the place where the output formats set their basic fields based on configuration values.
	 * <p/>
	 * This method is always called first on a newly instantiated output format.
	 *
	 * @param parameters The configuration with all parameters.
	 */
	@Override
	public void configure (Configuration parameters) {

	}

	/**
	 * Opens a parallel instance of the output format to store the result of its parallel instance.
	 * <p/>
	 * When this method is called, the output format it guaranteed to be configured.
	 *
	 * @param taskNumber The number of the parallel instance.
	 * @param numTasks   The number of parallel tasks.
	 * @throws java.io.IOException Thrown, if the output could not be opened due to an I/O problem.
	 */
	@Override
	public void open (int taskNumber, int numTasks) throws IOException {

	}

	/**
	 * Adds a record to the output.
	 * <p/>
	 * When this method is called, the output format it guaranteed to be opened.
	 *
	 * @param record The records to add to the output.
	 * @throws java.io.IOException Thrown, if the records could not be added to to an I/O problem.
	 */
	@Override
	public void writeRecord (Object record) throws IOException {
		System.out.println("got a record");
	}

	/**
	 * Method that marks the end of the life-cycle of parallel output instance. Should be used to close
	 * channels and streams and release resources.
	 * After this method returns without an error, the output is assumed to be correct.
	 * <p/>
	 * When this method is called, the output format it guaranteed to be opened.
	 *
	 * @throws java.io.IOException Thrown, if the input could not be closed properly.
	 */
	@Override
	public void close () throws IOException {

	}
}
