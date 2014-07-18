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
		assertEquals(nextline[2], 
			"<m:apply xml:id=\"m91.1.12\" xref=\"m91.1.12.pmml\">" +
            "  <m:csymbol cd=\"latexml\" xml:id=\"m91.1.3\" xref=\"m91.1.3.pmml\">maps-to</m:csymbol>" +
            "  <m:ci xml:id=\"m91.1.2\" xref=\"m91.1.2.pmml\">A</m:ci>" +
            "  <m:apply xml:id=\"m91.1.12.1\" xref=\"m91.1.12.1.pmml\">" +
            "    <m:times xml:id=\"m91.1.12.1.1\" xref=\"m91.1.12.1.1.pmml\"/>" +
            "    <m:ci xml:id=\"m91.1.4\" xref=\"m91.1.4.pmml\">M</m:ci>" +
            "    <m:ci xml:id=\"m91.1.5\" xref=\"m91.1.5.pmml\">α</m:ci>" +
            "    <m:ci xml:id=\"m91.1.7\" xref=\"m91.1.7.pmml\">A</m:ci>" +
            "    <m:apply xml:id=\"m91.1.12.1.2\" xref=\"m91.1.12.1.2.pmml\">" +
            "      <m:csymbol cd=\"ambiguous\" xml:id=\"m91.1.12.1.2.1\">superscript</m:csymbol>" +
            "      <m:ci xml:id=\"m91.1.9\" xref=\"m91.1.9.pmml\">M</m:ci>" +
            "      <m:apply xml:id=\"m91.1.10.1\" xref=\"m91.1.10.1.pmml\">" +
            "        <m:minus xml:id=\"m91.1.10.1.1\" xref=\"m91.1.10.1.1.pmml\"/>" +
            "        <m:cn type=\"integer\" xml:id=\"m91.1.10.1.2\" xref=\"m91.1.10.1.2.pmml\">1</m:cn>" +
            "      </m:apply>" +
            "    </m:apply>" +
            "  </m:apply>" +
            "</m:apply>");
		assertEquals(nextline[3],
			"<m:annotation-xml encoding=\"MathML-Presentation\" xml:id=\"m91.1.pmml\" xref=\"m91.1\">" +
        	"  <m:mrow xml:id=\"m91.1.12.pmml\" xref=\"m91.1.12\">" +
        	"    <m:mrow xml:id=\"m91.1.12a.pmml\" xref=\"m91.1.12\">" +
        	"      <m:mpadded lspace=\"1.7pt\" width=\"+1.7pt\" xml:id=\"m91.1.2.pmml\" xref=\"m91.1.2\">" +
        	"  	     <m:mi xml:id=\"m91.1.2a.pmml\" xref=\"m91.1.2\">A</m:mi>" +
        	"      </m:mpadded>" +
            "      <m:mo xml:id=\"m91.1.3.pmml\" xref=\"m91.1.3\">↦</m:mo>" +
            "      <m:mrow xml:id=\"m91.1.12.1.pmml\" xref=\"m91.1.12.1\">" +
            "        <m:mi xml:id=\"m91.1.4.pmml\" xref=\"m91.1.4\">M</m:mi>" +
            "        <m:mo xml:id=\"m91.1.12.1.1.pmml\" xref=\"m91.1.12.1.1\">⁢</m:mo>" +
            "        <m:mi xml:id=\"m91.1.5.pmml\" xref=\"m91.1.5\">α</m:mi>" +
            "        <m:mo xml:id=\"m91.1.12.1.1a.pmml\" xref=\"m91.1.12.1.1\">⁢</m:mo>" +
            "        <m:mrow xml:id=\"m91.1.7.pmml\" xref=\"m91.1.7\">" +
            "          <m:mo xml:id=\"m91.1.7a.pmml\" xref=\"m91.1.7\">(</m:mo>" +
            "          <m:mi xml:id=\"m91.1.7b.pmml\" xref=\"m91.1.7\">A</m:mi>" +
            "          <m:mo xml:id=\"m91.1.7c.pmml\" xref=\"m91.1.7\">)</m:mo>" +
            "        </m:mrow>" +
            "        <m:mo xml:id=\"m91.1.12.1.1b.pmml\" xref=\"m91.1.12.1.1\">⁢</m:mo>" +
            "        <m:msup xml:id=\"m91.1.12.1.2.pmml\" xref=\"m91.1.12.1.2\">" +
            "          <m:mi xml:id=\"m91.1.9.pmml\" xref=\"m91.1.9\">M</m:mi>" +
            "          <m:mrow xml:id=\"m91.1.10.1.pmml\" xref=\"m91.1.10.1\">" +
            "            <m:mo xml:id=\"m91.1.10.1.1.pmml\" xref=\"m91.1.10.1.1\">-</m:mo>" +
            "            <m:mn xml:id=\"m91.1.10.1.2.pmml\" xref=\"m91.1.10.1.2\">1</m:mn>" +
            "          </m:mrow>" +
            "        </m:msup>" +
            "      </m:mrow>" +
            "    </m:mrow>" +
            "    <m:mo xml:id=\"m91.1.12b.pmml\" xref=\"m91.1.12\">,</m:mo>" +
            "  </m:mrow>" +
			"</m:annotation-xml>");
			
	}

}
