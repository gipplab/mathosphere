package com.formulasearchengine.mathosphere.mlp.performance;

import com.google.common.base.Throwables;

import com.formulasearchengine.mathosphere.mlp.Main;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.output.TeeOutputStream;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Moritz on 27.12.2015.
 */
public class IdentifierExtraction {

  @SuppressWarnings("SameParameterValue")
  static public String getFileContents( String fname ) throws IOException {
    try ( InputStream is =IdentifierExtraction.class.getClassLoader().getResourceAsStream( fname ) ) {
      final Scanner s = new Scanner( is, "UTF-8" );
      //Stupid scanner tricks to read the entire file as one token
      s.useDelimiter( "\\A" );
      return s.hasNext() ? s.next() : "";
    }
  }

  private File getResources(String resourceName ) {
    URL url = getClass().getClassLoader().getResource( resourceName );
    File dir = null;
    try {
      //IntelliJ would accept if (url != null) { ... } else { throw new NullPointerException(); }
      //but I don't like that
      //noinspection ConstantConditions
      dir = new File( url.toURI() );
    } catch ( Exception e ) {
      fail( "Cannot open test resource folder." );
    }
    return dir;
  }

  private void runTestCollection( String resourceName ) throws Exception {
    runTestCollection( getResources( resourceName ) );
  }

  private void runTestCollection( File dir ) throws Exception {
    String queryString = null;
    String reference = null;
    Document query = null;
    for ( File nextFile : dir.listFiles() ) {
      if ( nextFile.getName().endsWith( "_wiki.txt" ) ) {
        File resultPath = new File( nextFile.getAbsolutePath().replace( "_wiki.txt", "_gold.csv" ) );
        File expectationPath = new File( nextFile.getAbsolutePath().replace( "_wiki.txt", "_exp.json" ) );
        //try {
          runTest(nextFile,resultPath,expectationPath);
        //} catch ( Exception e ) {
        //  fail( "Cannot load test tuple (" + resultPath + ", ... " + nextFile.getName() + " )" );
        //}
      }
    }
  }

  @Test
  public void runTest() throws Exception {
    runTestCollection( "com/formulasearchengine/mathosphere/mlp/perfromance" );
  }

    private String resoucePath(String resorseName) {
      ClassLoader classLoader = getClass().getClassLoader();
      URL resource = classLoader.getResource(resorseName);
      return decodePath(resource.getFile());
    }

    private static String decodePath(String urlEncodedPath) {
      try {
        return URLDecoder.decode(urlEncodedPath, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        throw Throwables.propagate(e);
      }
    }



    public String runTest(String[] args) throws Exception {
      final PrintStream stdout = System.out;
      final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
      TeeOutputStream tee = new TeeOutputStream(stdout, myOut);
      System.setOut(new PrintStream(tee));
      final long t0 = System.nanoTime();
      Main.main(args);
      final String standardOutput = myOut.toString();
      System.setOut(stdout);
      System.out.println((System.nanoTime() - t0) / 1000000000 + "s");
      return standardOutput;
    }


    private void runTest(File source, File gold, File expectations) throws Exception {
      FileReader in = new FileReader(gold);
      Iterable<CSVRecord> records = CSVFormat.RFC4180.withHeader().parse(in);
      Set<String> expected = new HashSet<>();
      for (CSVRecord record : records) {
        expected.add(record.get("identifier"));
      }
      final byte[] jsonData = Files.readAllBytes(expectations.toPath());
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(new String(jsonData));
      JSONObject identifiers = jsonObject.getJSONObject("identifiers");

      double expPrec = identifiers.getDouble("precision");
      double expRec = identifiers.getDouble("recall");


      String[] args = new String[4];
      args[0] = "list";
      args[1] = "-in";
      args[2] = source.getAbsolutePath();
      args[3] = "--tex";
      final PrintStream stdout = System.out;
      final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
      System.setOut(new PrintStream(myOut));
      Main.main(args);
      final String standardOutput = myOut.toString();
      System.setOut(stdout);
      Set<String> real = new HashSet<>(Arrays.asList(standardOutput.split(System.getProperty("line.separator"))));
      Set<String> tp = new HashSet<>(expected);
      Set<String> fn = new HashSet<>(expected);
      Set<String> fp = new HashSet<>(real);
      fn.removeAll(real);
      fp.removeAll(expected);
      tp.retainAll(real);

      double rec = ((double) tp.size() ) / (tp.size() + fn.size());
      double prec = ((double) tp.size() ) / (tp.size() + fp.size());


      assertThat("precision",prec, Matchers.greaterThan(expPrec));
      assertThat("recall",rec,Matchers.greaterThan(expRec));
    }

}
