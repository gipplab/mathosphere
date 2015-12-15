package com.formulasearchengine.mathosphere.mlp.cli;

import com.formulasearchengine.mathosphere.mlp.Main;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import static org.junit.Assert.assertTrue;

public class CliMainTest {

	@Test
	public void testMlpRus() throws Exception {
		String[] args = new String[9];
		final ClassLoader classLoader = getClass().getClassLoader();
		final File temp;
		temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
		args[0] = "mlp";
		args[1] = "-in";
		args[2] = classLoader.getResource("com/formulasearchengine/mathosphere/mlp/wikirusample.xml").getFile();
		args[3] = "-out";
		args[4] = temp.getAbsolutePath();
		args[5] = "--language";
		args[6] = "ru";
		args[7] = "-pos";
		args[8] = "";
		System.out.println(temp.getAbsolutePath());
		runTest(args);
		// System.out.println(standardOutput);
	}

	@Test
	public void testMlpRusPlain() throws Exception {
		String[] args = new String[11];
		final ClassLoader classLoader = getClass().getClassLoader();
		final File temp;
		temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
		args[0] = "mlp";
		args[1] = "-in";
		args[2] = classLoader.getResource("com/formulasearchengine/mathosphere/mlp/ru-sample.xml").getFile();
		args[3] = "-out";
		args[4] = temp.getAbsolutePath();
		args[5] = "--language";
		args[6] = "ru";
		args[7] = "-pos";
		args[8] = "";
		args[9] = "-T";
		args[10] = "";
		System.out.println(temp.getAbsolutePath());
		String res = runTest(args);
		System.out.println(res);
	}

	public String runTest(String[] args) throws Exception {
		final PrintStream stdout = System.out;
		final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
		System.setOut(new PrintStream(myOut));
		final long t0 = System.nanoTime();
		Main.main(args);
		final String standardOutput = myOut.toString();
		System.setOut(stdout);
		System.out.println((System.nanoTime()-t0)/1000000000+"s");
		return standardOutput;
	}

	@Test
  public void testExtract() throws Exception {

    String[] args = new String[3];
    final ClassLoader classLoader = getClass().getClassLoader();
    args[0] = "extract";
    args[1] = "-in";
    args[2] = classLoader.getResource("com/formulasearchengine/mathosphere/mlp/hamiltonian_esc.txt").getFile();
    final PrintStream stdout = System.out;
    final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(myOut));
    Main.main(args);
    final String standardOutput = myOut.toString();
    assertTrue(standardOutput.contains("magnetic dipole moment"));
    System.setOut(stdout);
    // System.out.println(standardOutput);
  }

  @Test
  public void testList() throws Exception {
    String[] args = new String[4];
    final ClassLoader classLoader = getClass().getClassLoader();
    args[0] = "list";
    args[1] = "-in";
    args[2] = classLoader.getResource("com/formulasearchengine/mathosphere/mlp/hamiltonian_esc.txt").getFile();
    args[3] = "-T";
    final PrintStream stdout = System.out;
    final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(myOut));
    Main.main(args);
    final String standardOutput = myOut.toString();
    assertTrue(standardOutput.contains("\\epsilon_{0}"));
    System.setOut(stdout);
    // System.out.println(standardOutput);
  }

  @Test
  public void testCount() throws Exception {
    String[] args = new String[4];
    final ClassLoader classLoader = getClass().getClassLoader();
    args[0] = "count";
    args[1] = "-in";
    args[2] = classLoader.getResource("identifier.json").getFile();
    args[3] = "--ids";
    final PrintStream stdout = System.out;
    final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(myOut));
    Main.main(args);
    final String standardOutput = myOut.toString();
    assertTrue(standardOutput.contains("{\"element\":\"i\",\"count\":42}"));
    System.setOut(stdout);
    //System.out.println(standardOutput);
  }

  @Test
  public void testCountTok() throws Exception {
    String[] args = new String[3];
    final ClassLoader classLoader = getClass().getClassLoader();
    args[0] = "count";
    args[1] = "-in";
    args[2] = classLoader.getResource("tokens.json").getFile();
    final PrintStream stdout = System.out;
    final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(myOut));
    Main.main(args);
    final String standardOutput = myOut.toString();
    assertTrue(standardOutput.contains("{\"element\":{\"f0\":\"TEX_ONLY\",\"f1\":\"i\",\"arity\":2},\"count\":88}"));
    System.setOut(stdout);
    //System.out.println(standardOutput);
  }
}

