package com.formulasearchengine.mathosphere.mlp.cli;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CliParamsTest {

    @Test
    public void count() {
        String[] args = {"count", "--formulae", "-in", "c:/tmp/mlp/input/", "-out", "c:/tmp/mlp/out/"};
        CliParams params = CliParams.from(args);
        assertEquals("count", params.getCommand());

        CountCommandConfig count = params.getCount();

        assertTrue(count.isFormulas());
        assertFalse(count.isDefinitions());
        assertFalse(count.isIdentifiers());
        assertEquals("c:/tmp/mlp/input/", count.getInput());
    }

    @Test
    public void help() {
        String[] args = {"help"};
        CliParams params = CliParams.from(args);
        assertEquals("help", params.getCommand());
    }

    @Test
    public void notUseTex() {
        String[] args = {"mlp", "-in", "c:/tmp/mlp/input/", "-out", "c:/tmp/mlp/out/"};
        CliParams params = CliParams.from(args);
        assertEquals("mlp", params.getCommand());
        assertFalse(params.getMlpCommandConfig().getUseTeXIdentifiers());
    }

    @Test
    public void texvcinfo() {
        String[] args = {"tags", "--texvcinfo", "expected", "-in", "c:/tmp/mlp/input/", "-out", "c:/tmp/mlp/out/"};
        CliParams params = CliParams.from(args);
        assertEquals("expected", params.getTagsCommandConfig().getTexvcinfoUrl());
    }

    @Test
    public void useTex() {
        String[] args = {"mlp", "--tex", "-in", "c:/tmp/mlp/input/", "-out", "c:/tmp/mlp/out/"};
        CliParams params = CliParams.from(args);
        assertEquals("mlp", params.getCommand());
        assertTrue(params.getMlpCommandConfig().getUseTeXIdentifiers());
    }

    @Test
    public void voidTest() {
        String[] args = {};
        CliParams params = CliParams.from(args);
        assertEquals("help", params.getCommand());
    }
}
