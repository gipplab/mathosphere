package mlp.text;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import mlp.PatternMatchingRelationFinder;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;

import static org.junit.Assert.*;

public class MathMLUtilsTest {

  @Test
  public void extractFromTex_simple() {
    String tex = "x^2 + y^2 = z^2";
    Set<String> identifiers = MathMLUtils.extractIdentifiersFromTex(tex).elementSet();
    Set<String> expected = ImmutableSet.of("x", "y", "z");
    assertEquals(expected, identifiers);
  }

  @Test
  public void extractFromTex_moreComplex() {
    String tex = "\\sqrt{x + y} = \\cfrac{\\phi + \\rho}{\\Theta \\cdot \\Phi}";
    Set<String> identifiers = MathMLUtils.extractIdentifiersFromTex(tex).elementSet();
    Set<String> expected = ImmutableSet.of("x", "y", "φ", "ρ", "Θ", "Φ");
    assertEquals(expected, identifiers);
  }

  @Test
  public void extractFromTex_subscripts() {
    String tex = "\\sigma_1 + \\sigma_2 = r_1";
    Set<String> identifiers = MathMLUtils.extractIdentifiersFromTex(tex).elementSet();
    Set<String> expected = ImmutableSet.of("σ_1", "σ_2", "r_1");
    assertTrue(identifiers.containsAll(expected));
  }

  @Test
  public void extractFromTex_superscriptIndentifier() {
    String tex = "\\sigma^x";
    Set<String> identifiers = MathMLUtils.extractIdentifiersFromTex(tex).elementSet();
    Set<String> expected = ImmutableSet.of("σ", "x");
    assertTrue(identifiers.containsAll(expected));
  }

  @Test
  public void extractFromTex_capturesMultipleOccurrences() {
    String tex = "\\sigma^2 + \\sigma + b";
    Multiset<String> identifiers = MathMLUtils.extractIdentifiersFromTex(tex);
    Multiset<String> expected = HashMultiset.create(Arrays.asList("σ", "σ", "b"));
    assertEquals(expected, identifiers);
  }

  @Test
  public void extractFromTex_oneIdTwoOccurrences_sizeIs2() {
    String tex = "\\sigma + \\sigma";
    Multiset<String> identifiers = MathMLUtils.extractIdentifiersFromTex(tex);
    assertEquals(2, identifiers.size());
  }

  @Test
  public void extractFromTex_boldText() {
    String tex = "\\mathbf r";
    Set<String> identifiers = MathMLUtils.extractIdentifiersFromTex(tex).elementSet();
    Set<String> expected = ImmutableSet.of("r");
    assertTrue(identifiers.containsAll(expected));
  }

  @Test
  public void extractFromMathML_complextMsub_noSubCaptured() throws Exception {
    String mathML = readResource("complex_msub.xml");
    Set<String> identifiers = MathMLUtils.extractIdentifiersFromMathML(mathML).elementSet();
    identifiers.forEach(id -> assertFalse(id.contains("_")));
  }

  private static String readResource(String file) throws IOException {
    InputStream inputStream = PatternMatchingRelationFinder.class.getResourceAsStream(file);
    return IOUtils.toString(inputStream);
  }

  @Test
  public void extractFromMathMl_identifiersFromMSub_notCaptured() throws Exception {
    String mathML = readResource("math-R_specific.xml");
    Set<String> identifiers = MathMLUtils.extractIdentifiersFromMathML(mathML).elementSet();
    Set<String> expected = ImmutableSet.of("R_specific", "R", "M");
    assertFalse(identifiers.contains("specific"));
    assertEquals(expected, identifiers);
  }

  @Test
  public void extractFromMathML_notParsable() throws Exception {
    String mathML = readResource("math-xmlparsingerror.xml");
    Set<String> identifiers = MathMLUtils.extractIdentifiersFromMathML(mathML).elementSet();
    // "a" is a stop word, it's removed
    Set<String> expected = ImmutableSet.of("x", "b", "c");
    assertEquals(expected, identifiers);
  }

  @Test
  public void extractFromMathML_isNumeric() throws Exception {
    assertFalse(MathMLUtils.isNumeric("a"));
    assertTrue(MathMLUtils.isNumeric("1"));
    assertTrue(MathMLUtils.isNumeric("10"));
    assertTrue(MathMLUtils.isNumeric("10.0"));
    assertTrue(MathMLUtils.isNumeric("10.00001"));
    assertFalse(MathMLUtils.isNumeric("10.00001a"));
    assertFalse(MathMLUtils.isNumeric("x1"));
  }

}
