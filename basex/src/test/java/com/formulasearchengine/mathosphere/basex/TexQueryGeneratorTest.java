package com.formulasearchengine.mathosphere.basex;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assume;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class TexQueryGeneratorTest {

	/**
	 * Check if we have a working connection to the xsede server before all unit tests.
	 */
	@Before
	public void checkConnection() {
		TexQueryGenerator gen = new TexQueryGenerator();
		HttpPost httppost = new HttpPost( gen.getLaTeXMLURL() );
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpResponse response;
		try {
			response = httpClient.execute( httppost );
			Assume.assumeTrue( response.getStatusLine().getStatusCode() != 4 );
		}  catch( final IOException e ) {
			Assume.assumeTrue( false );
		}
	}

	@Test
	public void testQuery () throws Exception {
		final TexQueryGenerator t = new TexQueryGenerator();
		final String mml = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\" id=\"p1.m1\" class=\"ltx_Math\" alttext=\"E=mc^{2}\" display=\"inline\">\n" +
			"  <apply>\n" +
			"    <eq/>\n" +
			"    <ci>E</ci>\n" +
			"    <apply>\n" +
			"      <times/>\n" +
			"      <ci>m</ci>\n" +
			"      <apply>\n" +
			"        <csymbol cd=\"ambiguous\">superscript</csymbol>\n" +
			"        <ci>c</ci>\n" +
			"        <cn type=\"integer\">2</cn>\n" +
			"      </apply>\n" +
			"    </apply>\n" +
			"  </apply>\n" +
			"</math>";
		assertEquals( mml, t.request( "E=mc^2" ) );
		assertEquals( 0, t.getOb().get("status_code") );
		assertEquals( "No obvious problems", t.getOb().get( "status" ) );
	}

	@Test
	public void testSen () throws Exception {
		TexQueryGenerator t = new TexQueryGenerator();
		final String withoutTexvc = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\" id=\"p1.m1\" class=\"ltx_Math\" alttext=\"\\sen\" display=\"inline\">\n" +
			"  <mtext>\\sen</mtext>\n" +
			"</math>";
		final String withTexv = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\" id=\"p1.m1\" class=\"ltx_Math\" alttext=\"\\sen\" display=\"inline\">\n" +
			"  <sin/>\n" +
			"</math>";
		List<NameValuePair> p = t.getParams();
		p.remove( new BasicNameValuePair( "preload", "texvc" ) );
		t.setParams( p );

		try {
			t.request( "\\sen" );
		} catch( final IOException expected ) {
			assertEquals( "2", t.getOb().get( "status_code" ) );
			assertEquals( withoutTexvc, t.getOb().get( "result" ) );
			assertEquals( "Tex request to MathML conversion server produced failed response.", expected.getMessage() );
		}

		t = new TexQueryGenerator();
		assertEquals( withTexv, t.request( "\\sen" )  );

	}

	@Test
	public void testEmpty() throws Exception {
		TexQueryGenerator t = new TexQueryGenerator();
		List<NameValuePair> p = t.getParams();
		p.clear();
		t.setParams( p );
		try {
			t.request( "" );
		} catch( final IOException expected ) {
			assertEquals( 3, t.getOb().get( "status_code" ) );
			assertEquals( "", t.getOb().get( "result" ) );
			assertEquals( "Tex request to MathML conversion server produced failed response.", expected.getMessage() );
		}
		p.add( new BasicNameValuePair( "destroy", "LaTeXML" ) );
		t.setParams( p );
		try {
			t.request( "" );
		} catch( final IOException expected ) {
			assertEquals( "Tex request to MathML conversion server produced failed response.", expected.getMessage() );
			assertEquals( 3, t.getOb().get( "status_code" ) );
			assertEquals( "", t.getOb().get( "result" ) );
		}
	}

	@Test
	public void testErrorHandling () throws Exception {
		final TexQueryGenerator t = new TexQueryGenerator();
		t.setLaTeXMLURL( "http://example.com" );
		assertEquals( "http://example.com", t.getLaTeXMLURL() );
		try {
			t.request( "E=mc^2" );
		} catch( final IOException expected ) {
			assertEquals( "com.fasterxml.jackson.core.JsonParseException",
					expected.getClass().getCanonicalName() );
		}

		t.setLaTeXMLURL( "xxy://invalid" );

		try {
			t.request( "E=mc^2" );
		} catch( final IOException expected ) {
			assertEquals( "org.apache.http.client.ClientProtocolException",
				expected.getClass().getCanonicalName() );
		}
	}
}
