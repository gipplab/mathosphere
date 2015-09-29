package mlp.text;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Moritz on 29.09.2015.
 */
public class UnicodeMapTest {

  @Test
  public void testString2TeX() throws Exception {
    String teX = UnicodeMap.string2TeX("Ĥψ=Eψ");
    //@TODO: This is invalid TeX \^ should be replaced by \hat
    assertEquals("\\^{H}\\psi =E\\psi ", teX);
  }

  @Test
  public void testChar2TeX() throws Exception {
    String c = UnicodeMap.char2TeX(1153);
    assertEquals("\\cyrchar\\cyrkoppa ", c);
  }
}
