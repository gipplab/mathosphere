package com.formulasearchengine.mathosphere.basex;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import static org.junit.Assert.*;

public class ClientTest {

	@BeforeClass
	public static void setup() throws Exception {
		if ( Server.isEmpty() ){
			( new ServerTest() ).testImportData();
		}
	}

	@Test
	public void basicTest() throws Exception {
		Client c = new Client();
		c.setShowTime( false );
		String res = c.runXQuery( "declare default element namespace \"http://www.w3.org/1998/Math/MathML\";\n" +
			"for $m in //*:expr return \n" +
			"for $x in $m//*:apply\n" +
			"[*[1]/name() = 'divide']\n" +
			"where\n" +
			"fn:count($x/*) = 3\n" +
			"return\n" +
			"<result>{$m/@url}</result>" );
		assertEquals( "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
			"<results xmlns=\"http://ntcir-math.nii.ac.jp/\" total=\"37\">\n" +
			"    <result for=\"NTCIR11-Math-\">\n" +
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
			"    </result>\n" +
			"</results>\n", res );
	}

	@Test
	public void MWSQuery() throws Exception {
		final String testInput = getFileContents( "dummy29.xml" );
		final String expectedOutput = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
			"<results xmlns=\"http://ntcir-math.nii.ac.jp/\" total=\"1\">\n" +
			"    <result for=\"NTCIR11-Math-\" >\n" +
			"      <hit id=\"dummy29\" xref=\"math000000000000.xml\" score=\"10\" rank=\"1\"/>\n" +
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
			"      <hit id=\"math.8.13\" xref=\"math000000000000.xml\" score=\"10\" rank=\"1\"/>\n" +
			"      <hit id=\"math.8.22\" xref=\"math000000000000.xml\" score=\"10\" rank=\"2\"/>\n" +
			"      <hit id=\"math.8.23\" xref=\"math000000000000.xml\" score=\"10\" rank=\"3\"/>\n" +
			"      <hit id=\"math.8.25\" xref=\"math000000000000.xml\" score=\"10\" rank=\"4\"/>\n" +
			"      <hit id=\"math.8.25\" xref=\"math000000000000.xml\" score=\"10\" rank=\"5\"/>\n" +
			"      <hit id=\"math.8.30\" xref=\"math000000000000.xml\" score=\"10\" rank=\"6\"/>\n" +
			"      <hit id=\"math.8.32\" xref=\"math000000000000.xml\" score=\"10\" rank=\"7\"/>\n" +
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
	public void testEmptyTex(){
		Client c = new Client();
		String res = c.runTexQuery( "" );
		assertEquals( "TeX query was empty.",res );
		assertFalse( c.isSuccess() );
	}

	@Test
		 public void testBadTex(){
		Client c = new Client();
		String res = c.runTexQuery( "++23424'ä#öä#ö\\exit" );
		assertTrue( res.startsWith( "Problem during TeX to MathML conversion" ) );
		assertFalse( c.isSuccess() );
	}

	@Test
	public void tesBadTex2(){
		Client c = new Client();
		String res = c.runTexQuery( "\\frac" );
		assertTrue( res.startsWith( "Problem during TeX to MathML conversion" ) );
		assertFalse( c.isSuccess() );
	}
	@Test
	public void testEmptyMML(){
		Client c = new Client();
		String res = c.runMWSQuery( null );
		assertEquals( "got empty MathML document",res );
		assertFalse( c.isSuccess() );
	}

	@Test
	public void MeasureBadXQuery(){
		Client c = new Client(  );
		assertEquals( Long.valueOf( -1 ), c.basex( ">invalid<" ) );
		assertTrue( c.runXQuery( ">invalid<" ).startsWith( "Query" ) );
		assertFalse( c.isSuccess() );
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
}