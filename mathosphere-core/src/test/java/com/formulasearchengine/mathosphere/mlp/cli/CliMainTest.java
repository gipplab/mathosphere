package com.formulasearchengine.mathosphere.mlp.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formulasearchengine.mathosphere.mlp.ml.WekaLearner;
import com.formulasearchengine.mathosphere.mlp.pojos.IdentifierDefinition;
import com.formulasearchengine.mathosphere.mlp.pojos.StrippedWikiDocumentOutput;
import com.google.common.base.Throwables;
import com.google.common.io.Files;

import com.formulasearchengine.mathosphere.mlp.Main;

import org.apache.commons.io.output.TeeOutputStream;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;

public class CliMainTest {

  @Test
  public void testMlpRus() throws Exception {
    String[] args = new String[9];
    final File temp;
    temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
    args[0] = "mlp";
    args[1] = "-in";
    args[2] = resourcePath("com/formulasearchengine/mathosphere/mlp/wikirusample.xml");
    args[3] = "-out";
    args[4] = temp.getAbsolutePath();
    args[5] = "--language";
    args[6] = "ru";
    args[7] = "-pos";
    args[8] = "";
    System.out.println(temp.getAbsolutePath());
    runTest(args);
  }

  private String resourcePath(String resourceName) {
    ClassLoader classLoader = getClass().getClassLoader();
    URL resource = classLoader.getResource(resourceName);
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
    args[2] = resourcePath("com/formulasearchengine/mathosphere/mlp/ru-sample.xml");
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
  public void testHelp() throws Exception {
    String[] args = new String[1];
    args[0] = "help";
    runTest(args);
  }

  @Test
  public void testMlpEngPlain() throws Exception {
    String[] args = new String[5];
    final File temp;
    temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
    args[0] = "mlp";
    args[1] = "-in";
    args[2] = resourcePath("com/formulasearchengine/mathosphere/mlp/sample.xml");
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
    args[2] = resourcePath("com/formulasearchengine/mathosphere/mlp/hamiltonian_esc.txt");
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
    args[2] = resourcePath("identifier.json");
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
    args[2] = resourcePath("tokens.json");
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
      "-in", resourcePath("com/formulasearchengine/mathosphere/mlp/gold/eval_dataset_sample.xml"),
      "-out", temp.getAbsolutePath(),
      "--queries", resourcePath("com/formulasearchengine/mathosphere/mlp/gold/gold.json"),
      "--nd", resourcePath("com/formulasearchengine/mathosphere/mlp/gold/nd.json"),
      "--tex",
      "-t", "0.8",
      "--level", "2",
      "--ref", resourcePath("com/formulasearchengine/mathosphere/mlp/nd"),
    };
    final PrintStream stdout = System.out;
    final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(myOut));
    Main.main(args);
    System.setOut(stdout);
  }


  @Test
  public void testPatternMatcher() throws Exception {
    final File temp = Files.createTempDir();
    String[] args = {CliParams.EVAL,
      "-in", resourcePath("com/formulasearchengine/mathosphere/mlp/gold/eval_dataset_sample.xml"),
      "-out", temp.getAbsolutePath(),
      "--queries", resourcePath("com/formulasearchengine/mathosphere/mlp/gold/gold.json"),
      "--nd", resourcePath("com/formulasearchengine/mathosphere/mlp/gold/nd.json"),
      "--tex",
      "--usePatternMatcher",
    };
    final PrintStream stdout = System.out;
    final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(myOut));
    Main.main(args);
    System.setOut(stdout);
  }

  @Test
  @Ignore
  /**
   * Actually not a test but documentation of how the svm training is done.
   * Takes long.
   * Does not work with the eval_dataset_sample.xml because it has too little samples.
   */
  public void testMachineLearning() throws Exception {
    final File temp = Files.createTempDir();
    String[] args = {CliParams.ML,
      "-in", resourcePath("com/formulasearchengine/mathosphere/mlp/gold/eval_dataset.xml"),
      "-out", temp.getAbsolutePath(),
      "--goldFile", resourcePath("com/formulasearchengine/mathosphere/mlp/gold/gold.json"),
      "--tex",
      "--threads", "10",
      "--writeInstances",
      "--writeSvmModel"
    };
    Main.main(args);
  }

  @Test
  @Ignore
  /**
   * Actually not a test but documentation of how the svm training is done.
   * Takes long.
   * Does not work with the eval_dataset_sample.xml because it has too little samples.
   */
  public void testMachineLearningPercent() throws Exception {
    final File temp = Files.createTempDir();
    String[] args = {CliParams.ML,
      "-in", resourcePath("com/formulasearchengine/mathosphere/mlp/gold/eval_dataset.xml"),
      "-out", temp.getAbsolutePath(),
      "--goldFile", resourcePath("com/formulasearchengine/mathosphere/mlp/gold/gold_with_alias.json"),
      "--tex",
      "--texvcinfo", "http://localhost:10044/texvcinfo",
      "--threads", "10",
      "--samplePercent", "10",
      "--samplePercent", "20",
      "--samplePercent", "30",
      "--samplePercent", "40",
      "--samplePercent", "50",
      "--samplePercent", "60",
      "--samplePercent", "70",
      "--samplePercent", "80",
      "--samplePercent", "90",
      "--samplePercent", "100",
    };
    Main.main(args);
  }

  /**
   * Tests if the classification throws no error. Also tests if a correct definiens is extracted. Must have a good model!
   *
   * @throws Exception
   */
  @Test
  public void testMachineLearningClassification() throws Exception {
    final File temp = Files.createTempDir();
    String[] args = {CliParams.CLASSIFY,
      "-in", resourcePath("com/formulasearchengine/mathosphere/mlp/gold/eval_dataset_sample.xml"),
      "-out", temp.getAbsolutePath(),
      "--tex",
      "--threads", "1",
      "--svmModel", resourcePath("com/formulasearchengine/mathosphere/mlp/ml/svm_model__c_1.0_gamma_0.022097087.model"),
      "--stringFilter", resourcePath("com/formulasearchengine/mathosphere/mlp/ml/string_filter__c_1.0_gamma_0.022097087.model"),
    };
    final PrintStream stdout = System.out;
    final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(myOut));
    Main.main(args);
    System.setOut(stdout);
    final File extraction = new File(temp.getAbsolutePath() + "/extractedDefiniens/json");
    //must be a rather small file, lets assume smaller than 2kb. This is also a sanity check not to deserialize a large file in case of error.
    Assert.assertTrue(extraction.length() < 2 * 1024);
    ObjectMapper mapper = new ObjectMapper();
    StrippedWikiDocumentOutput strippedWikiDocumentOutput = mapper.readValue(extraction, StrippedWikiDocumentOutput.class);
    Assert.assertEquals(strippedWikiDocumentOutput.getTitle(), "Martingale (betting system)");
    Assert.assertTrue(strippedWikiDocumentOutput.getRelations().contains(new IdentifierDefinition("q", "probability")));
  }

  @Test
  @Ignore
  /**
   * Actually not a test but documentation of how the svm optimisation is done.
   */
  public void testMachineLearningClassificationWithNamespaces() throws Exception {
    final File temp = Files.createTempDir();
    String[] args = {CliParams.CLASSIFY,
      "-in", resourcePath("com/formulasearchengine/mathosphere/mlp/gold/eval_dataset.xml"),
      "-out", temp.getAbsolutePath(),
      "--tex",
      "--namespace",
      "--nd", "C:\\tmp\\mlp-tmp\\gold-weak-snowball-True-True-svd-150-kmeans-k=10000.json",
      "--texvcinfo", "http://localhost:10044/texvcinfo",
      "--threads", "5",
      "--evaluate",
      "--svmModel", resourcePath("com/formulasearchengine/mathosphere/mlp/ml/svm_model__c_1.0_gamma_0.022097087.model"),
      "--stringFilter", resourcePath("com/formulasearchengine/mathosphere/mlp/ml/string_filter__c_1.0_gamma_0.022097087.model"),
    };
    Main.main(args);
  }

  @Test
  @Ignore
  /**
   * Actually not a test but documentation of how the svm optimisation was done.
   */
  public void testMachineLearningFromPreprocessedInstances() throws Exception {
    final File temp = Files.createTempDir();
    String[] args = {CliParams.ML,
      "-in", resourcePath("com/formulasearchengine/mathosphere/mlp/gold/eval_dataset.xml"),
      "-out", temp.getAbsolutePath(),
      "--goldFile", resourcePath("com/formulasearchengine/mathosphere/mlp/gold/gold.json"),
      "--threads", "10",
      "--instances", resourcePath("com/formulasearchengine/mathosphere/mlp/ml/instances.arff")
    };
    Main.main(args);
  }

  @Test
  @Ignore
  /**
   * Actually not a test but documentation of how the svm optimisation was done.
   */
  public void testMachineLearningCorase() throws Exception {
    final File temp = Files.createTempDir();
    List<String> costAndGamma = new ArrayList<>();
    for (double c : WekaLearner.C_coarse) {
      costAndGamma.add("--svmCost");
      costAndGamma.add("" + c);
    }
    for (double g : WekaLearner.Y_coarse) {
      costAndGamma.add("--svmGamma");
      costAndGamma.add("" + g);
    }
    String[] args = {CliParams.ML,
      "-in", resourcePath("com/formulasearchengine/mathosphere/mlp/gold/eval_dataset.xml"),
      "-out", temp.getAbsolutePath(),
      "--goldFile", resourcePath("com/formulasearchengine/mathosphere/mlp/gold/gold.json"),
      "--tex",
      "--texvcinfo", "http://localhost:10044/texvcinfo",
      "--threads", "1",
      "--writeInstances"
    };
    String[] allArgs = Stream.concat(Arrays.stream(args), costAndGamma.stream()).toArray(String[]::new);
    Main.main(allArgs);
  }

}
