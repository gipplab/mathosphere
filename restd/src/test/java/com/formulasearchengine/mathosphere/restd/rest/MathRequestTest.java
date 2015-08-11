package com.formulasearchengine.mathosphere.restd.rest;

import com.formulasearchengine.mathosphere.basex.Client;
import com.formulasearchengine.mathosphere.basex.Server;
import com.formulasearchengine.mathosphere.basex.types.*;
import com.formulasearchengine.mathosphere.restd.domain.Cache;
import com.formulasearchengine.mathosphere.restd.domain.MathRequest;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by jjl4 on 7/28/15.
 */
public class MathRequestTest {
	@Test
	public void invalidLimitOffsetTest() {
		final String query = "<?xml version=\"1.0\"?> <mws:query xmlns:mws=\"http://search.mathweb.org/ns\" xmlns:m=\"http://www.w3.org/1998/Math/MathML\" limitmin=\"0\" answsize=\"30\">     <mws:expr>         <m:apply xml:id=\"p1.1.m1.1.5.cmml\"><m:sin xml:id=\"p1.1.m1.1.1.cmml\"/><mws:qvar>x</mws:qvar></m:apply></mws:expr></mws:query>";

		final Results results = new Results();
		final Run run =new Run( "runtag", 23L, "automated" );
		final Run run2 = new Run( "runtag", 52L, "type" );
		final Result result = new Result( "NTCIR11-Math-1", 21L );
		final Hit hit = new Hit( "id", "filename", 0, 0);
		final Formula formula = new Formula( "id", "queryFormulaID", "filename#formulaID", null );
		final Formula formula2 = new Formula( "id", "queryFormulaID", "filename#formulaID", 0 );
		final Qvar qvar = new Qvar( "queryQvarID", "qvarID" );
		formula.addQvar( qvar );
		hit.addFormula( formula2 );
		hit.addFormula( formula );
		result.addHit( hit );
		run.addResult( result );
		results.addRun( run );
		results.addRun( run2 );

		results.setShowTime( false );

		Cache.cacheResults( query, results );
		final MathRequest request = new MathRequest( query );
		request.setOffset( 3 );
		request.setLimit( 3 );
		Results requestResults = request.run().getResults();
		System.out.println( Client.resultsToXML( requestResults ) );
		Assert.assertTrue( requestResults.getRuns().isEmpty() );
	}

	@Test
	public void limitOffsetCacheTest() throws Exception {
		final String query = "<?xml version=\"1.0\"?> <mws:query xmlns:mws=\"http://search.mathweb.org/ns\" xmlns:m=\"http://www.w3.org/1998/Math/MathML\" limitmin=\"0\" answsize=\"30\">     <mws:expr>         <m:apply xml:id=\"p1.1.m1.1.5.cmml\"><m:sin xml:id=\"p1.1.m1.1.1.cmml\"/><mws:qvar>x</mws:qvar></m:apply></mws:expr></mws:query>";

		String resultsXML = TestUtils.getFileContents( "com/formulasearchengine/mathosphere/restd/rest/results.xml" );

		final Results results = (Results) Client.xmlToClass( resultsXML, Results.class );

		Cache.cacheResults( query, results );

		final MathRequest request = new MathRequest( query );

		request.setRunType( "" );
		request.setQueryID( "NTCIR11-Math-" );
		request.setOffset( 2 );
		request.setLimit( 2 );

		Results requestResults = request.run().getResults();

		String testXML = TestUtils.getFileContents( "com/formulasearchengine/mathosphere/restd/rest/LimitOffsetTest.xml" );

		XMLUnit.setIgnoreWhitespace( true );
		XMLUnit.setIgnoreAttributeOrder( true );
		System.out.println( "Results:\n" + Client.resultsToXML( requestResults ) );
		XMLAssert.assertXMLEqual( testXML, Client.resultsToXML( requestResults ) );
	}
}
