package com.formulasearchengine.mathosphere.mlp.cli;

import org.junit.Test;

import static org.junit.Assert.*;

public class CliParamsTest {

  @Test
  public void help() {
    String[] args = { "help" };
    CliParams params = CliParams.from(args);
    assertEquals("help", params.getCommand());
  }

  @Test
  public void useTex() {
    String[] args = { "mlp", "--tex" };
    CliParams params = CliParams.from(args);
    assertEquals("mlp", params.getCommand());
    assertTrue(params.getFlinkMlp().getUseTeXIdentifiers());
    // assertTrue(params.getMlp().getUseTeXIdentifiers());
  }

  @Test
  public void NotUseTex() {
    String[] args = { "mlp", "" };
    CliParams params = CliParams.from(args);
    assertEquals("mlp", params.getCommand());
    assertFalse(params.getFlinkMlp().getUseTeXIdentifiers());
    assertFalse(params.getMlp().getUseTeXIdentifiers());
  }

  @Test
  public void count() {
    String[] args = { "count", "--formulae", "-in", "c:/tmp/mlp/input/" };
    CliParams params = CliParams.from(args);
    assertEquals("count", params.getCommand());

    CountCommandConfig count = params.getCount();

    assertTrue(count.isFormulas());
    assertFalse(count.isDefinitions());
    assertFalse(count.isIdentifiers());
    assertEquals("c:/tmp/mlp/input/", count.getInput());
  }

}
