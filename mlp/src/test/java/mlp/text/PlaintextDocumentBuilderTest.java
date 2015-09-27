package mlp.text;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PlaintextDocumentBuilderTest {

  @Test
  public void extractPlainText_wikiLinks() {
    String input = "In [[quantum mechanics]], the '''Schrödinger equation''' is a "
      + "[[partial differential equation]] "
      + "that describes how the [[quantum state]] of a [[physical system]] changes with [[time]]. "
      + "It was formulated in late 1925, and published in 1926, by the [[Austria]]n [[physicist]] "
      + "[[Erwin Schrödinger]]. "
      + "In the [[Copenhagen interpretation|standard interpretation of quantum mechanics]], "
      + "the wavefunction is the most complete description that can be given to a physical system.";

    String actual = WikiTextUtils.extractPlainText(input);
    String expected = "In “quantum mechanics”, the Schrödinger equation is a “partial differential equation” "
      + "that describes how the “quantum state” of a “physical system” changes with “time”. "
      + "It was formulated in late 1925, and published in 1926, by the “Austrian” “physicist” "
      + "“Erwin Schrödinger”. "
      + "In the “standard interpretation of quantum mechanics”, "
      + "the wavefunction is the most complete description that can be given to a physical system.";
    assertEquals(expected, actual);
  }

  @Test
  public void extractPlainText_removeTagsAndItalic() {
    String input = "For a constant potential, ''V'' = ''V''<sub>0</sub>, the solution is oscillatory "
      + "for ''E'' > "
      + "''V''<sub>0</sub> and exponential for ''E'' < ''V''<sub>0</sub>, corresponding to "
      + "energies that "
      + "are allowed or disallowed in classical mechanics.";

    String actual = WikiTextUtils.extractPlainText(input);

    String expected = "For a constant potential, V = V_0, the solution is oscillatory for E > V_0 "
      + "and exponential for E < V_0, corresponding to energies that are allowed or disallowed "
      + "in classical mechanics.";
    assertEquals(expected, actual);
  }

}
