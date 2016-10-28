package com.formulasearchengine.mathosphere.mathpd;

import com.formulasearchengine.mathosphere.mlp.Main;
import com.google.common.base.Throwables;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by Moritz on 12.11.2015.
 */
public class FlinkPdTest {

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
  public void testCountTok() throws Exception {
    final File temp;
    temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
    String[] args = new String[7];
    args[0] = "pd";
    args[1] = "-in";
    args[2] = resourcePath("com/formulasearchengine/mathosphere/mathpd/test9.xml");
    args[3] = "-ref";
    args[4] = resourcePath("com/formulasearchengine/mathosphere/mathpd/ex1.html");
    args[5] = "-out";
    args[6] = temp.getAbsolutePath();
    final PrintStream stdout = System.out;
    final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(myOut));
    Main.main(args);
    final String standardOutput = myOut.toString();
    assertTrue(standardOutput.contains("switched to FINISHED"));
    System.setOut(stdout);
    System.out.println(standardOutput);
  }

}