package de.tuberlin.dima.schubotz.fse;

import de.tuberlin.dima.schubotz.utils.TestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class PreprocessIT {
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    @Test
    public void TestLocalExecution() throws Exception {
        final String inputModule = "ArxivRawInput";
        final String algoModule = "RawToPreprocessed";
        final String rawDataFile = "file://" + getClass().getClassLoader().getResources(
                TestUtils.getTestFile10StringPath()).nextElement().getPath();
        final String rawQueryFile = "file://" + getClass().getClassLoader().getResources(
                TestUtils.getFQueryStringPath()).nextElement().getPath();
        final String runtag = "mainTest10";
        final String numSubTasks = "1";
        final File outputFileFolder = testFolder.newFolder("testMain10");
        final String outputDir = "file://" + outputFileFolder.getCanonicalPath();

        final String[] params = {algoModule, inputModule, "-DATARAW_FILE", rawDataFile,
                "-QUERY_FILE", rawQueryFile, "-RUNTAG", runtag,
                "-NUM_SUB_TASKS", numSubTasks, "-OUTPUT_DIR", outputDir};

        MainProgram.main(params);

        assertEquals(2, (long) outputFileFolder.listFiles().length);

    }
}
