package mlp.text;

import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by Moritz on 28.09.2015.
 */
public class TeX2MathMLTest  {

  @Test
  public void testTeX2MML() throws Exception {
    String MML = TeX2MathML.TeX2MML("E=mc^2");
    Assert.assertTrue(MML.contains("</math>"));
  }
}
