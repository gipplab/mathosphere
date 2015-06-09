package com.formulasearchengine.mathosphere.basex;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import static org.junit.Assert.*;

public final class ClientTest {

	@BeforeClass
	public static void setup() throws Exception {
		BaseXTestSuite.setup();
	}

	@Test
	public void basicTest() throws Exception {
		Client c = new Client();
		c.setShowTime( false );
		String res = c.runQueryNtcirWrap("declare default element namespace \"http://www.w3.org/1998/Math/MathML\";\n" +
			"for $m in //*:expr return \n" +
			"for $x in $m//*:apply\n" +
			"[*[1]/name() = 'divide']\n" +
			"where\n" +
			"fn:count($x/*) = 3\n" +
			"return\n" +
			"<result>{$m/@url}</result>");
		assertEquals( "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
			"<results xmlns=\"http://ntcir-math.nii.ac.jp/\" total=\"37\">\n" +
			"    <result for=\"NTCIR11-Math-\">\n" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"4#math.4.5\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"1\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"4#math.4.5\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"2\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"5#math.5.2\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"3\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"5#math.5.17\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"4\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"5#math.5.18\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"5\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"5#math.5.18\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"6\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"5#math.5.19\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"7\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"5#math.5.19\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"8\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"5#math.5.19\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"9\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"5#math.5.20\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"10\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"5#math.5.21\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"11\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"5#math.5.22\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"12\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"5#math.5.23\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"13\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"6#math.6.11\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"14\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"6#math.6.14\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"15\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"6#math.6.15\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"16\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"6#math.6.15\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"17\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"6#math.6.15\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"18\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"6#math.6.15\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"19\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"6#math.6.20\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"20\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"7#math.7.0\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"21\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"7#math.7.1\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"22\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"7#math.7.1\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"23\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"7#math.7.2\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"24\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"7#math.7.2\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"25\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"7#math.7.3\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"26\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"7#math.7.3\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"27\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"7#math.7.4\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"28\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"8#math.8.6\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"29\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"8#math.8.7\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"30\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"8#math.8.21\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"31\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"8#math.8.22\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"32\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"8#math.8.23\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"33\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"8#math.8.33\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"34\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"8#math.8.34\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"35\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"8#math.8.35\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"36\">\n      </hit>" +
			"      <hit id=\"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"dummy29\"/>\" " +
				"xref=\"\" score=\"10\" rank=\"37\">\n      </hit>" +
			"    </result>\n" +
			"</results>\n", res );
	}

	@Test
	public void mwsQuery() throws Exception {
		final String testInput = getFileContents( "dummy29.xml" );
		final String expectedOutput = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
			"<results xmlns=\"http://ntcir-math.nii.ac.jp/\" total=\"1\">\n" +
			"    <result for=\"NTCIR11-Math-\" >\n" +
			"      <hit id=\"dummy29\" xref=\"\" score=\"10\" rank=\"1\">\n" +
			"      </hit>" +
			"    </result>\n"+
			"</results>\n";
		Document query = XMLHelper.String2Doc( testInput );
		Client c = new Client();
		c.setShowTime( true );
		String res = c.runMWSQuery( query );
		assertEquals( expectedOutput, res.replaceAll( "runtime=\"\\d+\"", "" ) );
	}

