package de.tuberlin.dima.schubotz.wiki;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import de.tuberlin.dima.schubotz.wiki.mappers.WikiQueryCleaner;
import de.tuberlin.dima.schubotz.wiki.mappers.WikiQueryMapper;
import de.tuberlin.dima.schubotz.wiki.types.WikiQueryTuple;
import eu.stratosphere.api.common.Plan;
import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.api.java.io.TextInputFormat;
import eu.stratosphere.api.java.operators.DataSource;
import eu.stratosphere.api.java.typeutils.BasicTypeInfo;
import eu.stratosphere.client.LocalExecutor;
import eu.stratosphere.core.fs.Path;
import eu.stratosphere.core.fs.FileSystem.WriteMode;

public class WikiQueryIT {
	static ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

	@Test
	public void test() throws IOException {
		String wikiQueryInput = getClass().getClassLoader().getResources("wikiQuery.xml").nextElement().getPath();
		String STR_SPLIT = WikiProgram.STR_SPLIT;
		Log LOG = LogFactory.getLog(WikiQueryIT.class);
		
		TextInputFormat formatQuery = new TextInputFormat(new Path(wikiQueryInput));
		
		//this will leave a System.getProperty("line.separator")</topics> at the end as well as header info at the begin
		formatQuery.setDelimiter(WikiProgram.QUERY_SEPARATOR);
		
		//Run mapper on query, check that it extracts properly for first query
		DataSet<String> cleanWikiQueryText = new DataSource<String>(env,formatQuery,BasicTypeInfo.STRING_TYPE_INFO)
											 .flatMap(new WikiQueryCleaner());
		
		DataSet<WikiQueryTuple> wikiQuerySet = cleanWikiQueryText.flatMap(new WikiQueryMapper(STR_SPLIT));
		
		File file = File.createTempFile("wikiQueryIT", "csv");
		file.deleteOnExit();
		wikiQuerySet.writeAsCsv(file.getCanonicalPath(), "\n", ",", WriteMode.OVERWRITE);
		
		try {
			Plan plan = env.createProgramPlan();
			LocalExecutor.execute(plan);
		} catch (Exception e) {
			e.printStackTrace();
			fail("WikiQueryIT Execution failed.");
		}
		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String[] nextline = reader.readLine().split(",");
		assertEquals(nextline.length != 0, true);
	}

}
