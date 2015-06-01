package com.formulasearchengine.mathosphere.basex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.formulasearchengine.mathmlquerygenerator.NtcirPattern;
import com.formulasearchengine.mathmlquerygenerator.XQueryGenerator;
import net.xqj.basex.BaseXXQDataSource;
import org.intellij.lang.annotations.Language;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.xquery.*;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Inputs NtcirPattern queries, runs them through the BaseX search engine, and then outputs results.
 * Created by Moritz on 08.11.2014.
 */
public class Client {
	private static final Pattern CR_PATTERN = Pattern.compile("\r");
	private Results results = new Results();
	private Results.Run currentRun = results.new Run( "baseX" + System.currentTimeMillis(), "automated" );
	private Results.Run.Result currentResult;
	private Long lastQueryDuration;
	private boolean useXQ = true;
	private boolean lastQuerySuccess = false;

	/**
	 * Constructs a new empty Client. Used for running individual queries.
	 */
	public Client() {}

	/**
	 * Constructs a new Client with the given queryset. This constructor will also search all queries immediately.
	 * @param patterns List of NtcirPattern
	 */
	public Client(List<NtcirPattern> patterns) {
		for (final NtcirPattern pattern : patterns) {
			processPattern( pattern );
		}
		results.addRun( currentRun );
	}

	/**
	 * @return Returns results in XML format.
	 */
	public String getXML() {
		return results.toXML();
	}

	/**
	 * @return Returns results in CSV format.
	 */
	public String getCSV() {
		return results.toCSV();
	}

	/**
	 * Setter for whether or not to show time in results.
	 * @param showTime Boolean for showing time or not
	 */
	public void setShowTime (boolean showTime) {
		results.setShowTime(showTime);
	}

	/**
	 * Setter for whether or not to use XQuery expression.
	 * @param useXQ Boolean for using XQuery expressions.
	 */
	public void setUseXQ (boolean useXQ) {
		this.useXQ = useXQ;
	}

	/**
	 * @return Whether or not the last query succeeded.
	 */
	public boolean isLastQuerySuccess() {
		return lastQuerySuccess;
	}

	private void processPattern(NtcirPattern pattern) {
		currentResult = currentRun.new Result( pattern.getNum() );
		basex( pattern.getxQueryExpression() );
		currentRun.addResult( currentResult );
	}

	/**
	 * Wrapper around XQuery search method runQuery() which handles exceptions and returns the length of time
	 * it took to run that query.
	 * @param query Query in XQuery string format.
	 * @return Time it took to run the query.
	 */
	public Long basex(String query) {
		try {
			runQuery( query );
		} catch (final XQException e) {
			e.printStackTrace();
			return -1L;
		}
		return lastQueryDuration;
	}

    private static XQConnection getXqConnection() throws XQException {
		final Server srv = Server.getInstance();
		final XQDataSource xqs = new BaseXXQDataSource();
		xqs.setProperty( "serverName", srv.SERVER_NAME);
		xqs.setProperty( "port", srv.PORT );
		xqs.setProperty( "databaseName", srv.DATABASE_NAME );

		return xqs.getConnection( srv.USER, srv.PASSWORD );
	}

	/**
	 * Connects with the BaseX database, sending the given query as an XQuery query and saves the
	 * result in a list.
	 * @param query Query in XQuery string format.
	 * @return Number of results.
	 * @throws XQException When getXqConnection() fails to connect to the BaseX server, XQJ fails to process the query,
	 * or XQJ fails to execute the query.
	 */
	protected int runQuery(String query) throws XQException {
		int score = 10;
		int rank = 1;
		if ( useXQ ) {
			final XQConnection conn = getXqConnection();
			final XQPreparedExpression xqpe = conn.prepareExpression( query );
			lastQueryDuration = System.nanoTime();
			final XQResultSequence rs = xqpe.executeQuery();
			lastQueryDuration = System.nanoTime() - lastQueryDuration;
			currentResult.setTime( lastQueryDuration );
			while (rs.next()) {
				currentResult.addHit(CR_PATTERN.matcher(rs.getItemAsString(null)).replaceAll(""), "", score, rank );
				rank++;
			}
			conn.close();
		} else {
			//TODO: This does not yet work
/*			measurement = System.nanoTime();
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
			}*/
		}
		return rank-1;
	}

	/**
	 * Calls {@link #runQuery(String)} and wraps the result with the NTCIR XML format.
	 * This adds the result to {@link #currentResult}
	 * @param query XQuery string
	 * @return NTCIR XML formatted result
	 */
	public String runQueryNtcirWrap(String query) {
		currentResult = currentRun.new Result( "" );
		try {
			runQuery( query );
			lastQuerySuccess = true;
			if ( currentResult.size() > 0 ) {
				return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
					+ "<results xmlns=\"http://ntcir-math.nii.ac.jp/\" total=\"" + currentResult.size() + "\">\n"
					+ currentResult.toXML() + "</results>\n";
			} else {
				return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
					+ "<results xmlns=\"http://ntcir-math.nii.ac.jp/\" total=\"0\" />\n";
			}
		} catch (final XQException e) {
			lastQuerySuccess = false;
			return "Query :\n" + query + "\n\n failed " + e.getLocalizedMessage();
		}
	}

