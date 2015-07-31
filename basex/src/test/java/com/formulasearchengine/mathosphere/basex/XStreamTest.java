package com.formulasearchengine.mathosphere.basex;

import com.formulasearchengine.mathosphere.basex.types.*;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;

/**
 * Tests marshalling of types into NTCIR xml format.
 * Created by jjl4 on 6/24/15.
 */
public class XStreamTest {
	@Test
	public void testResultToXML() throws Exception {
		final Result result = new Result( "NTCIR11-Math-1", 21L );
		final Hit hit = new Hit( "id", "filename", null, null );
		final Hit hit2 = new Hit( "id", "filename", 0, 0 );
		final Formula formula = new Formula( "id", "queryFormulaID", "filename#formulaID", 0 );
		final Formula formula2 = new Formula( "id", "queryFormulaID", "filename#formulaID", 0 );
		final Qvar qvar = new Qvar( "queryQvarID", "qvarID" );
		final Qvar qvar2 = new Qvar( "queryQvarID", "qvarID" );
		formula.addQvar( qvar );
		formula.addQvar( qvar2 );
		hit.addFormula( formula );
		hit.addFormula( formula2 );
		result.addHit( hit );
		result.addHit( hit2 );

		result.setShowTime( true );

		XMLUnit.setIgnoreWhitespace( true );
		XMLUnit.setIgnoreAttributeOrder( true );
		XMLAssert.assertXMLEqual( TestUtils.getFileContents( TestUtils.BASEX_RESOURCE_DIR + "testResultToXML.xml" ),
				Client.resultToXML( result ) );

	}

	@Test
	public void testResultsToXML() throws Exception {
		final Results results = new Results();
		final Run run =new Run( "runtag", 23L, "type" );
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

		results.setShowTime( true );

		XMLUnit.setIgnoreWhitespace( true );
		XMLUnit.setIgnoreAttributeOrder( true );
		XMLAssert.assertXMLEqual( TestUtils.getFileContents( TestUtils.BASEX_RESOURCE_DIR + "testResultsToXML.xml" ),
				Client.resultsToXML( results ) );
	}

	@Test
	public void xmlToResult() throws Exception {
		final String file = TestUtils.getFileContents( TestUtils.BASEX_RESOURCE_DIR + "testResultToXML.xml" );
		final Result result = (Result) Client.xmlToClass( file, Result.class );

		result.setShowTime( true );

		System.out.println( "RES:\n" + Client.resultToXML( result ) );

		XMLUnit.setIgnoreWhitespace( true );
		XMLUnit.setIgnoreAttributeOrder( true );
		XMLAssert.assertXMLEqual( file, Client.resultToXML( result ) );
	}

	@Test
	public void xmlToResults() throws Exception {
		final String file = TestUtils.getFileContents( TestUtils.BASEX_RESOURCE_DIR + "testResultsToXML.xml" );
		final Results results = (Results) Client.xmlToClass( file, Results.class );

		results.setShowTime( true );

		XMLUnit.setIgnoreWhitespace( true );
		XMLUnit.setIgnoreAttributeOrder( true );
		XMLAssert.assertXMLEqual( file, Client.resultsToXML( results ) );
	}

	@Test
	public void testResultsShowTime() throws Exception {
		final Results results = new Results();
		final Run run =new Run( "runtag", 23L, "type" );
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

		final String file = TestUtils.getFileContents( TestUtils.BASEX_RESOURCE_DIR + "testResultsShowTime.xml" );
		XMLUnit.setIgnoreWhitespace( true );
		XMLUnit.setIgnoreAttributeOrder( true );
		XMLAssert.assertXMLEqual( file, Client.resultsToXML( results ) );
	}
}
