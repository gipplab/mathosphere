package com.formulasearchengine.mathosphere.mlp.text;

import com.formulasearchengine.mathosphere.mlp.PatternMatchingRelationFinder;
import com.formulasearchengine.mathosphere.mlp.pojos.MathTag;
import com.formulasearchengine.mathosphere.mlp.text.WikiTextUtils.MathMarkUpType;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class WikiTextUtilsTest {

  public static String getTestResource(String testFile) throws IOException {
    InputStream stream = PatternMatchingRelationFinder.class.getResourceAsStream(testFile);
    return IOUtils.toString(stream, "utf-8");
  }

  @Test
  public void findMathTags() {
    String input = "Text text <math>V = V_0</math> text text <math>V = V_1</math> text. "
      + "Text <math>V = V_2</math>.";
    List<MathTag> actual = WikiTextUtils.findMathTags(input);
    List<MathTag> expected = Arrays.asList(
      new MathTag(10, "<math>V = V_0</math>", MathMarkUpType.LATEX),
      new MathTag(41, "<math>V = V_1</math>", MathMarkUpType.LATEX),
      new MathTag(73, "<math>V = V_2</math>", MathMarkUpType.LATEX));
    assertEquals(expected, actual);
  }

  @Test
  public void findMathTags_first() {
    String input = "<math>V = V_0</math> text text.";
    List<MathTag> actual = WikiTextUtils.findMathTags(input);
    List<MathTag> expected = Arrays.asList(new MathTag(0, "<math>V = V_0</math>", MathMarkUpType.LATEX));
    assertEquals(expected, actual);
  }

  @Test
  public void replaceAllFormulas() {
    String text = "Text text <math>V = V_0</math> text text <math>V = V_1</math> text. "
      + "Text <math>V = V_2</math>.";

    MathTag tag1 = new MathTag(10, "<math>V = V_0</math>", MathMarkUpType.LATEX);
    MathTag tag2 = new MathTag(41, "<math>V = V_1</math>", MathMarkUpType.LATEX);
    MathTag tag3 = new MathTag(73, "<math>V = V_2</math>", MathMarkUpType.LATEX);
    List<MathTag> tags = Arrays.asList(tag1, tag2, tag3);

    String actual = WikiTextUtils.replaceAllFormulas(text, tags);

    String expected = "Text text " + tag1.placeholder() + " text text " + tag2.placeholder() + " text. "
      + "Text " + tag3.placeholder() + ".";

    assertEquals(expected, actual);
  }

  @Test
  public void extractPlainText_subsup() {
    String input = "V = V<sub>0</sub>. E < V<sup>24</sup>";
    String actual = WikiTextUtils.subsup(input);

    String expected = "V = V_0. E < V^24";
    assertEquals(expected, actual);
  }

  @Test
  public void guessMarkupType_normalMathTag_isLatex() {
    String text = "<math>E_{rot} = \\frac{l(l+1) \\hbar^2}{2 \\mu r_{0}^2}</math>";
    MathTag tag = WikiTextUtils.findMathTags(text).get(0);
    assertEquals(MathMarkUpType.LATEX, tag.getMarkUpType());
  }

  @Test
  public void guessMarkupType_weirdMathTag_isLatex() {
    String text = "<math style>E_{rot} = \\frac{l(l+1) \\hbar^2}{2 \\mu r_{0}^2} l=0,1,2,... </math>";
    MathTag tag = WikiTextUtils.findMathTags(text).get(0);
    assertEquals(MathMarkUpType.LATEX, tag.getMarkUpType());
  }

  @Test
  public void guessMarkupType_isMathML() {
    String text = "<math><mi>x</mi></math>";
    MathTag tag = WikiTextUtils.findMathTags(text).get(0);
    assertEquals(MathMarkUpType.MATHML, tag.getMarkUpType());
  }

  @Test
  public void testReplaceAllFormulas1() throws Exception {

  }

  @Test
  public void testRenderAllFormulae() throws Exception {
    //final ClassLoader classLoader = getClass().getClassLoader();
    //String testString = PosTaggerTest.readText("mean_wiki.txt");
    String testString = "The energy <math>E</math>,";
    String out = WikiTextUtils.renderAllFormulae(testString);
    assertEquals("The energy <math xmlns=\"http://www.w3.org/1998/Math/MathML\" id=\"p1.1.m1.1\" class=\"ltx_Math\" alttext=\"E\" display=\"inline\">\n" +
      "  <semantics id=\"p1.1.m1.1a\">\n" +
      "    <mi id=\"p1.1.m1.1.1\" xref=\"p1.1.m1.1.1.cmml\">E</mi>\n" +
      "    <annotation-xml encoding=\"MathML-Content\" id=\"p1.1.m1.1b\">\n" +
      "      <ci id=\"p1.1.m1.1.1.cmml\" xref=\"p1.1.m1.1.1\">E</ci>\n" +
      "    </annotation-xml>\n" +
      "    <annotation encoding=\"application/x-tex\" id=\"p1.1.m1.1c\">E</annotation>\n" +
      "  </semantics>\n" +
      "</math>,", out);

  }

  @Ignore
  @Test
  public void renderResource() throws Exception {
    final String name = "n20";
    String testString = PosTaggerTest.readText(name + "_wiki.txt");
    String targetPath=PatternMatchingRelationFinder.class.getResource(name+"_wiki.txt").getPath().replace("_wiki.txt", "_exc.txt");
    FileWriter writer = new FileWriter(targetPath);
    String out = WikiTextUtils.renderAllFormulae(testString);
    writer.write(out);
    writer.close();
    System.out.println(out);
  }
}
