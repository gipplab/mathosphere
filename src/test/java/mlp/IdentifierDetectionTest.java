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

import java.util.Set;

import mlp.utils.MathIdentifierExtractor;

import org.junit.Test;

import com.google.common.collect.Iterables;

/**
 * @author rob
 */
public class IdentifierDetectionTest {

    @Test
    public void singleIdentifiers() {
        Set<String> detected;

        String x = "x";
        detected = MathIdentifierExtractor.getAllfromTex(x);
        assertEquals("Number of identifiers mismatch (should be 1)", 1, detected.size());
        assertEquals("Extracted identifier did not match", x, Iterables.get(detected, 0));

        String xx = "x + x^{2}";
        detected = MathIdentifierExtractor.getAllfromTex(xx);
        assertEquals("Number of identifiers mismatch (should be 1)", 1, detected.size());
        assertEquals("Extracted identifier did not match", x, Iterables.get(detected, 0));
    }
}