	@Test
	public void MWS2() throws Exception {
		final String testInput = getFileContents( "mws.xml" );
		final String expectedOutput = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
			"<results xmlns=\"http://ntcir-math.nii.ac.jp/\" total=\"7\">\n" +
			"    <result for=\"NTCIR11-Math-\">\n" +
			"      <hit id=\"8#math.8.13\" xref=\"\" score=\"10\" rank=\"1\">\n" +
			"      </hit>" +
			"      <hit id=\"8#math.8.22\" xref=\"\" score=\"10\" rank=\"2\">\n" +
			"      </hit>" +
			"      <hit id=\"8#math.8.23\" xref=\"\" score=\"10\" rank=\"3\">\n" +
			"      </hit>" +
			"      <hit id=\"8#math.8.25\" xref=\"\" score=\"10\" rank=\"4\">\n" +
			"      </hit>" +
			"      <hit id=\"8#math.8.25\" xref=\"\" score=\"10\" rank=\"5\">\n" +
			"      </hit>" +
			"      <hit id=\"8#math.8.30\" xref=\"\" score=\"10\" rank=\"6\">\n" +
			"      </hit>" +
			"      <hit id=\"8#math.8.32\" xref=\"\" score=\"10\" rank=\"7\">\n" +
			"      </hit>" +
			"    </result>\n"+
			"</results>\n";
		Document query = XMLHelper.String2Doc( testInput );
		Client c = new Client();
		c.setShowTime( false );
		c.setUseXQ( true );
		String res = c.runMWSQuery( query );
		assertEquals( expectedOutput, res );
		c.setUseXQ( false );
		c.runMWSQuery( query );
		assertEquals( expectedOutput, res );
	}
	@Test
	public void testEmpty(){
		String empty = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
			"<results xmlns=\"http://ntcir-math.nii.ac.jp/\" total=\"0\" />\n";
		Client c = new Client();
		String res = c.runTexQuery( "\\sin(\\cos(x^5))" );
		assertEquals( empty,res );
	}
	@Test
	public void testqVar(){
		String empty = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
			"<results xmlns=\"http://ntcir-math.nii.ac.jp/\" total=\"0\" />\n";
		Client c = new Client();
		String res = c.runTexQuery( "a_i)" );
		assertEquals( empty,res );
	}

	@Test
	public void testEmptyTex(){
		Client c = new Client();
		String res = c.runTexQuery( "" );
		assertEquals( "TeX query was empty.",res );
		assertFalse( c.isLastQuerySuccess() );
	}

	@Test
		 public void testBadTex(){
		Client c = new Client();
		String res = c.runTexQuery( "++23424'ä#öä#ö\\exit" );
		assertTrue( res.startsWith( "Problem during TeX to MathML conversion" ) );
		assertFalse( c.isLastQuerySuccess() );
	}

	@Test
	public void testBadTex2(){
		Client c = new Client();
		String res = c.runTexQuery( "\\frac" );
		assertTrue( res.startsWith( "Problem during TeX to MathML conversion" ) );
		assertFalse( c.isLastQuerySuccess() );
	}
	@Test
	public void testEmptyMML(){
		Client c = new Client();
		String res = c.runMWSQuery( null );
		assertEquals( "got empty MathML document", res );
		assertFalse( c.isLastQuerySuccess() );
	}

	@Test
	public void measureBadXQuery(){
		Client c = new Client(  );
		assertEquals( Long.valueOf( -1 ), c.basex( ">invalid<" ) );
		assertTrue( c.runQueryNtcirWrap( ">invalid<" ).startsWith( "Query" ) );
		assertFalse( c.isLastQuerySuccess() );
	}

	@Test
	public void testDelete() throws Exception{
		String cnt4 ="count(//*:expr[matches(@url, 'math\\.4\\.*')])";
		Client c = new Client(  );
		assertEquals( 9, c.countRevisionFormula( 4 ) );
		assertTrue( c.deleteRevisionFormula( 4 ) );
	}
	@Test
	public void testInsert() throws Exception{
		Document doc = XMLHelper.String2Doc( getFileContents( "math.4.3.xml" ) );
		Client c = new Client(  );
		assertEquals( 0, c.countRevisionFormula( 800 ) );
		c.updateFormula( doc.getDocumentElement() );
		assertEquals( 1, c.countRevisionFormula( 800 ) );

	}

	@SuppressWarnings("SameParameterValue")
	static public String getFileContents( String fname ) throws IOException {
		try ( InputStream is = ClientTest.class.getClassLoader().getResourceAsStream( fname ) ) {
			final Scanner s = new Scanner( is, "UTF-8" );
			//Stupid scanner tricks to read the entire file as one token
			s.useDelimiter( "\\A" );
			return s.hasNext() ? s.next() : "";
		}
	}

	@Test
	public void testCountAllFormula() throws Exception {
		Client c = new Client();
		assertEquals( 104,c.countAllFormula());
	}

