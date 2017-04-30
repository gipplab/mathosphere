package com.formulasearchengine.mathosphere.mlp.performance;

import com.formulasearchengine.mathosphere.mlp.StupidRelationFinder;
import com.formulasearchengine.mathosphere.mlp.cli.MachineLearningDefinienExtractionConfig;
import com.google.common.io.Files;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

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
    StupidRelationFinder.find(MachineLearningDefinienExtractionConfig.test());
    System.setOut(stdout);
  }
}
