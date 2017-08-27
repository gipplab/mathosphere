package com.formulasearchengine.mathosphere.mlp.contracts;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.core.fs.Path;
import org.junit.Test;
import scala.Tuple2;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by Moritz on 27.08.2017.
 */

public class JsonArrayOutputFormatTest {


    @Test
    public void allTests() throws Exception {
        final File temp;
        temp = Files.createTempDir();
        final HashMap<String, Tuple2<String, String>> testCases = new HashMap<>();
        testCases.put("empty", new Tuple2<>("", "[\n]\n"));
        testCases.put("ab", new Tuple2<>("{\"a\":\"b\"}", "[\n{\"a\":\"b\"}\n]\n"));
        testCases.put("twoElements", new Tuple2<>("\"a\"\n\"b\"", "[\n\"a\",\n\"b\"\n]\n"));
        final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        for (Map.Entry<String, Tuple2<String, String>> entry : testCases.entrySet()) {
            final String fname = temp.getAbsolutePath() + "/" + entry.getKey() + ".json";
            env.fromElements(entry.getValue()._1().split("\n")).output(new JsonArrayOutputFormat(new Path(fname)));
        }
        env.execute();
        for (Map.Entry<String, Tuple2<String, String>> entry : testCases.entrySet()) {
            final String fname = temp.getAbsolutePath() + "/" + entry.getKey() + ".json";
            final String real = FileUtils.readFileToString(new File(fname), "UTF-8");
            assertEquals(entry.getKey(), entry.getValue()._2(), real);
        }
    }
}