package com.formulasearchengine.mathosphere.basex;

import com.formulasearchengine.mathmlquerygenerator.xmlhelper.XMLHelper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Scanner;

import static org.junit.Assert.*;

public final class ClientTest {

	/**
	 * Checks if there is a working connection to the xsede server, stops the test if there isn't.
	 */
	public void checkConnection() {
		TexQueryGenerator gen = new TexQueryGenerator();
		HttpPost httppost = new HttpPost( gen.getLaTeXMLURL() );
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpResponse response;
		try {
			response = httpClient.execute( httppost );
			if ( response.getStatusLine().getStatusCode() == 4 ) {
				System.out.println( "Ignoring unit test. Xsede connection unstable." );
				Assume.assumeTrue( false );
			}
		}  catch ( final IOException e ) {
			System.out.println( "Ignoring unit test. Xsede connection unstable." );
			Assume.assumeTrue( false );
		}
	}

	@Before
	public void setup() throws Exception {
		BaseXTestSuite.setup();
	}

	@After
	public void shutdownServer() throws Exception {
		Server.getInstance().shutdown();
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
		Document query = XMLHelper.String2Doc( testInput, true );
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
		Document query = XMLHelper.String2Doc( testInput, true );
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
		checkConnection();
		String empty = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
			"<results xmlns=\"http://ntcir-math.nii.ac.jp/\" total=\"0\" />\n";
		Client c = new Client();
		String res = c.runTexQuery( "\\sin(\\cos(x^5))" );
		assertEquals( empty,res );
	}
	@Test
	public void testqVar(){
		checkConnection();
		String empty = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
			"<results xmlns=\"http://ntcir-math.nii.ac.jp/\" total=\"0\" />\n";
		Client c = new Client();
		String res = c.runTexQuery( "a_i)" );
		assertEquals( empty,res );
	}

	@Test
	public void testEmptyTex(){
		checkConnection();
		Client c = new Client();
		String res = c.runTexQuery( "" );
		assertEquals( "TeX query was empty.",res );
		assertFalse( c.isLastQuerySuccess() );
	}

	@Test
	 public void testBadTex(){
		checkConnection();
		Client c = new Client();
		String res = c.runTexQuery( "++23424'ä#öä#ö\\exit" );
		assertTrue( res.startsWith( "Problem during TeX to MathML conversion" ) );
		assertFalse( c.isLastQuerySuccess() );
	}

	@Test
	public void testBadTex2(){
		checkConnection();
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
		Document doc = XMLHelper.String2Doc( getFileContents( "math.4.3.xml" ), true );
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
		final Document doc = XMLHelper.String2Doc(getFileContents( "mws.xml" ), true);
		final String query = "declare default element namespace \"http://www.w3.org/1998/Math/MathML\";\n" +
			"for $m in //*:expr return \n" +
			"for $x in $m//*:apply\n" +
			"[*[1]/name() = 'divide']\n" +
			"where\n" +
			"fn:count($x/*) = 3\n" +
			"let $q := map{\"x\" : (data($x/*[2]/@xml:id)),\"y\" : (data($x/*[3]/@xml:id))}\n" +
			"return\n" +
			Benchmark.NTCIR_FOOTER;
		final Client c = new Client();
		c.setShowTime( false );
		c.setUseXQ( false );
		final String res = c.runQueryNTCIR( query, "f1.0" );

		//Strip all ID values
		final byte[] byteArray = res.getBytes( "UTF-8" );
		final ByteArrayInputStream inputStream = new ByteArrayInputStream( byteArray );
		final XMLEventReader reader = XMLInputFactory.newFactory().createXMLEventReader( inputStream );
		final StringWriter hitWriter = new StringWriter();
		final XMLEventWriter writer = XMLOutputFactory.newInstance().createXMLEventWriter( hitWriter );

		while ( reader.hasNext() ) {
			final XMLEvent curEvent = reader.nextEvent();
			final XMLEvent writeEvent;
			if (curEvent.getEventType() == XMLStreamConstants.START_ELEMENT) {
				final String name = curEvent.asStartElement().getName().getLocalPart();
				if ( "formula".equals( name ) || "hit".equals( name ) ) {
					writeEvent = Client.replaceAttr( curEvent.asStartElement(), "id", "id" );
				} else {
					writeEvent = curEvent;
				}
			} else {
				writeEvent = curEvent;
			}
			writer.add( writeEvent );
		}

		final String expectedXML = getFileContents( "testNTCIRReturnExpected.xml" );
		XMLUnit.setIgnoreWhitespace( true );
		XMLUnit.setIgnoreAttributeOrder( true );
		XMLAssert.assertXMLEqual( expectedXML, hitWriter.toString() );
	}
}
