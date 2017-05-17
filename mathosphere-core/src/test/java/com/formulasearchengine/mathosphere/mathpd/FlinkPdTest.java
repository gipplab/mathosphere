package com.formulasearchengine.mathosphere.mathpd;

import com.formulasearchengine.mathosphere.mlp.Main;
import com.google.common.base.Throwables;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by Moritz on 12.11.2015.
 */
public class FlinkPdTest {

    /**
     * Some tests can only be executed locally, as we cannot commit the files to git.
     */
    private static final boolean IS_LOCAL = false;

    private static String decodePath(String urlEncodedPath) {
        try {
            return URLDecoder.decode(urlEncodedPath, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw Throwables.propagate(e);
        }
    }

    private String resourcePath(String resourceName) {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(resourceName);
        return decodePath(resource.getFile());
    }

    @Test
    public void testCountTok() throws Exception {
        if (!IS_LOCAL)
            return;
        final File temp;
        temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
        System.out.println(temp);
        String[] args = new String[7];
        args[0] = "pd";
        args[1] = "-in";
        args[2] = resourcePath("com/formulasearchengine/mathosphere/mathpd/test9.xml");
        args[3] = "-ref";
        args[4] = resourcePath("com/formulasearchengine/mathosphere/mathpd/ex1.html");
        args[5] = "-out";
        args[6] = temp.getAbsolutePath();
        final PrintStream stdout = System.out;
        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));
        Main.main(args);
        final String standardOutput = myOut.toString();

        assertTrue(standardOutput.contains("switched to status FINISHED"));
        System.setOut(stdout);
        System.out.println(standardOutput);
    }

    @Test
    public void testTextTok() throws Exception {
        final File temp;
        temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
        System.out.println(temp);
        String[] args = new String[8];
        args[0] = "pd";
        args[1] = "-in";
        args[2] = resourcePath("com/formulasearchengine/mathosphere/mathpd/test9.xml");
        args[3] = "-ref";
        args[4] = resourcePath("com/formulasearchengine/mathosphere/mathpd/ex1.html");
        args[5] = "-out";
        args[6] = temp.getAbsolutePath();
        args[7] = "--text";
        final PrintStream stdout = System.out;
        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));
        Main.main(args);
        final String standardOutput = myOut.toString();

        assertTrue(standardOutput.contains("switched to status FINISHED"));
        System.setOut(stdout);
        System.out.println(standardOutput);
    }



    @Test
    public void testDistances() throws Exception {
        if (!IS_LOCAL)
            return;
        String filename2 = "161214_allpdcases.xml";
        //filename = "161214_somepdcases.xml";
        //filename = "test9.xml";
        //filename = "twice.xhtml";
        String filename1 = "test9.xml";
        //filename2 = filename1;

        final File temp;
        temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
        String[] args = new String[7];
        args[0] = "pd";
        args[1] = "-in";
        args[2] = resourcePath("com/formulasearchengine/mathosphere/mathpd/" + filename1);
        args[3] = "-ref";
        args[4] = resourcePath("com/formulasearchengine/mathosphere/mathpd/" + filename2);
        args[5] = "-out";
        args[6] = temp.getAbsolutePath();
        final PrintStream stdout = System.out;
        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        String[] a2 = args.clone();
        a2[8] = "--preprocess";
        Main.main(a2);
        Main.main(args);

        //ConverterPairCSVToMatrix.main(new String[]{resourcePath("com/formulasearchengine/mathosphere/mathpd/" + filename1)});
        ConverterPairCSVToMatrix.main(new String[]{temp.getAbsolutePath()});
    }

}
