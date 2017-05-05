package com.formulasearchengine.mathosphere.mlp.performance;

import com.formulasearchengine.mathosphere.mlp.Main;
import com.google.common.base.Throwables;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

public class DefinitionExtractionTest {

  @Test
  @Ignore
  public void testPerformance() throws Exception {
    PerformanceHelper.runTestCollection("com/formulasearchengine/mathosphere/mlp/performance", "extract");
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
  @Ignore
  /**
   * runs the full definition extraction pipeline on a large data set.
   */
  public void testEval() throws Exception {
    File temp;
    temp = File.createTempFile("temp gamma 0.1 threshold 0.6", Long.toString(System.nanoTime()));
    String[] args = {"eval",
      "-in", resoucePath("com/formulasearchengine/mathosphere/mlp/gold/eval_dataset.xml"),
      "-out", temp.getAbsolutePath(),
      "--queries", resoucePath("com/formulasearchengine/mathosphere/mlp/gold/gold.json"),
      "--tex",
      "--texvcinfo", "http://localhost:10044/texvcinfo",
      "--gamma", "0.1f",
      "--threshold", "0.6f"};
    final PrintStream stdout = System.out;
    final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(myOut));
    Main.main(args);
    final String standardOutput = myOut.toString();
    //assertThat(standardOutput, containsString ("W(2, k) > 2^k/k^\\varepsilon"));
    System.setOut(stdout);
    System.out.println(standardOutput);
  }

}
