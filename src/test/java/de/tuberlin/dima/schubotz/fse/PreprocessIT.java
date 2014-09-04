package de.tuberlin.dima.schubotz.fse;

import de.tuberlin.dima.schubotz.utils.TestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
        File[] files = {testFolder.newFolder("testMain10"),
                testFolder.newFile("preprocessedData.csv"),
                testFolder.newFile("preprocessedQueries.csv"),
                testFolder.newFile("latexCounts.csv"),
                testFolder.newFile("numDocs.csv"),
                testFolder.newFile("keywordCounts.csv")};
        for (File file : files) {
            file.setWritable(true);
            if (!file.canWrite() || !file.canRead()) {
                fail("Unable to write to files, test cannot be run.");
                return;
            };
        }

        String outputDir = testFolder.getRoot().getCanonicalPath();
        outputDir = outputDir.replaceAll("\\\\","/");

        try (BufferedWriter out = new BufferedWriter(new FileWriter(outputDir + "/" + "numDocs.csv"))) {
            out.write("asdfasdf");
        } catch (Exception e) {
            fail("Could not write to numDocs: " + outputDir + " " + e.getMessage());
        }

        final String[] params = {algoModule, inputModule, "-DATARAW_FILE", rawDataFile,
                "-QUERY_FILE", rawQueryFile, "-RUNTAG", runtag,
                "-NUM_SUB_TASKS", numSubTasks, "-OUTPUT_DIR", outputDir};

        MainProgram.main(params);

        for (File file : files) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                br.readLine();
                assertTrue(br.readLine().length() > 0);
            }
        }

    }
}
