package com.formulasearchengine.mathosphere.mlp;

import com.formulasearchengine.mathosphere.mlp.cli.FlinkMlpCommandConfig;
import org.apache.commons.io.IOUtils;
import org.apache.sling.commons.json.JSONObject;
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
  public void testRunFromMMLText() throws Exception {
    FlinkMlpCommandConfig config = FlinkMlpCommandConfig.test();
    final FlinkMlpRelationFinder finder = new FlinkMlpRelationFinder();
    String input = "<p class=\"ltx_p\">A function <math>f</math> " +
            "<math class=\"ltx_Math\" altimg=\"m125.png\" altimg-height=\"23px\" altimg-valign=\"-7px\" altimg-width=\"62px\" alttext=\"f(x,y)\" display=\"inline\">" +
              "<mrow><mi>f</mi><mo>\u2061</mo><mrow><mo stretchy=\"false\">(</mo>" +
              "<mi>x</mi><mo>,</mo><mi>y</mi><mo stretchy=\"false\">)</mo>" +
              "</mrow></mrow>" +
            "</math>" +
            " is <em class=\"ltx_emph ltx_font_italic\">continuous at a point</em> " +
            "<math class=\"ltx_Math\" altimg=\"m52.png\" altimg-height=\"23px\" altimg-valign=\"-7px\" altimg-width=\"48px\" alttext=\"(a,b)\" display=\"inline\">" +
              "<mrow><mo href=\"./front/introduction#Sx4.p1.t1.r28\" stretchy=\"false\">(</mo>" +
              "<mi>a</mi><mo href=\"./front/introduction#Sx4.p1.t1.r28\">,</mo><mi>b</mi><mo href=\"./front/introduction#Sx4.p1.t1.r28\" stretchy=\"false\">)</mo>" +
            "</mrow></math> if</p>";
    final String s = finder.runFromText(config, input);
    JSONObject json = new JSONObject(s);
    System.out.println(json.toString(2));
    assertThat(s, containsString("function"));
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
    String input = IOUtils.toString(FlinkMlpRelationFinderTest.class.getResourceAsStream("ham_optimized_wiki.txt"),"UTF-8");
    final String s = finder.runFromText(config, input);
    assertThat(s, containsString("Q155640"));
  }
}