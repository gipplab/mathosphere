package com.formulasearchengine.mathosphere.mlp.performance;

import com.formulasearchengine.mathosphere.TestUtils;
import com.formulasearchengine.mathosphere.mlp.StupidRelationFinder;
import com.formulasearchengine.mathosphere.mlp.cli.CliParams;
import com.formulasearchengine.mathosphere.mlp.cli.MachineLearningDefinienExtractionConfig;
import com.google.common.base.Throwables;
import com.google.common.io.Files;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Created by Leo on 29.03.2017.
 */
public class StupidRelationFinderTest {
  @Test
  public void testStupidRelationFinder() throws Exception {
    final File temp;
    temp = Files.createTempDir();
    System.out.println(temp.getAbsolutePath());
    final PrintStream stdout = System.out;
    final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(myOut));
    String[] args = {
      "-in", resourcePath("com/formulasearchengine/mathosphere/mlp/gold/eval_dataset_sample.xml"),
      "-out", temp.getAbsolutePath(),
      "--goldFile", resourcePath("com/formulasearchengine/mathosphere/mlp/gold/gold_with_alias.json"),
      "--threads", "1",
      "--tex",
    };
    MachineLearningDefinienExtractionConfig config = MachineLearningDefinienExtractionConfig.from(args);
    config.setUseTeXIdentifiers(true);
    StupidRelationFinder.find(config);
    System.setOut(stdout);
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
}
