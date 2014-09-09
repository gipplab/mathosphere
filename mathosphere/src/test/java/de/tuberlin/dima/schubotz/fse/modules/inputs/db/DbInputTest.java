package de.tuberlin.dima.schubotz.fse.modules.inputs.db;

import de.tuberlin.dima.schubotz.fse.settings.DataStorage;
import de.tuberlin.dima.schubotz.fse.settings.SettingNames;
import de.tuberlin.dima.schubotz.fse.settings.Settings;
import de.tuberlin.dima.schubotz.utils.CommandLineOutputFormat;
import de.tuberlin.dima.schubotz.utils.TestUtils;
import org.apache.commons.cli.Option;
import org.apache.flink.api.common.io.OutputFormat;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class DbInputTest {

	@Test
	public void testGetOptionsAsIterable () throws Exception {
		DbInput dbi = new DbInput();
		Collection<Option> opt = dbi.getOptionsAsIterable();
		assertEquals(1,opt.size());
		for ( Option option : opt ) {
			//This is not an actual loop
			assertEquals( option.getArgName(), ("password") );
		}

	}

	@Test
	public void testConfigure () throws Exception {
		ExecutionEnvironment env = ExecutionEnvironment.createLocalEnvironment();
		DbInput dbi = new DbInput();
		DataStorage data = new DataStorage();
        String TestPassword = TestUtils.getFileContents("testpassword");
		Settings.setProperty( SettingNames.PASSWORD, TestPassword   );
		dbi.configure( env, data);


        OutputFormat out = new CommandLineOutputFormat<>();
        data.getDatabaseTupleDataSet().output( out);
		env.execute("Mathosphere");

	}

}