	@Test
	public void testNTCIRReturn() throws Exception {
		final Document doc = XMLHelper.String2Doc(getFileContents( "mws.xml" ));
		final String query = "declare default element namespace \"http://www.w3.org/1998/Math/MathML\";\n" +
			"for $m in //*:expr return \n" +
			"for $x in $m//*:apply\n" +
			"[*[1]/name() = 'divide']\n" +
			"where\n" +
			"fn:count($x/*) = 3\n" +
			"return\n" +
			Benchmark.NTCIR_FOOTER;
		final Client c = new Client();
		c.setShowTime( false );
		c.setUseXQ( true );
		final String res = c.runQueryNTCIR( query , "f1.0");

		final String expectedRegex =
		"    <result for=\"NTCIR11-Math-\">\n" +
		"      <hit id=\".*?\" xref=\"math/math\\.xml\" score=\"0\" rank=\"1\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.4\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id696\" xref=\"math/math\\.xml\" score=\"0\" rank=\"2\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.4\\.2\\.8\\.1\\.5\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id1020\" xref=\"math/math\\.xml\" score=\"0\" rank=\"3\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.3\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id2176\" xref=\"math/math\\.xml\" score=\"0\" rank=\"4\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.1\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id2334\" xref=\"math/math\\.xml\" score=\"0\" rank=\"5\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.2\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id2363\" xref=\"math/math\\.xml\" score=\"0\" rank=\"6\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.8\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id2537\" xref=\"math/math\\.xml\" score=\"0\" rank=\"7\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.2\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id2566\" xref=\"math/math\\.xml\" score=\"0\" rank=\"8\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.8\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id2514\" xref=\"math/math\\.xml\" score=\"0\" rank=\"9\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.13\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id2661\" xref=\"math/math\\.xml\" score=\"0\" rank=\"10\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.6\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id2724\" xref=\"math/math\\.xml\" score=\"0\" rank=\"11\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.6\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id2775\" xref=\"math/math\\.xml\" score=\"0\" rank=\"12\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.1\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id2826\" xref=\"math/math\\.xml\" score=\"0\" rank=\"13\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.1\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id3571\" xref=\"math/math\\.xml\" score=\"0\" rank=\"14\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.4\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id4075\" xref=\"math/math\\.xml\" score=\"0\" rank=\"15\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.3\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id4303\" xref=\"math/math\\.xml\" score=\"0\" rank=\"16\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.3\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id4378\" xref=\"math/math\\.xml\" score=\"0\" rank=\"17\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.5\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id4391\" xref=\"math/math\\.xml\" score=\"0\" rank=\"18\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.5\\.2\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id4392\" xref=\"math/math\\.xml\" score=\"0\" rank=\"19\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.5\\.3\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id4779\" xref=\"math/math\\.xml\" score=\"0\" rank=\"20\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.11\\.1\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id4973\" xref=\"math/math\\.xml\" score=\"0\" rank=\"21\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.4\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id5373\" xref=\"math/math\\.xml\" score=\"0\" rank=\"22\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.4\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id5571\" xref=\"math/math\\.xml\" score=\"0\" rank=\"23\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.6\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id6049\" xref=\"math/math\\.xml\" score=\"0\" rank=\"24\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.4\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id6256\" xref=\"math/math\\.xml\" score=\"0\" rank=\"25\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.6\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id6714\" xref=\"math/math\\.xml\" score=\"0\" rank=\"26\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.4\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id6925\" xref=\"math/math\\.xml\" score=\"0\" rank=\"27\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.6\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id7345\" xref=\"math/math\\.xml\" score=\"0\" rank=\"28\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.4\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id8115\" xref=\"math/math\\.xml\" score=\"0\" rank=\"29\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.5\\.2\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id8194\" xref=\"math/math\\.xml\" score=\"0\" rank=\"30\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.7\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id8815\" xref=\"math/math\\.xml\" score=\"0\" rank=\"31\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.7\\.2\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id8934\" xref=\"math/math\\.xml\" score=\"0\" rank=\"32\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.11\\.2\\.1\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id9031\" xref=\"math/math\\.xml\" score=\"0\" rank=\"33\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.11\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id9758\" xref=\"math/math\\.xml\" score=\"0\" rank=\"34\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.6\\.2\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id9844\" xref=\"math/math\\.xml\" score=\"0\" rank=\"35\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.8\\.2\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id9925\" xref=\"math/math\\.xml\" score=\"0\" rank=\"36\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.8\\.cmml\" score=\"0\"/>      </hit>      <hit id=\"id10782\" xref=\"math/math\\.xml\" score=\"0\" rank=\"37\">\n" +
		"        <formula id=\".*?\" for=\"f1\\.0\" xref=\"p1\\.1\\.m1\\.1\\.4\\.2\\.cmml\" score=\"0\"/>      </hit>    </result>\n";
		System.out.println(expectedRegex);
		assertTrue(res.matches(expectedRegex));


	}
}