	/**
	 * Calls {@link #runQueryNtcirWrap(String)} given a MathML MathWebSearch XML document query
	 * @param mwsQuery Document in MathML MathWebSearch query format
	 * @return NTCIR XML formatted result
	 */
	public String runMWSQuery( Document mwsQuery ) {
		if ( mwsQuery == null ){
			lastQuerySuccess = false;
			return "got empty MathML document";
		}
		final XQueryGenerator generator = new XQueryGenerator( mwsQuery );
		generator.setHeader( Benchmark.BASEX_HEADER );
		generator.setFooter( Benchmark.BASEX_FOOTER );
		return runQueryNtcirWrap(generator.toString());
	}

	/**
	 * Calls {@link #runMWSQuery(Document)} given a Tex string.
	 * Converts the Tex string into MathML MathWebSearch XML document query format and then runs the search.
	 * @param tex Tex string
	 * @return NTCIR XML formatted result
	 */
	public String runTexQuery( String tex ){
		if (tex == null || tex.isEmpty()){
			lastQuerySuccess = false;
			return "TeX query was empty.";
		}
		final TexQueryGenerator t = new TexQueryGenerator();
		final String mmlString = t.request(tex);
		if ( mmlString != null ){
			final Document doc = XMLHelper.String2Doc( mmlString );
			return runMWSQuery( doc );
		}
		lastQuerySuccess = false;
		try {
			return t.getErrorMessage() ;
		} catch (final JsonProcessingException ignore ) {
			return "Tex parsing failed. Can not parse error message.";
		}
	}

	/**
	 * Runs a query with no timing or effects on {@link #currentResult}
	 * @param query XQuery string
	 * @return XQResult in string format
	 * @throws XQException
	 */
	static String directXQuery(String query) throws XQException {
		final StringBuilder outputBuilder = new StringBuilder();
		final XQConnection conn = getXqConnection();
		final XQPreparedExpression xqpe = conn.prepareExpression( query );
		final XQResultSequence rs = xqpe.executeQuery();
		while (rs.next()) {
			outputBuilder.append(CR_PATTERN.matcher(rs.getItemAsString(null)).replaceAll(""));
		}
		conn.close();
		return outputBuilder.toString();
	}

	/**
	 * Returns XQuery expression for matching formulae based on revision number
	 * @param rev Revision number to match
	 * @return XQuery expression
	 */
	private String getRevFormula( int rev ) {
		return "expr[matches(@url, '" + rev + "#(.*)')]";
	}

	/**
	 * Shortcut call on {@link #directXQuery(String)} to count the number of formulae with specified revision number
	 * @param rev Revision number to count
	 * @return Number of formulae with specified revision number
	 */
	public int countRevisionFormula(int rev){
		try {
			return Integer.parseInt( directXQuery( "count(//*:" + getRevFormula( rev ) + ")"
			) );
		} catch (final XQException e) {
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * Shortcut call on {@link #directXQuery(String)} to count the total number of formulae
	 * @return Total number of formulae
	 */
	public int countAllFormula(){
		try {
			return Integer.parseInt( directXQuery( "count(./*/*)" ) );
		} catch (final XQException e) {
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * Shortcut call on {@link #directXQuery(String)} to delete all formulae with specified revision number
	 * @param rev Revision number
	 * @return Whether or not this operation succeeded
	 */
	public boolean deleteRevisionFormula(int rev){
		try {
			directXQuery( "delete node //*:"+ getRevFormula( rev ) );
			return countRevisionFormula(rev) == 0;
		} catch (XQException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Inserts the first formula from a harvest XML file into the server database.
	 * @param n Node to start with
	 * @return Whether or not this operation succeeded
	 */
	public boolean updateFormula(Node n){
		try {
            @Language("XQuery") final String xUpdate = "declare namespace mws=\"http://search.mathweb.org/ns\";\n" +
			"declare variable $input external;\n" +
			"for $e in $input/mws:expr\n" +
			"return ( delete node //*[@url=$e/@url], insert node $e into /mws:harvest[1])";
			final XQConnection conn = getXqConnection();
			final XQPreparedExpression xqpe = conn.prepareExpression( xUpdate );
			xqpe.bindNode( new QName( "input" ), n, null );
			xqpe.executeQuery();
			return true;
		} catch (final XQException e ) {
			e.printStackTrace();
			return false;
		}
	}
}
