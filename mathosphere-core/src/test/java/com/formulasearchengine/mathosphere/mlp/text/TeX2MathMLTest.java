package com.formulasearchengine.mathosphere.mlp.text;

import org.junit.Test;

import java.net.UnknownHostException;

import static org.junit.Assert.assertTrue;

/**
 * Created by Moritz on 28.09.2015.
 */
public class TeX2MathMLTest {

  @Test
  public void testTeX2MML() throws Exception {
    try {
      String MML = TeX2MathML.TeX2MML("E=mc^2");
      assertTrue(MML.contains("</math>"));
    } catch (UnknownHostException u) {
      //TODO: figure out how to skip tests
    }
  }
}
