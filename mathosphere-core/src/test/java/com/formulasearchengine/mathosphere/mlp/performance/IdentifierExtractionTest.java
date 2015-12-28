package com.formulasearchengine.mathosphere.mlp.performance;

import org.junit.Test;

public class IdentifierExtractionTest {

  @Test
  public void testPerformance() throws Exception {
    PerformanceHelper.runTestCollection("com/formulasearchengine/mathosphere/mlp/performance","list");
  }
}
