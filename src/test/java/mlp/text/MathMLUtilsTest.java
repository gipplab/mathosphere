package mlp.text;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.Set;

import mlp.RelationFinder;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public class MathMLUtilsTest {

    @Test
    public void extractFromTex_simple() {
        String tex = "x^2 + y^2 = z^2";
        Set<String> identifiers = MathMLUtils.extractIdentifiersFromTex(tex);
        Set<String> expected = ImmutableSet.of("x", "y", "z");
        assertEquals(expected, identifiers);
    }

    @Test
    public void extractFromTex_moreComplex() {
        String tex = "\\sqrt{x + y} = \\cfrac{\\phi + \\rho}{\\Theta \\cdot \\Phi}";
        Set<String> identifiers = MathMLUtils.extractIdentifiersFromTex(tex);
        Set<String> expected = ImmutableSet.of("x", "y", "φ", "ρ", "Θ", "Φ");
        assertEquals(expected, identifiers);
    }

    @Test
    public void extractFromTex_subscripts() {
        String tex = "\\sigma_1 + \\sigma_2 = r_1";
        Set<String> identifiers = MathMLUtils.extractIdentifiersFromTex(tex);
        Set<String> expected = ImmutableSet.of("σ_1", "σ_2", "r_1");
        assertTrue(identifiers.containsAll(expected));
    }

    @Test
    public void extractFromTex_superscriptIndentifier() {
        String tex = "\\sigma^x";
        Set<String> identifiers = MathMLUtils.extractIdentifiersFromTex(tex);
        Set<String> expected = ImmutableSet.of("σ", "x");
        assertTrue(identifiers.containsAll(expected));
    }

    @Test
    public void extractFromTex_boldText() {
        String tex = "\\mathbf r";
        Set<String> identifiers = MathMLUtils.extractIdentifiersFromTex(tex);
        Set<String> expected = ImmutableSet.of("r");
        assertTrue(identifiers.containsAll(expected));
    }

    @Test
    public void extractFromTex_complextMsub_noSubCaptured() throws Exception {
        InputStream inputStream = RelationFinder.class.getResourceAsStream("complex_msub.xml");
        String mathML = IOUtils.toString(inputStream);
        Set<String> identifiers = MathMLUtils.extractIdentifiersFromMathML(mathML);
        identifiers.forEach(id -> assertFalse(id.contains("_")));
    }
    

}
