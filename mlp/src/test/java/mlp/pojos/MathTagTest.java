package mlp.pojos;

import mlp.text.WikiTextUtils;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

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
}
