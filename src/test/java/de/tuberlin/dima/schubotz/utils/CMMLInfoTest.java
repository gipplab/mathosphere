package de.tuberlin.dima.schubotz.utils;

import de.tuberlin.dima.schubotz.fse.utils.CMMLInfo;
import junit.framework.TestCase;
import org.custommonkey.xmlunit.XMLUnit;

public class CMMLInfoTest extends TestCase {
    String rawTests[] = {"<annotation-xml encoding=\"MathML-Content\" id=\"I1.i2.p1.1.m1.1.cmml\" xref=\"I1.i2.p1.1.m1.1\">\n" +
            "  <apply id=\"I1.i2.p1.1.m1.1.6.cmml\" xref=\"I1.i2.p1.1.m1.1.6\">\n" +
            "    <list id=\"I1.i2.p1.1.m1.1.6.1.cmml\"/>\n" +
            "    <apply id=\"I1.i2.p1.1.m1.1.6.2.cmml\" xref=\"I1.i2.p1.1.m1.1.6.2\">\n" +
            "      <csymbol cd=\"ambiguous\" id=\"I1.i2.p1.1.m1.1.6.2.1.cmml\">subscript</csymbol>\n" +
            "      <ci id=\"I1.i2.p1.1.m1.1.1.cmml\" xref=\"I1.i2.p1.1.m1.1.1\">I</ci>\n" +
            "      <cn id=\"I1.i2.p1.1.m1.1.2.1.cmml\" type=\"integer\" xref=\"I1.i2.p1.1.m1.1.2.1\">1</cn>\n" +
            "    </apply>\n" +
            "    <apply id=\"I1.i2.p1.1.m1.1.6.3.cmml\" xref=\"I1.i2.p1.1.m1.1.6.3\">\n" +
            "      <csymbol cd=\"ambiguous\" id=\"I1.i2.p1.1.m1.1.6.3.1.cmml\">subscript</csymbol>\n" +
            "      <ci id=\"I1.i2.p1.1.m1.1.4.cmml\" xref=\"I1.i2.p1.1.m1.1.4\">I</ci>\n" +
            "      <cn id=\"I1.i2.p1.1.m1.1.5.1.cmml\" type=\"integer\" xref=\"I1.i2.p1.1.m1.1.5.1\">2</cn>\n" +
            "    </apply>\n" +
            "  </apply>\n" +
            "</annotation-xml>\n",
            "<annotation-xml encoding=\"MathML-Content\" id=\"I1.i2.p1.1.m2.1.cmml\" xref=\"I1.i2.p1.1.m2.1\">\n" +
                    "  <apply id=\"I1.i2.p1.1.m2.1.8.cmml\" xref=\"I1.i2.p1.1.m2.1.8\">\n" +
                    "    <eq id=\"I1.i2.p1.1.m2.1.6.cmml\" xref=\"I1.i2.p1.1.m2.1.6\"/>\n" +
                    "    <apply id=\"I1.i2.p1.1.m2.1.8.1.cmml\" xref=\"I1.i2.p1.1.m2.1.8.1\">\n" +
                    "      <intersect id=\"I1.i2.p1.1.m2.1.3.cmml\" xref=\"I1.i2.p1.1.m2.1.3\"/>\n" +
                    "      <apply id=\"I1.i2.p1.1.m2.1.8.1.1.cmml\" xref=\"I1.i2.p1.1.m2.1.8.1.1\">\n" +
                    "        <csymbol cd=\"ambiguous\" id=\"I1.i2.p1.1.m2.1.8.1.1.1.cmml\">subscript</csymbol>\n" +
                    "        <ci id=\"I1.i2.p1.1.m2.1.1.cmml\" xref=\"I1.i2.p1.1.m2.1.1\">I</ci>\n" +
                    "        <cn id=\"I1.i2.p1.1.m2.1.2.1.cmml\" type=\"integer\" xref=\"I1.i2.p1.1.m2.1.2.1\">1</cn>\n" +
                    "      </apply>\n" +
                    "      <apply id=\"I1.i2.p1.1.m2.1.8.1.2.cmml\" xref=\"I1.i2.p1.1.m2.1.8.1.2\">\n" +
                    "        <csymbol cd=\"ambiguous\" id=\"I1.i2.p1.1.m2.1.8.1.2.1.cmml\">subscript</csymbol>\n" +
                    "        <ci id=\"I1.i2.p1.1.m2.1.4.cmml\" xref=\"I1.i2.p1.1.m2.1.4\">I</ci>\n" +
                    "        <cn id=\"I1.i2.p1.1.m2.1.5.1.cmml\" type=\"integer\" xref=\"I1.i2.p1.1.m2.1.5.1\">2</cn>\n" +
                    "      </apply>\n" +
                    "    </apply>\n" +
                    "    <emptyset id=\"I1.i2.p1.1.m2.1.7.cmml\" xref=\"I1.i2.p1.1.m2.1.7\"/>\n" +
                    "  </apply>\n" +
                    "</annotation-xml>\n",
            "<annotation-xml encoding=\"MathML-Content\" id=\"I1.i2.p1.1.m3.1.cmml\" xref=\"I1.i2.p1.1.m3.1\">\n" +
                    "  <apply id=\"I1.i2.p1.1.m3.1.16.cmml\" xref=\"I1.i2.p1.1.m3.1.16\">\n" +
                    "    <eq id=\"I1.i2.p1.1.m3.1.14.cmml\" xref=\"I1.i2.p1.1.m3.1.14\"/>\n" +
                    "    <apply id=\"I1.i2.p1.1.m3.1.16.1.cmml\" xref=\"I1.i2.p1.1.m3.1.16.1\">\n" +
                    "      <interval closure=\"closed\" id=\"I1.i2.p1.1.m3.1.16.1.1.cmml\"/>\n" +
                    "      <apply id=\"I1.i2.p1.1.m3.1.16.1.2.cmml\" xref=\"I1.i2.p1.1.m3.1.16.1.2\">\n" +
                    "        <times id=\"I1.i2.p1.1.m3.1.16.1.2.1.cmml\" xref=\"I1.i2.p1.1.m3.1.16.1.2.1\"/>\n" +
                    "        <ci id=\"I1.i2.p1.1.m3.1.2.cmml\" xref=\"I1.i2.p1.1.m3.1.2\">&#119964;</ci>\n" +
                    "        <apply id=\"I1.i2.p1.1.m3.1.16.1.2.2.cmml\" xref=\"I1.i2.p1.1.m3.1.16.1.2.2\">\n" +
                    "          <csymbol cd=\"ambiguous\" id=\"I1.i2.p1.1.m3.1.16.1.2.2.1.cmml\">subscript</csymbol>\n" +
                    "          <ci id=\"I1.i2.p1.1.m3.1.4.cmml\" xref=\"I1.i2.p1.1.m3.1.4\">I</ci>\n" +
                    "          <cn id=\"I1.i2.p1.1.m3.1.5.1.cmml\" type=\"integer\" xref=\"I1.i2.p1.1.m3.1.5.1\">1</cn>\n" +
                    "        </apply>\n" +
                    "      </apply>\n" +
                    "      <apply id=\"I1.i2.p1.1.m3.1.16.1.3.cmml\" xref=\"I1.i2.p1.1.m3.1.16.1.3\">\n" +
                    "        <times id=\"I1.i2.p1.1.m3.1.16.1.3.1.cmml\" xref=\"I1.i2.p1.1.m3.1.16.1.3.1\"/>\n" +
                    "        <ci id=\"I1.i2.p1.1.m3.1.8.cmml\" xref=\"I1.i2.p1.1.m3.1.8\">&#119964;</ci>\n" +
                    "        <apply id=\"I1.i2.p1.1.m3.1.16.1.3.2.cmml\" xref=\"I1.i2.p1.1.m3.1.16.1.3.2\">\n" +
                    "          <csymbol cd=\"ambiguous\" id=\"I1.i2.p1.1.m3.1.16.1.3.2.1.cmml\">subscript</csymbol>\n" +
                    "          <ci id=\"I1.i2.p1.1.m3.1.10.cmml\" xref=\"I1.i2.p1.1.m3.1.10\">I</ci>\n" +
                    "          <cn id=\"I1.i2.p1.1.m3.1.11.1.cmml\" type=\"integer\" xref=\"I1.i2.p1.1.m3.1.11.1\">2</cn>\n" +
                    "        </apply>\n" +
                    "      </apply>\n" +
                    "    </apply>\n" +
                    "    <cn id=\"I1.i2.p1.1.m3.1.15.cmml\" type=\"integer\" xref=\"I1.i2.p1.1.m3.1.15\">0</cn>\n" +
                    "  </apply>\n" +
                    "</annotation-xml>\n"};
    boolean isEquation[] = {false, true, true};

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testIsEquation() throws Exception {
        int i = 0;
        for (String rawTest : rawTests) {
            CMMLInfo cmmlElement = new CMMLInfo(rawTest);
            assertEquals("Test " + i + " failed", isEquation[i], cmmlElement.isEquation());
            i++;
        }
    }

    public void testToString() throws Exception {
        for (String rawTest : rawTests) {
            CMMLInfo cmmlElement = new CMMLInfo(rawTest);
            // Ignore Windows style line-breaks
            XMLUnit.compareXML(rawTest,cmmlElement.toString());
        }
    }

    public void testAbstract2CDs() throws Exception {
        for (String rawTest : rawTests) {
            CMMLInfo cmmlElement = CMMLInfo.newFromSnippet(rawTest);
            cmmlElement.abstract2CDs();
            System.out.println(cmmlElement);
        }
    }

    public void testStrictCmml1() throws Exception {
        String simpleMathTag = TestUtils.getFileContents("de/tuberlin/dima/schubotz/utils/sample.mml");
        String simpleStrictMathTag = TestUtils.getFileContents("de/tuberlin/dima/schubotz/utils/reference_sample.mml");
        CMMLInfo cmml = new CMMLInfo(simpleMathTag);
        XMLUnit.compareXML(simpleStrictMathTag, cmml.toStrictCmml().toString());
    }
}