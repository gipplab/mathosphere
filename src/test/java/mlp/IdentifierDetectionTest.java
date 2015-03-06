/*        __
 *        \ \
 *   _   _ \ \  ______
 *  | | | | > \(  __  )
 *  | |_| |/ ^ \| || |
 *  | ._,_/_/ \_\_||_|
 *  | |
 *  |_|
 * 
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <rob ∂ CLABS dot CC> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return.
 * ----------------------------------------------------------------------------
 */
package mlp;

import static org.junit.Assert.assertEquals;

import java.util.List;

import mlp.utils.TexIdentifierExtractor;

import org.junit.Test;

/**
 * @author rob
 */
public class IdentifierDetectionTest {

    @Test
    public void singleIdentifiers() {
        List<String> detected;

        String x = "x";
        detected = TexIdentifierExtractor.getAllfromTex(x);
        assertEquals("Number of identifiers mismatch (should be 1)", 1, detected.size());
        assertEquals("Extracted identifier did not match", x, detected.get(0));

        String xx = "x + x^{2}";
        detected = TexIdentifierExtractor.getAllfromTex(xx);
        assertEquals("Number of identifiers mismatch (should be 1)", 1, detected.size());
        assertEquals("Extracted identifier did not match", x, detected.get(0));
    }
}
