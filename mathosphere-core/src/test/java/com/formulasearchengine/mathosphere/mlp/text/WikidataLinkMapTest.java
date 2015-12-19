package com.formulasearchengine.mathosphere.mlp.text;

import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static net.sf.ezmorph.test.ArrayAssertions.assertEquals;

/**
 * Created by Moritz on 18.12.2015.
 */
public class WikidataLinkMapTest {

  @Test
  public void testTitle2Data() throws Exception {
    Map<String, String> testcases = new HashMap<>();
    testcases.put("[[complex number|complex]]", "Q11567");
    testcases.put("[[complex conjugate]]", "Q381040");
    testcases.put("[[partial derivative]]", "Q186475");
    testcases.put("[[Schrödinger's equation]]", "Q165498");
    testcases.put("[[Hamiltonian mechanics]]", "Q477921");
    testcases.put("[[Operator (physics)]]", "Q2597952");
    //testcases.put("[[Bra-ket notation]]", "Q59090");
    testcases.put("[[Quantum state]]", "Q230883");
    testcases.put("[[Linear algebra]]", "Q852571");
    testcases.put("[[Conservation of energy]]", "Q11382");
    testcases.put("[[Potential theory]]", "Q1154848");
    testcases.put("[[Many-body problem]]", "Q617316");
    testcases.put("[[Electrostatics]]", "Q26336");
    testcases.put("[[Electric field]]", "Q46221");
    testcases.put("[[Magnetic field]]", "Q11480");
    WikidataLinkMap map = new WikidataLinkMap(getClass().getResource("title2Data.csv").getFile(), false);
    for (Map.Entry<String, String> entry : testcases.entrySet()) {
      assertEquals("Test for " + entry.getKey(),entry.getValue(), map.title2Data(entry.getKey()));
    }

  }

  @Test
  public void testWriteFile() throws Exception {
    final File temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
    WikidataLinkMap map = new WikidataLinkMap(getClass().getResource("title2Data.csv").getFile(), false);
    // WikidataLinkMap map = new WikidataLinkMap("D:/wiki-en-titles-2.csv", false); 325M input --> 3.4G jvm memory
    // WikidataLinkMap map = new WikidataLinkMap("D:/wiki-en-titles-no-dup.csv", true); //306M input --> 3.4G jvm memory
    map.writeFile(temp.getAbsolutePath());
    System.out.println(temp.getAbsolutePath());
  }
}