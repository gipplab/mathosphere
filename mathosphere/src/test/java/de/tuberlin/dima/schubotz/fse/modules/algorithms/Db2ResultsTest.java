package de.tuberlin.dima.schubotz.fse.modules.algorithms;

import de.tuberlin.dima.schubotz.fse.settings.DataStorage;
import de.tuberlin.dima.schubotz.fse.types.DatabaseResultTuple;
import de.tuberlin.dima.schubotz.utils.TestUtils;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.jdbc.JDBCOutputFormat;
import org.apache.flink.configuration.Configuration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static de.tuberlin.dima.schubotz.fse.modules.inputs.db.DbInputTest.configureEE;

public class Db2ResultsTest {

	@Before
	public void setUp () throws Exception {

	}

	@Test
	@Ignore
	public void testOutput () throws Exception{
		Db2Results dbw = new Db2Results();
		DatabaseResultTuple r = new DatabaseResultTuple();
		r.setNamedField( DatabaseResultTuple.fields.queryNum, 1 );
		r.setNamedField( DatabaseResultTuple.fields.queryFormulaID, "test" );
		r.setNamedField( DatabaseResultTuple.fields.fId, 1 );
		r.setNamedFieldB( DatabaseResultTuple.fields.cdMatch, null );
		r.setNamedFieldB( DatabaseResultTuple.fields.dataMatch, null );
		r.setNamedField( DatabaseResultTuple.fields.queryCoverage, 1.3 );
		r.setNamedField( DatabaseResultTuple.fields.matchDepth, 4 );
		r.setNamedField( DatabaseResultTuple.fields.isFormulae, false );
		r.setNamedField( DatabaseResultTuple.fields.vote, 2 );
		TestUtils.setTestPassword();
		JDBCOutputFormat out = dbw.getOutput();
		out.configure( new Configuration(  ) );
		out.open( 1,1 );
		out.writeRecord( r );
		out.close();
	}
/*	@Test
    @Ignore
	public void testOutput2 () throws Exception{
		Db2Results dbw = new Db2Results();
		MainMapper.Query q = (new MainMapper()).getQuery( 1,"s" );

		DatabaseResultTuple r= MainMapper.makeRecord( q, 3,1,null,null,null,null,null );
		TestUtils.setTestPassword();
		JDBCOutputFormat out = dbw.getOutput();
		out.configure( new Configuration(  ) );
		out.open( 1,1 );
		out.writeRecord( r );
		out.close();
	}*/
	@Test
	public void testConfigure () throws Exception {
		TestUtils.setTestPassword();
		TestUtils.setTestQueries();
		DataStorage data = new DataStorage();
		ExecutionEnvironment env = configureEE(data);
		Db2Results dbw = new Db2Results();
		dbw.configure( env,data );
		//OutputFormat out = new CommandLineOutputFormat<>();
		//data.getResultSet().output( out);
		env.execute("Mathosphere");
	}


}