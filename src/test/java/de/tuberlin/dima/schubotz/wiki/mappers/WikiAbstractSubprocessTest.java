package de.tuberlin.dima.schubotz.wiki.mappers;

import de.tuberlin.dima.schubotz.wiki.WikiProgram;
import de.tuberlin.dima.schubotz.wiki.types.WikiQueryTuple;
import de.tuberlin.dima.schubotz.wiki.types.WikiTuple;
import eu.stratosphere.api.common.Plan;
import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.api.java.io.CsvReader;
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
    private static String CSV_LINE_SEPARATOR = WikiProgram.CSV_LINE_SEPARATOR;
    private static String CSV_FIELD_SEPARATOR = WikiProgram.CSV_FIELD_SEPARATOR;
    private static Log LOG = LogFactory.getLog(WikiAbstractSubprocessTest.class);

    private ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

    protected void testDataMap(DataSet<?> outputSet,
                               String expectedOutputFile) throws Exception {

        File outputFile = File.createTempFile(this.getClass().getSimpleName(), "csv");
        outputFile.deleteOnExit();
        //File outputFile = new File("/home/jjl4/", "csv");

        outputSet.writeAsCsv(outputFile.getCanonicalPath(), CSV_LINE_SEPARATOR,
                CSV_FIELD_SEPARATOR, FileSystem.WriteMode.OVERWRITE);

        Plan plan = env.createProgramPlan();
        LocalExecutor.execute(plan);

        File expectedFile = new File(WikiAbstractSubprocessTest.class
                .getClassLoader().getResource(expectedOutputFile).getPath());
        assertTrue("Files could not be read!", expectedFile.canRead());
        assertTrue("Output does not match expected output!", FileUtils.contentEquals(outputFile, expectedFile));
    }

    protected DataSet<?> getCleanedData(String filename) throws Exception {
        String dir = null;
        try {
            dir = WikiAbstractSubprocessTest.class.getClassLoader().getResource(filename).getPath();
        } catch (NullPointerException e) {
            //Try again with absolute path
            dir = new File(filename).getPath();
        }
        TextInputFormat format = new TextInputFormat(new Path(dir));
        if (dir.contains("expected")) { //Process as csv with tuples
            CsvReader reader = env.readCsvFile(dir);
            reader = reader.lineDelimiter(CSV_LINE_SEPARATOR);
            if (dir.contains("Query")) {
                return reader.tupleType(WikiQueryTuple.class);
            } else {
                return reader.tupleType(WikiTuple.class);
            }
        } else { //Process as normal data
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
}
