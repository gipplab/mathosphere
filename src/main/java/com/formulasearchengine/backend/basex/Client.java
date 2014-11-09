package com.formulasearchengine.backend.basex;

import com.formulasearchengine.mathmlquerygenerator.NtcirPattern;
import net.xqj.basex.BaseXXQDataSource;

import javax.xml.xquery.*;
import java.util.List;

/**
 * Created by Moritz on 08.11.2014.
 */
public class Client {
	private Results results = new Results();
	private Results.Run currentRun = results.new Run( "baseX"+System.currentTimeMillis(), "automated" );
	private Results.Run.Result currentResult;
	public String getXML (){
		return results.toXML();
	}
	public String getCSV (){
		return results.toCSV();
	}
	public Client (List<NtcirPattern> patterns) {
		for ( NtcirPattern pattern : patterns ) {
			processPattern( pattern );
		}
		results.addRun( currentRun );
	}

	private void processPattern (NtcirPattern pattern) {
		currentResult = currentRun.new Result( pattern.getNum() );
		basex( pattern.getxQueryExpression() );
		currentRun.addResult( currentResult );
	}

	/**
	 * Connects with the BaseX database, sending the given query and saves the
	 * result in a list
	 */
	public Long basex(String query) {
		Long measurement = -1L;
		Integer score = 10;
		Integer rank = 1;
		try {
			XQConnection conn = getXqConnection(  );

			XQPreparedExpression xqpe = conn.prepareExpression(query);
			measurement = System.nanoTime();
			XQResultSequence rs = xqpe.executeQuery();
			measurement = System.nanoTime() - measurement;
			currentResult.setTime( measurement );
			while (rs.next()) {
				currentResult.addHit( rs.getItemAsString( null ) , "" , score.toString(), rank.toString()  );
				rank++;
			}

			conn.close();
		} catch (XQException e) {
			e.printStackTrace();
			return measurement;
		}

		return measurement;
	}

	private static XQConnection getXqConnection () throws XQException {
		XQDataSource xqs = new BaseXXQDataSource();
		xqs.setProperty("serverName", "localhost");
		xqs.setProperty("port", "1984");
		xqs.setProperty("databaseName", "math");

		return xqs.getConnection("admin", "admin");
	}
}
