package com.formulasearchengine.mathosphere.mathpd.pojos;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Moritz on 28.04.2017.
 */
public class ExtractedMathPDDocumentTest {
    @Test
    public void getPlainText() throws Exception {
        String input =
                "For a constant potential, <math>V</math> , the solution is oscillatory for <math>E > V_{0}</math> "
                        + "and exponential for <math>E < V_{0}\n</math> , corresponding to energies that are allowed or disallowed "
                        + "in classical mechanics. ";
        String expected = "For a constant potential,  , the solution is oscillatory for  and exponential for  , corresponding to energies that are allowed or disallowed in classical mechanics. ";
        final ExtractedMathPDDocument document = new ExtractedMathPDDocument("test", input);
        CharSequence actual = document.getPlainText();
        assertEquals(expected, actual);
    }
}