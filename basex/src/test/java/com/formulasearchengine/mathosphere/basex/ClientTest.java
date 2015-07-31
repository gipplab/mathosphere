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
import javax.xml.xquery.XQException;
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
		String res = Client.resultsToXML( c.runQueryNtcirWrap("declare default element namespace \"http://www.w3.org/1998/Math/MathML\";\n" +
			"for $m in //*:expr return \n" +
			"for $x in $m//*:apply\n" +
			"[*[1]/name() = 'divide']\n" +
			"where\n" +
			"fn:count($x/*) = 3\n" +
			"return\n" +
			"<result>{$m/@url}</result>") );

		assertEquals(TestUtils.getFileContents( TestUtils.BASEX_RESOURCE_DIR + "testClientBasic.xml" ), res );
	}

	@Test
	public void mwsQuery() throws Exception {
		final String testInput = getFileContents( "dummy29.xml" );
		final String expectedOutput = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
			"<results xmlns=\"http://ntcir-math.nii.ac.jp/\">\n" +
			"  <run runtag=\"\" run_type=\"\">\n" +
			"    <result for=\"NTCIR11-Math-\">\n" +
			"      <hit id=\"dummy29\" xref=\"\" score=\"10\" rank=\"1\"/>\n" +
			"    </result>\n"+
			"  </run>\n" +
			"</results>";
		Document query = XMLHelper.String2Doc( testInput, true );
		Client c = new Client();
		c.setShowTime( false );
		String res = Client.resultsToXML( c.runMWSQuery( query ) );
		assertEquals( expectedOutput, res );
	}

	@Test
	public void MWS2() throws Exception {
		final String testInput = getFileContents( "mws.xml" );
		final String expectedOutput = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
			"<results xmlns=\"http://ntcir-math.nii.ac.jp/\">\n" +
			"  <run runtag=\"\" run_type=\"\">\n" +
			"    <result for=\"NTCIR11-Math-\">\n" +
			"      <hit id=\"8#math.8.13\" xref=\"\" score=\"10\" rank=\"1\"/>\n" +
			"      <hit id=\"8#math.8.22\" xref=\"\" score=\"10\" rank=\"2\"/>\n" +
			"      <hit id=\"8#math.8.23\" xref=\"\" score=\"10\" rank=\"3\"/>\n" +
			"      <hit id=\"8#math.8.25\" xref=\"\" score=\"10\" rank=\"4\"/>\n" +
			"      <hit id=\"8#math.8.25\" xref=\"\" score=\"10\" rank=\"5\"/>\n" +
			"      <hit id=\"8#math.8.30\" xref=\"\" score=\"10\" rank=\"6\"/>\n" +
			"      <hit id=\"8#math.8.32\" xref=\"\" score=\"10\" rank=\"7\"/>\n" +
			"    </result>\n"+
			"  </run>\n" +
			"</results>";
		Document query = XMLHelper.String2Doc( testInput, true );
		Client c = new Client();
		c.setShowTime( false );
		c.setUseXQ( true );
		String res = c.resultsToXML( c.runMWSQuery( query ) );
		assertEquals( expectedOutput, res );
		c.setUseXQ( false );
		c.runMWSQuery( query );
		assertEquals( expectedOutput, res );
	}
	@Test
	public void testEmpty() throws Exception {
		checkConnection();
		String empty = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
			"<results xmlns=\"http://ntcir-math.nii.ac.jp/\"/>";
		Client c = new Client();
		String res = c.resultsToXML( c.runTexQuery( "\\sin(\\cos(x^5))" ) );
		assertEquals( empty,res );
	}
	@Test
	public void testqVar() throws Exception {
		checkConnection();
		String empty = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
			"<results xmlns=\"http://ntcir-math.nii.ac.jp/\"/>";
		Client c = new Client();
		String res = c.resultsToXML( c.runTexQuery( "a_i)" ) );
		assertEquals( empty,res );
	}

	@Test
	public void testEmptyTex() throws Exception {
		checkConnection();
		Client c = new Client();
		try {
			String res = c.resultsToXML( c.runTexQuery( "" ) );
		} catch ( final IllegalArgumentException expected ) {
			assertEquals( "Got empty TeX query", expected.getMessage());
		}
	}

	@Test
	 public void testBadTex() throws Exception {
		checkConnection();
		Client c = new Client();
		try {
			String res = c.resultsToXML( c.runTexQuery( "++23424'ä#öä#ö\\exit" ) );
		} catch ( final IOException expected ) {
			assertTrue( expected.getMessage().startsWith( "Unable to process MathML conversion server response to Tex request." ) );
		}
	}

	@Test
	public void testBadTex2() throws Exception {
		checkConnection();
		Client c = new Client();
		try {
			String res = c.resultsToXML( c.runTexQuery( "\\frac" ) );
		} catch ( final IOException expected ) {
			assertTrue( expected.getMessage().startsWith( "Unable to process MathML conversion server response to Tex request." ) );
		}
	}
	@Test
	public void testEmptyMML() throws Exception {
		Client c = new Client();
		try {
			String res = c.resultsToXML( c.runMWSQuery( null ) );
		} catch ( final IllegalArgumentException expected ) {
			assertEquals( "Got empty MathML document", expected.getMessage() );
		}
	}

	@Test
	public void measureBadXQuery(){
		Client c = new Client(  );
		try {
			c.basex( ">invalid<" );
		} catch ( final XQException expected ) {
			assertEquals( "Invalid XQuery syntax, syntax does not pass static validation.", expected.getMessage() );
		}
		try {
			c.runQueryNtcirWrap( ">invalid<" );
		} catch ( final XQException expected ) {
			assertEquals( "Invalid XQuery syntax, syntax does not pass static validation.", expected.getMessage() );
		}
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
		final String res = c.resultToXML( c.runQueryNTCIR( query, "f1.0" ) );

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
