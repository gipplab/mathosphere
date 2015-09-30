package mlp.pojos;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import mlp.text.WikiTextUtils;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static mlp.text.WikiTextUtilsTest.getTestResource;

/**
 * Created by Moritz on 30.09.2015.
 */
public class MathTagTest {

  @Test
  public void testGetContentHash() throws Exception {
    MathTag t = new MathTag(1, "<math><mrow><mo>x</mo><mrow></math>", WikiTextUtils.MathMarkUpType.MATHML);
    assertEquals("2cf05995ef521b456aa419201d160406", t.getContentHash());
  }

  @Test
  public void testPlaceholder() throws Exception {

  }

  @Test
  public void testGetMarkUpType() throws Exception {

  }

  @Test
  public void testGetIdentifier() throws Exception {
    MathTag tagX = new MathTag(1, "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mrow><mi>x</mi></mrow></math>", WikiTextUtils.MathMarkUpType.MATHML);
    assertEquals(ImmutableSet.of("x"), tagX.getIdentifier(true, true).elementSet());
    assertEquals(ImmutableSet.of("x"), tagX.getIdentifier(false, true).elementSet());
    MathTag schrödinger = new MathTag(1,getTestResource("schrödinger_eq.xml"),WikiTextUtils.MathMarkUpType.MATHML);
    //MathTag schrödingerTex = new MathTag(1,"i\\hbar\\frac{\\partial}{\\partial t}\\Psi(\\mathbb{r},\\,t)=-\\frac{\\hbar^{2}}{2m}" +
    //  "\\nabla^{2}\\Psi(\\mathbb{r},\\,t)+V(\\mathbb{r})\\Psi(\\mathbb{r},\\,t).", WikiTextUtils.MathMarkUpType.LATEX);
    ImmutableMultiset<String> lIds = ImmutableMultiset.of("i",
      "\\hbar", "\\hbar",
      "t", "t", "t", "t",
      "\\Psi", "\\Psi", "\\Psi",
      "\\mathbb{r}", "\\mathbb{r}", "\\mathbb{r}", "\\mathbb{r}",
      "V",
      "m");
    assertEquals(lIds,schrödinger.getIdentifier(true,false));

  }
}
