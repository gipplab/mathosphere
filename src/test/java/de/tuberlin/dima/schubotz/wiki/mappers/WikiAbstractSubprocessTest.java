package de.tuberlin.dima.schubotz.wiki.mappers;

import de.tuberlin.dima.schubotz.wiki.WikiProgram;
import eu.stratosphere.api.common.Plan;
import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.api.java.io.TextInputFormat;
import eu.stratosphere.api.java.operators.DataSource;
import eu.stratosphere.api.java.typeutils.BasicTypeInfo;
import eu.stratosphere.client.LocalExecutor;
import eu.stratosphere.core.fs.FileSystem;
import eu.stratosphere.core.fs.Path;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Ignore;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Used as template for test classes to compare actual and expected output.
 */
@Ignore
public abstract class WikiAbstractSubprocessTest {
    protected static String STR_SPLIT = WikiProgram.STR_SPLIT;
    private static String QUERY_SEPARATOR = WikiProgram.QUERY_SEPARATOR;
    private static String WIKI_SEPARATOR = WikiProgram.WIKI_SEPARATOR;
    private static Log LOG = LogFactory.getLog(WikiAbstractSubprocessTest.class);

    private ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

    protected void testDataMap(DataSet<?> outputSet,
                               String expectedOutputFile) throws Exception {

        //File outputFile = File.createTempFile("testProcessOutput", "csv");
        //outputFile.deleteOnExit();
        File outputFile = new File("/home/jjl4/", "csv");
        outputSet.writeAsCsv(outputFile.getCanonicalPath(), "\n", ",", FileSystem.WriteMode.OVERWRITE);

        Plan plan = env.createProgramPlan();
        LocalExecutor.execute(plan);

        File expectedFile = new File(WikiAbstractSubprocessTest.class
                .getClassLoader().getResource(expectedOutputFile).getPath());
        assertTrue("Files could not be read!", expectedFile.canRead());
        assertTrue("Output does not match expected output!", FileUtils.contentEquals(outputFile, expectedFile));
    }

    protected DataSet<String> getCleanedData(String filename) throws IOException {
        String dir = WikiAbstractSubprocessTest.class.getClassLoader().getResource(filename).getPath();
        TextInputFormat format = new TextInputFormat(new Path(dir));
        FlatMapFunction<String, String> cleaner;
        if (dir.contains("Query")) {
            format.setDelimiter(QUERY_SEPARATOR);
            cleaner = new WikiQueryCleaner();
        } else { //WikiData
            format.setDelimiter(WIKI_SEPARATOR);
            cleaner = new WikiCleaner();
        }
        return new DataSource<String>(env,format, BasicTypeInfo.STRING_TYPE_INFO)
                .flatMap(cleaner);
    }
}
