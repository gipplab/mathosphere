package com.formulasearchengine.mathosphere.mlp;

import com.formulasearchengine.mathosphere.mlp.cli.FlinkMlpCommandConfig;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;

/**
 * Created by Moritz on 12.11.2015.
 */
public class FlinkMlpRelationFinderTest {

  @Test
  public void testRunFromText() throws Exception {
    FlinkMlpCommandConfig config = FlinkMlpCommandConfig.test();
    final FlinkMlpRelationFinder finder = new FlinkMlpRelationFinder();
    String input = "In <math>E=mc^2</math>, <math>E</math> stands for energy," +
        " <math>m</math> denotes mass and <math>c</math> is the speed of light.";
    final String s = finder.runFromText(config, input);
    assertThat(s, containsString("mass"));
  }

  @Test
  public void testRunFromText2() throws Exception {
    FlinkMlpCommandConfig config = FlinkMlpCommandConfig.test();
    config.setUseTeXIdentifiers(true);
    final FlinkMlpRelationFinder finder = new FlinkMlpRelationFinder();
    String input = "The symbol ''r'' i.e. <math>r\\in\\mathbb{R}</math> denotes a real numbers.";
    final String s = finder.runFromText(config, input);
    assertThat(s, containsString("real numbers"));
  }

  @Test
  public void testRunFromText3() throws Exception {
    FlinkMlpCommandConfig config = FlinkMlpCommandConfig.test();
    config.setUseTeXIdentifiers(true);
    final FlinkMlpRelationFinder finder = new FlinkMlpRelationFinder();
    String input = "The symbol <math>\\mu</math> denotes the magnetic moment.";
    final String s = finder.runFromText(config, input);
    assertThat(s, containsString("magnetic moment"));
  }

  @Test
  public void testRunFromHamText() throws Exception {
    FlinkMlpCommandConfig config = FlinkMlpCommandConfig.test();
    config.setUseTeXIdentifiers(true);
    config.setWikiDataFile(getClass().getResource("text/test-map-no-dup.csv").getFile());

    final FlinkMlpRelationFinder finder = new FlinkMlpRelationFinder();
    String input = IOUtils.toString(FlinkMlpRelationFinderTest.class.getResourceAsStream("ham_optimized_wiki.txt"));
    final String s = finder.runFromText(config, input);
    assertThat(s, containsString("Q155640"));
  }
}