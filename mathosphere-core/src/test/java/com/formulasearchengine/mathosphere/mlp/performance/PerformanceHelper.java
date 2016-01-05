package com.formulasearchengine.mathosphere.mlp.performance;

import com.formulasearchengine.mathosphere.mlp.Main;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.hamcrest.Matchers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Moritz on 28.12.2015.
 */
public class PerformanceHelper {

  private static File getResources(String resourceName) throws Exception {
    PerformanceHelper instance = new PerformanceHelper();
    URL url = instance.getClass().getClassLoader().getResource(resourceName);
    File dir = null;
    try {
      assert url != null;
      dir = new File(url.toURI());
    } catch (Exception e) {
      fail("Cannot open test resource folder.");
    }
    return dir;
  }

  static void runTestCollection(String resourceName, String command) throws Exception {
    runTestCollection(getResources(resourceName),command);
  }

  private static void runTestCollection(File dir,String command) throws Exception {
    //noinspection ConstantConditions
    for (File nextFile : dir.listFiles()) {
      if (nextFile.getName().endsWith("_wiki.txt")) {
        File resultPath = new File(nextFile.getAbsolutePath().replace("_wiki.txt", "_gold.csv"));
        File expectationPath = new File(nextFile.getAbsolutePath().replace("_wiki.txt", "_exp.json"));
        runTest(nextFile, resultPath, expectationPath, command);
      }
    }
  }

  private static void runTest(File source, File gold, File expectations, String command) throws Exception {
    FileReader in = new FileReader(gold);
    Iterable<CSVRecord> records = CSVFormat.RFC4180.withHeader().parse(in);
    Set<String> expected = new HashSet<>();
    for (CSVRecord record : records) {
      expected.add(record.get("identifier"));
    }
    final byte[] jsonData = Files.readAllBytes(expectations.toPath());
    JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(new String(jsonData));
    JSONObject identifiers = jsonObject.getJSONObject("identifiers");

    double expPrec = identifiers.getDouble("precision");
    double expRec = identifiers.getDouble("recall");


    String[] args = {
        command,
        "-in", source.getAbsolutePath(),
        "--tex"
    };
    final PrintStream stdout = System.out;
    final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(myOut));
    final long t0 = System.nanoTime();
    Main.main(args);
    final String standardOutput = myOut.toString();
    System.setOut(stdout);
    System.out.println((System.nanoTime() - t0) / 1000000000 + "s");
    Set<String> real = new HashSet<>(Arrays.asList(standardOutput.split(System.getProperty("line.separator"))));
    Set<String> tp = new HashSet<>(expected);
    Set<String> fn = new HashSet<>(expected);
    Set<String> fp = new HashSet<>(real);
    fn.removeAll(real);
    fp.removeAll(expected);
    tp.retainAll(real);

    double rec = ((double) tp.size()) / (tp.size() + fn.size());
    double prec = ((double) tp.size()) / (tp.size() + fp.size());


    assertThat("precision", prec, Matchers.greaterThan(expPrec));
    assertThat("recall", rec, Matchers.greaterThan(expRec));
  }


}
