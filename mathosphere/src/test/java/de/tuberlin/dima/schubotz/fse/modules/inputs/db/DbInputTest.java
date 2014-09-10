package de.tuberlin.dima.schubotz.fse.modules.inputs.db;

import de.tuberlin.dima.schubotz.fse.settings.DataStorage;
import de.tuberlin.dima.schubotz.utils.CommandLineOutputFormat;
import de.tuberlin.dima.schubotz.utils.TestUtils;
import org.apache.commons.cli.Option;
import org.apache.flink.api.common.io.OutputFormat;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DbInputTest {

	@Test
	public void testGetOptionsAsIterable () throws Exception {
		DbInput dbi = new DbInput();
		Collection<Option> opt = dbi.getOptionsAsIterable();
		assertEquals(2,opt.size());
		final HashSet<String> strings = new HashSet<>();
		for ( Option option : opt ) {
			strings.add( option.getArgName() );
		}
		assertTrue( strings.contains( "password" ) );

	}

	@Test
	public void testConfigure () throws Exception {
		TestUtils.setTestPassword();
		TestUtils.setTestQueries();
		DataStorage data = new DataStorage();
		ExecutionEnvironment env = configureEE(data);
		OutputFormat out = new CommandLineOutputFormat<>();
		//data.getDatabaseTupleDataSet().output( out);
		data.getcQuerySet().output( out);
		env.execute("Mathosphere");

	}

	@Test
	public void testConfigure2 () throws Exception {
		TestUtils.setTestPassword();
		TestUtils.setTestQueries();
		DataStorage data = new DataStorage();
		ExecutionEnvironment env = configureEE(data);
		OutputFormat out = new CommandLineOutputFormat<>();
		data.getVotes().output( out);
		env.execute("Mathosphere");

	}
	public static ExecutionEnvironment configureEE (DataStorage data) throws Exception {
		ExecutionEnvironment env = ExecutionEnvironment.createLocalEnvironment();
		DbInput dbi = new DbInput();
		dbi.configure( env, data);
		return env;
	}

}