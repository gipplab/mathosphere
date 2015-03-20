package com.formulasearchengine.mathosphere.basex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.formulasearchengine.mathmlquerygenerator.NtcirPattern;
import com.formulasearchengine.mathmlquerygenerator.XQueryGenerator;
import net.xqj.basex.BaseXXQDataSource;
import org.basex.core.cmd.Open;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.iter.Iter;
import org.basex.query.value.item.Item;
import org.w3c.dom.Document;

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
	private boolean useXQ = true;
	private boolean success = false;

	public Client() {}

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
		} catch ( XQException | IOException | QueryException e ) {
			e.printStackTrace();
			return -1L;
		}
		return measurement;
	}

	private int runQuery( String query ) throws XQException, IOException, QueryException {
		int score = 10;
		int rank = 1;
		if ( useXQ ) {
			XQConnection conn = getXqConnection();
			XQPreparedExpression xqpe = conn.prepareExpression( query );
			measurement = System.nanoTime();
			XQResultSequence rs = xqpe.executeQuery();
			measurement = System.nanoTime() - measurement;
			currentResult.setTime( measurement );
			while (rs.next()) {
				currentResult.addHit( rs.getItemAsString( null ).replaceAll( "\r", "" ), "", score, rank );
				rank++;
			}
			conn.close();
		} else {
			measurement = System.nanoTime();
			measurement = System.nanoTime() - measurement;
			currentResult.setTime( measurement );
			new Open("math").execute( Server.context );
			QueryProcessor proc = new QueryProcessor(query, Server.context );
			Iter iter = proc.iter();
			for(Item item; (item = iter.next()) != null;) {
				Object o = item.toJava();
				String s;
				if(o instanceof String){
					s = (String) o;
				} else {
					s = item.toString();
				}
				currentResult.addHit( s, "", score, rank );
				rank++;
			}
		}
		return rank--;
	}

	private static XQConnection getXqConnection() throws XQException {
		XQDataSource xqs = new BaseXXQDataSource();
		xqs.setProperty( "serverName", "localhost" );
		xqs.setProperty( "port", "1984" );
		xqs.setProperty( "databaseName", "math" );

		return xqs.getConnection( "admin", "admin" );
	}

	public String getXML() {
		return results.toXML();
	}

	public String getCSV() {
		return results.toCSV();
	}
	
	public String runTexQuery( String tex ){
		if (tex == null || tex.equals( "" )){
			success = false;
			return "TeX query was empty.";
		}
		TexQueryGenerator t = new TexQueryGenerator();
		String mmlString = t.request( tex );
		if ( mmlString != null ){
			Document doc = XMLHelper.String2Doc( mmlString );
			return runMWSQuery( doc );
		}
		success = false;
		try {
			return t.getErrorMessage() ;
		} catch ( JsonProcessingException ignore ) {
			return "Tex parsing failed. Can not parse error message.";
		}
	}

	public String runMWSQuery( Document mwsQuery ) {
		if ( mwsQuery == null ){
			success = false;
			return "got empty MathML document";
		}
		XQueryGenerator generator = new XQueryGenerator( mwsQuery );
		generator.setHeader( Benchmark.BASEX_HEADER );
		generator.setFooter( Benchmark.BASEX_FOOTER );
		return runXQuery( generator.toString() );
	}

	public String runXQuery (String query) {
		currentResult = currentRun.new Result( "" );
		try {
			runQuery( query );
			success = true;
			if ( currentResult.size() > 0 ) {
				return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
					+ "<results xmlns=\"http://ntcir-math.nii.ac.jp/\" total=\"" + currentResult.size() + "\">\n"
					+ currentResult.toXML() + "</results>\n";
			} else {
				return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
					+ "<results xmlns=\"http://ntcir-math.nii.ac.jp/\" total=\"0\" />\n";
			}
		} catch ( Exception e ) {
			success = false;
			return "Query :\n" + query + "\n\n failed " + e.getLocalizedMessage();
		}
	}

	public void setShowTime (boolean showTime) {
		this.results.setShowTime(showTime);
	}
	public void setUseXQ (boolean useXQ) {
		this.useXQ = useXQ;
	}

	public boolean isSuccess () {
		return success;
	}
}
