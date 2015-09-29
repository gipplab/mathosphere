package mlp.cli;

import mlp.Main;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertTrue;

public class CliMainTest {
  @Test
  public void testMLP() throws Exception {

    String[] args = new String[3];
    final ClassLoader classLoader = getClass().getClassLoader();
    args[0] = "extract";
    args[1] = "-in";
    args[2] = classLoader.getResource("mlp/hamiltonian_esc.txt").getFile();
    final PrintStream stdout = System.out;
    final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(myOut));
    Main.main(args);
    final String standardOutput = myOut.toString();
    assertTrue(standardOutput.contains("magnetic dipole moment"));
    System.setOut(stdout);
    System.out.println(standardOutput);
  }

  @Test
  public void testList() throws Exception {

    String[] args = new String[3];
    final ClassLoader classLoader = getClass().getClassLoader();
    args[0] = "list";
    args[1] = "-in";
    args[2] = classLoader.getResource("mlp/hamiltonian_esc.txt").getFile();
    final PrintStream stdout = System.out;
    final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(myOut));
    Main.main(args);
    final String standardOutput = myOut.toString();
    //assertTrue(standardOutput.contains("magnetic dipole moment"));
    System.setOut(stdout);
    System.out.println(standardOutput);
  }
}

