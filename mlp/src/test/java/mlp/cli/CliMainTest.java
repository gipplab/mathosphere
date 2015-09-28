package mlp.cli;

import junit.framework.Assert;
import mlp.Main;
import org.junit.Test;



import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class CliMainTest {
  @Test
  public void testMain() throws Exception {

    String[] args = new String[3];
    final ClassLoader classLoader = getClass().getClassLoader();
    args[0] = "extract";
    args[1] = "-in";
    args[2] = classLoader.getResource("mlp/schodinger_esc.txt").getFile();
    final PrintStream stdout = System.out;
    final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(myOut));
    Main.main(args);
    final String standardOutput = myOut.toString();
    Assert.assertTrue(standardOutput.contains("generalized coordinates"));
    System.setOut(stdout);
    System.out.println(standardOutput);
  }
}

