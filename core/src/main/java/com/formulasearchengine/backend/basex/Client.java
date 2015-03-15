package com.formulasearchengine.backend.basex;

import com.formulasearchengine.mathmlquerygenerator.NtcirPattern;
import com.formulasearchengine.mathmlquerygenerator.XQueryGenerator;
import net.xqj.basex.BaseXXQDataSource;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xquery.*;
import java.io.IOException;
import java.util.List;

/**
 * Created by Moritz on 08.11.2014.
 */
public class Client {
	private Results results = new Results();
	private Results.Run currentRun = results.new Run( "baseX" + System.currentTimeMillis(), "automated" );
	private Results.Run.Result currentResult;
	private Long measurement;

	public Client( List<NtcirPattern> patterns ) {
		for ( NtcirPattern pattern : patterns ) {
			processPattern( pattern );
		}
		results.addRun( currentRun );
	}

	private void processPattern( NtcirPattern pattern ) {
		currentResult = currentRun.new Result( pattern.getNum() );
		basex( pattern.getxQueryExpression() );
		currentRun.addResult( currentResult );
	}

	/**
	 * Connects with the BaseX database, sending the given query and saves the
	 * result in a list
	 */
	public Long basex( String query ) {
		try {
			runQuery( query );
		} catch ( XQException e ) {
			e.printStackTrace();
			return -1L;
		}
		return measurement;
	}

	private int runQuery( String query ) throws XQException {
		int score = 10;
		int rank = 1;
		XQConnection conn = getXqConnection();
		XQPreparedExpression xqpe = conn.prepareExpression( query );
		measurement = System.nanoTime();
		XQResultSequence rs = xqpe.executeQuery();
		measurement = System.nanoTime() - measurement;
		currentResult.setTime( measurement );
		while (rs.next()) {
			currentResult.addHit( rs.getItemAsString( null ), "", score, rank );
			rank++;
		}
		conn.close();
		return rank--;
	}

	private static XQConnection getXqConnection() throws XQException {
		XQDataSource xqs = new BaseXXQDataSource();
		xqs.setProperty( "serverName", "localhost" );
		xqs.setProperty( "port", "1984" );
		xqs.setProperty( "databaseName", "math" );

		return xqs.getConnection( "admin", "admin" );
	}

	public Client() {
	}

	public String getXML() {
		return results.toXML();
	}

	public String getCSV() {
		return results.toCSV();
	}
	
	public String runTexQuery( String tex ){
		TexQueryGenerator t = new TexQueryGenerator();
		String mmlString = t.request( tex );
		try {
			Document doc = XMLHelper.String2Doc( mmlString );
			return runMWSQuery( doc );
		} catch ( ParserConfigurationException | IOException e ) {
			return "Can not parse tex";
		}
	}

	public String runMWSQuery( Document mwsQuery ) {
		XQueryGenerator generator = new XQueryGenerator( mwsQuery );
		generator.setHeader( Benchmark.BASEX_HEADER );
		generator.setFooter( Benchmark.BASEX_FOOTER );
		return runXQuery( generator.toString() );
	}

	public String runXQuery (String query) {
		currentResult = currentRun.new Result( "" );
		try {
			runQuery( query );
			if ( currentResult.size() > 0 ) {
				return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
					+ "<results xmlns=\"http://ntcir-math.nii.ac.jp/\" total=\"" + currentResult.size() + "\">\n"
					+ currentResult.toXML() + "</results>\n";
			} else {
				return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
					+ "<results xmlns=\"http://ntcir-math.nii.ac.jp/\" total=\"0\" />\n";
			}

		} catch ( Exception e ) {
			return "Query :\n" + query + "\n\n failed " + e.getLocalizedMessage();
		}
	}

	public void setShowTime (boolean showTime) {
		this.results.setShowTime(showTime);
	}
}
