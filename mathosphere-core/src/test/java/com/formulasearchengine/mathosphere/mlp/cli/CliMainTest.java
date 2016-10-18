package com.formulasearchengine.mathosphere.mlp.cli;

import com.google.common.base.Throwables;
import com.google.common.io.Files;

import com.formulasearchengine.mathosphere.mlp.Main;

import org.apache.commons.io.output.TeeOutputStream;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

import static org.junit.Assert.assertTrue;

public class CliMainTest {

  @Test
  public void testMlpRus() throws Exception {
    String[] args = new String[9];
    final File temp;
    temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
    args[0] = "mlp";
    args[1] = "-in";
    args[2] = resoucePath("com/formulasearchengine/mathosphere/mlp/wikirusample.xml");
    args[3] = "-out";
    args[4] = temp.getAbsolutePath();
    args[5] = "--language";
    args[6] = "ru";
    args[7] = "-pos";
    args[8] = "";
    System.out.println(temp.getAbsolutePath());
    runTest(args);
  }

  private String resoucePath(String resorseName) {
    ClassLoader classLoader = getClass().getClassLoader();
    URL resource = classLoader.getResource(resorseName);
    return decodePath(resource.getFile());
  }

  private static String decodePath(String urlEncodedPath) {
    try {
      return URLDecoder.decode(urlEncodedPath, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw Throwables.propagate(e);
    }
  }

  @Test
  public void testMlpRusPlain() throws Exception {
    String[] args = new String[11];
    final File temp;
    temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
    args[0] = "mlp";
    args[1] = "-in";
    args[2] = resoucePath("com/formulasearchengine/mathosphere/mlp/ru-sample.xml");
    args[3] = "-out";
    args[4] = temp.getAbsolutePath();
    args[5] = "--language";
    args[6] = "ru";
    args[7] = "-pos";
    args[8] = "";
    args[9] = "--tex";
    args[10] = "";
    System.out.println(temp.getAbsolutePath());
    String res = runTest(args);
    System.out.println(res);
  }

  @Test
  public void testMlpEngPlain() throws Exception {
    String[] args = new String[5];
    final File temp;
    temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
    args[0] = "mlp";
    args[1] = "-in";
    args[2] = resoucePath("com/formulasearchengine/mathosphere/mlp/sample.xml");
    args[3] = "-out";
    args[4] = temp.getAbsolutePath();
    System.out.println(temp.getAbsolutePath());
    runTest(args);
  }

  @Test
  public void testMlpEngPlainWithWikidata() throws Exception {
    final File temp;
    final String file = decodePath(getClass().getResource("../sample.xml").getFile());
    final String wikiDataList = decodePath(getClass().getResource("../text/test-map-no-dup.csv").getFile());
    temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
    String[] args = {"mlp", "-in", file, "-out", temp.getAbsolutePath(), "--tex", "-w", wikiDataList};
    System.out.println(temp.getAbsolutePath());
    String res = runTest(args);
    System.out.println(res);
  }

  public String runTest(String[] args) throws Exception {
    final PrintStream stdout = System.out;
    final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
    TeeOutputStream tee = new TeeOutputStream(stdout, myOut);
    System.setOut(new PrintStream(tee));
    final long t0 = System.nanoTime();
    Main.main(args);
    final String standardOutput = myOut.toString();
    System.setOut(stdout);
    System.out.println((System.nanoTime() - t0) / 1000000000 + "s");
    return standardOutput;
  }

  @Test
  @Ignore
  public void testExtract() throws Exception {
    String[] args = new String[3];
    args[0] = "extract";
    args[1] = "-in";
    args[2] = resoucePath("com/formulasearchengine/mathosphere/mlp/hamiltonian_esc.txt");
    final PrintStream stdout = System.out;
    final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(myOut));
    Main.main(args);
    final String standardOutput = myOut.toString();
    assertTrue(standardOutput.contains("magnetic dipole moment"));
    System.setOut(stdout);
  }

  @Test
  public void testCount() throws Exception {
    String[] args = new String[4];
    args[0] = "count";
    args[1] = "-in";
    args[2] = resoucePath("identifier.json");
    args[3] = "--ids";
    final PrintStream stdout = System.out;
    final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(myOut));
    Main.main(args);
    final String standardOutput = myOut.toString();
    assertTrue(standardOutput.contains("{\"element\":\"i\",\"count\":42}"));
    System.setOut(stdout);
    // System.out.println(standardOutput);
  }

  @Test
  public void testCountTok() throws Exception {
    String[] args = new String[3];
    args[0] = "count";
    args[1] = "-in";
    args[2] = resoucePath("tokens.json");
    final PrintStream stdout = System.out;
    final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(myOut));
    Main.main(args);
    final String standardOutput = myOut.toString();
    assertTrue(standardOutput
        .contains("{\"element\":{\"f0\":\"TEX_ONLY\",\"f1\":\"i\",\"arity\":2},\"count\":88}"));
    System.setOut(stdout);
    // System.out.println(standardOutput);
  }

  @Test
  public void testEval() throws Exception {
    final File temp;
    temp = Files.createTempDir();
    System.out.println(temp.getAbsolutePath());
    String[] args = {"eval",
        "-in", resoucePath("com/formulasearchengine/mathosphere/mlp/gold/eval_dataset.xml"),
        "-out", temp.getAbsolutePath(),
        "--queries", resoucePath("com/formulasearchengine/mathosphere/mlp/gold/gold.json"),
        "--nd", resoucePath("com/formulasearchengine/mathosphere/mlp/gold/nd.json"),
        "--tex",
        "-t", "0.8",
        "--level","2",
        "--ref", resoucePath("com/formulasearchengine/mathosphere/mlp/nd")};
    final PrintStream stdout = System.out;
    final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(myOut));
    Main.main(args);
    final String standardOutput = myOut.toString();
    System.setOut(stdout);
    //System.out.println(standardOutput);
  }


}
