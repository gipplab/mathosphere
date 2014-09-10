package de.tuberlin.dima.schubotz.fse.mappers;

import de.tuberlin.dima.schubotz.fse.settings.DataStorage;
import de.tuberlin.dima.schubotz.utils.CommandLineOutputFormat;
import de.tuberlin.dima.schubotz.utils.TestUtils;
import org.apache.flink.api.common.io.OutputFormat;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.LocalCollectionOutputFormat;
import org.apache.flink.api.java.tuple.Tuple3;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static de.tuberlin.dima.schubotz.fse.modules.inputs.db.DbInputTest.configureEE;

public class MainMapperTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testOpen() throws Exception {

    }

    @Test
    public void testFlatMap() throws Exception {

    }
	@Test
	public void testLoadQueries() throws Exception {
		TestUtils.setTestPassword();
		TestUtils.setTestQueries();
		DataStorage data = new DataStorage();
		ExecutionEnvironment env = configureEE(data);
		OutputFormat out = new CommandLineOutputFormat<>();
		final MainMapper mainMapper = new MainMapper();

		Collection<Tuple3<Integer,String,String>> queryList = new ArrayList<>(  );
		data.getcQuerySet().output(new LocalCollectionOutputFormat<>(queryList));
		mainMapper.loadQueries(queryList);
		env.execute("Mathosphere");
	}

}