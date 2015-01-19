package com.formulasearchengine.backend.basex;

import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;

public class ClientTest extends TestCase {
	@Test
	@Ignore
	public void testbasicTest() throws Exception {
		(new ServerTest()).testImportData();
		Client c = new Client();
		String res = c.execute( "declare default element namespace \"http://www.w3.org/1998/Math/MathML\";\n" +
			"for $m in //*:expr return \n" +
			"for $x in $m//*:apply\n" +
			"[*[1]/name() = 'divide']\n" +
			"where\n" +
			"fn:count($x/*) = 3\n" +
			"return\n" +
			"<result>{$m/@url}</result>" );
		assertEquals( res.replaceFirst( "runtime=\"\\d+\"","" ), "    <result for=\"NTCIR11-Math-\" >\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.4.5\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"1\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.4.5\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"2\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.5.2\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"3\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.5.17\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"4\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.5.18\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"5\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.5.18\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"6\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.5.19\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"7\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.5.19\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"8\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.5.19\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"9\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.5.20\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"10\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.5.21\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"11\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.5.22\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"12\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.5.23\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"13\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.6.11\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"14\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.6.14\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"15\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.6.15\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"16\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.6.15\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"17\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.6.15\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"18\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.6.15\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"19\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.6.20\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"20\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.7.0\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"21\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.7.1\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"22\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.7.1\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"23\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.7.2\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"24\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.7.2\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"25\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.7.3\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"26\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.7.3\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"27\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.7.4\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"28\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.8.6\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"29\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.8.7\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"30\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.8.21\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"31\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.8.22\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"32\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.8.23\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"33\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.8.33\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"34\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.8.34\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"35\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"math.8.35\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"36\"/>\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"dummy29\"/>\" xref=\"math000000000000.xml\" score=\"10\" rank=\"37\"/>\n" +
			"    </result>\n" );
	}


}