package de.tuberlin.dima.schubotz.fse.modules.inputs.db;

import de.tuberlin.dima.schubotz.fse.settings.DataStorage;
import de.tuberlin.dima.schubotz.fse.settings.SettingNames;
import de.tuberlin.dima.schubotz.fse.settings.Settings;
import de.tuberlin.dima.schubotz.utils.CommandLineOutputFormat;
import org.apache.commons.cli.Option;
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
		Settings.setProperty( SettingNames.PASSWORD, "txZf4TRat4eyDyyU" );
		dbi.configure( env, data);


		data.getDataSet().output( new CommandLineOutputFormat() );
		env.execute("Mathosphere");
//		final boolean parsed = ClientConsole.parseParameters( args );
//
//		if (parsed) {
//			configureEnv();
//
//			final DataStorage data = new DataStorage();
//
//			//Run input module specified by command line
//			final Input inputModule = getModule(
//				Settings.getProperty( SettingNames.INPUT ), Input.class);
//			inputModule.configure(env, data);
//
//			//Run algorith module specified by command line
//			final Algorithm algoModule = getModule(
//				Settings.getProperty(SettingNames.ALGORITHM), Algorithm.class);
//			algoModule.configure(env, data);
//
//            /* Trust user to run input module for now
//            //Run input modules required by algorithm
//            for (final Class clazz : algorithm.getRequiredInputsAsIterable()) {
//                final Module addInputObj = (Module) getObjectFromGenericClass(clazz, Input.class);
//                addInputObj.configure(env, data);
//            }*/
//
//
//            /* Algorithms are tied to output (preprocess, etc.)
//            final Class outputClass = getClass(
//                    Settings.getProperty(SettingNames.OUTPUT_OPTION),Output.class.getPackage().getName());
//            final Module outputObj = (Module) getObjectFromGenericClass(outputClass, Output.class);
//            outputObj.configure(env, data);
//            */
//
//			//Plan plan = env.createProgramPlan();
//			//LocalExecutor.execute(plan);
//			env.execute("Mathosphere");
	}

}