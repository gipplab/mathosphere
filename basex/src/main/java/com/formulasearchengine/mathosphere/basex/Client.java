package com.formulasearchengine.mathosphere.basex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.formulasearchengine.mathmlquerygenerator.NtcirPattern;
import com.formulasearchengine.mathmlquerygenerator.XQueryGenerator;
import com.formulasearchengine.mathmlquerygenerator.xmlhelper.XMLHelper;
import com.formulasearchengine.mathosphere.basex.types.Hit;
import com.formulasearchengine.mathosphere.basex.types.Result;
import com.formulasearchengine.mathosphere.basex.types.Results;
import com.formulasearchengine.mathosphere.basex.types.Run;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
import com.thoughtworks.xstream.io.xml.Xpp3Driver;
import net.xqj.basex.BaseXXQDataSource;
import org.intellij.lang.annotations.Language;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.TransformerException;
import javax.xml.xquery.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Inputs NtcirPattern queries, runs them through the BaseX search engine, and then outputs results.
 * Created by Moritz on 08.11.2014.
 */
public class Client {
	private static final Pattern CR_PATTERN = Pattern.compile("\r");
	private Results results = new Results();
	private Run currentRun = new Run( "baseX" + System.currentTimeMillis(), "automated" );
	private Result currentResult = new Result( "NTCIR11-Math-" );
	private Long lastQueryDuration;
	private boolean useXQ = true;

	private boolean showTime = true;

	public static final String USER = "admin";
	public static final String PASSWORD = "admin";

	/**
	 * Constructs a new empty Client. Used for running individual queries.
	 */
	public Client() {}

	/**
	 * Constructs a new Client with the given queryset. This constructor will also search all queries immediately.
	 * @param patterns List of NtcirPattern
	 */
	public Client(List<NtcirPattern> patterns) throws XQException {
		for (final NtcirPattern pattern : patterns) {
			processPattern( pattern );
		}
		results.addRun( currentRun );
	}

	/**
	 * @return Returns results in XML format.
	 */
	public String getXML() {
		results.setShowTime( showTime );
		return resultsToXML( results );
	}

	/**
	 * @return Returns given Result as XML string, and shows time based on showTime
	 */
	public static String resultToXML( Result result ) {
		//Use custom coder to disable underscore escaping so run_type is properly printed
		final XStream stream = new XStream( new Xpp3Driver( new XmlFriendlyNameCoder( "_-", "_" ) ) );
		if ( !result.getShowTime() ) {
			stream.omitField( Result.class, "ms" );
		}
		stream.processAnnotations( Result.class );
		return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + stream.toXML( result );
	}

	/**
	 * @return Returns given Results as XML string, and shows time based on showTime
	 */
	public static String resultsToXML( Results results ) {
		//Use custom coder to disable underscore escaping so run_type is properly printed
		final XStream stream = new XStream(new Xpp3Driver( new XmlFriendlyNameCoder( "_-", "_" ) ) );
		if ( !results.getShowTime() ) {
			stream.omitField( Run.class, "ms" );
			stream.omitField( Result.class, "ms" );
		}
		stream.processAnnotations( Results.class );
		return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + stream.toXML( results );
	}

	/**
	 * @return the given XML string as an object of the given class. note that this method disables
	 * underscore as an escape character if the class is Results so the attribute "run_type" is printed correctly.
	 */
	public static Object xmlToClass( String xml, Class convertClass ) {
		final XStream stream;
		if ( convertClass.equals( Results.class )) {
			//Use custom coder to disable underscore escaping so run_type is properly printed
			stream = new XStream( new Xpp3Driver( new XmlFriendlyNameCoder( "_-", "_" ) ) );
		} else {
			stream = new XStream();
		}
		stream.processAnnotations( convertClass );
		return stream.fromXML( xml );
	}

	/**
	 * Setter for whether or not to show time in results.
	 * @param showTime Boolean for showing time or not
	 */
	public void setShowTime (boolean showTime) {
		this.showTime = showTime;
		results.setShowTime( showTime );
	}

	/**
	 * Setter for whether or not to use XQuery expression.
	 * @param useXQ Boolean for using XQuery expressions.
	 */
	public void setUseXQ (boolean useXQ) {
		this.useXQ = useXQ;
	}

	private void processPattern(NtcirPattern pattern) throws XQException {
		currentResult = new Result( pattern.getNum() );
		currentResult.setShowTime( showTime );
		basex( pattern.getxQueryExpression() );
		currentRun.addResult( currentResult );
	}

	/**
	 * Wrapper around XQuery search method runQueryBaseXSimple() which handles exceptions and returns the length of time
	 * it took to run that query.
	 * @param query Query in XQuery string format.
	 * @return Time it took to run the query.
	 */
	public Long basex(String query) throws XQException {
		runQueryBaseXSimple( query );
		return lastQueryDuration;
	}

    private static XQConnection getXqConnection() throws XQException {
		final Server srv = Server.getInstance();
		final XQDataSource xqs = new BaseXXQDataSource();
		//Other properties: description, logLevel, loginTimeout, readOnly
		xqs.setProperty( "serverName", Server.SERVER_NAME);
		xqs.setProperty( "port", String.valueOf(Server.PORT) );
		xqs.setProperty( "databaseName", Server.DATABASE_NAME );
		xqs.setProperty( "user", USER );
		xqs.setProperty( "password", PASSWORD );

		return xqs.getConnection( USER, PASSWORD );
	}

	//Alternative API that enables XQuery v3.1
	private static BaseXClient getBaseXClient() throws IOException {
		final Server srv = Server.getInstance();
		final BaseXClient session = new BaseXClient(Server.SERVER_NAME, Server.PORT, USER, PASSWORD);
		session.execute("OPEN " + Server.DATABASE_NAME);
		return session;
	}

	/**
	 * Connects with the BaseX database, sending the given query as an XQuery query and saves the
	 * result in the currentResult list. Assumes NTCIR_FOOTER is used as the result return type.
	 * @param query Query in XQuery string format.
	 * @param queryID ID number to mark this query (required for NTCIR search highlight format)
	 * @return Result in NTCIR_FOOTER XML format (not in full NTCIR format)
	 * @throws XQException When getXqConnection() falis to connect to the BaseX server, XQJ fails to process the query,
	 * or XQJ fails to execute the query.
	 * @throws XMLStreamException When the output fails to parse as XML
	 * @throws IOException When the client fails to open properly
	 * @throws TransformerException When the XML reader/writers fail
	 */
	protected Result runQueryNTCIR( String query, String queryID )
			throws XQException, XMLStreamException, IOException, TransformerException, java.io.UnsupportedEncodingException {
		int score = 0;
		int rank = 1;
		if ( useXQ ) {
			return null;
		} else {
			final BaseXClient session = getBaseXClient();
			try {
				lastQueryDuration = System.nanoTime();
				final BaseXClient.Query querySession = session.query( query );
				lastQueryDuration = System.nanoTime() - lastQueryDuration;
				currentResult.setTime( lastQueryDuration );
				currentResult.setShowTime( showTime );

				while ( querySession.more() ) {
					final String result = querySession.next();
					final byte[] byteArray = result.getBytes( "UTF-8" );
					final ByteArrayInputStream inputStream = new ByteArrayInputStream( byteArray );
					final XMLEventReader reader = XMLInputFactory.newFactory().createXMLEventReader( inputStream );
					final StringWriter hitWriter = new StringWriter();
					final XMLEventWriter writer = XMLOutputFactory.newInstance().createXMLEventWriter( hitWriter );

					while ( reader.hasNext() ) {
						final XMLEvent curEvent = reader.nextEvent();
						switch ( curEvent.getEventType() ) {
							case XMLStreamConstants.START_ELEMENT:
								if ( "formula".equals( curEvent.asStartElement().getName().getLocalPart() ) ) {
									writer.add( replaceAttr( curEvent.asStartElement(), "for", queryID ) );
								} else {
									writer.add( curEvent );
								}
								break;
							case XMLStreamConstants.START_DOCUMENT:
								//do nothing
								break;
							default:
								writer.add( curEvent );
								break;
						}
					}
					currentResult.addHit( (Hit) xmlToClass( hitWriter.toString(), Hit.class ) );
				}
			} finally {
				session.close();
			}
			return currentResult;
		}
	}

	/**
	 * @return Returns new StartElement with replaced value for given attribute
	 */
	public static StartElement replaceAttr( StartElement event, String attribute, String value ) {
		final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		final Iterator<Attribute> attributeIterator = event.getAttributes();
		final List<Attribute> attrs = new ArrayList<>();
		while ( attributeIterator.hasNext() ) {
			final Attribute curAttr = attributeIterator.next();
			if ( attribute.equals( curAttr.getName().getLocalPart() ) ) {
				attrs.add( eventFactory.createAttribute( new QName( attribute ), value ) );
			} else {
				attrs.add( curAttr );
			}
		}
		return eventFactory.createStartElement( new QName( event.getName().getLocalPart() ), attrs.iterator(), event.getNamespaces() );
	}

	/**
	 * Connects with the BaseX database, sending the given query as an XQuery query and saves the
	 * result in the currentResult list. Assumes BASEX_FOOTER is used as the result return type.
	 * @param query Query in XQuery string format.
	 * @return Number of results.
	 * @throws XQException When getXqConnection() fails to connect to the BaseX server, XQJ fails to process the query,
	 * or XQJ fails to execute the query.
	 */
	protected int runQueryBaseXSimple( String query ) throws XQException {
		int score = 10;
		int rank = 1;
		if ( useXQ ) {
			final XQConnection conn = getXqConnection();
			try {
				final XQPreparedExpression xqpe = conn.prepareExpression( query );
				lastQueryDuration = System.nanoTime();
				final XQResultSequence rs = xqpe.executeQuery();
				lastQueryDuration = System.nanoTime() - lastQueryDuration;
				currentResult.setTime( lastQueryDuration );
				currentResult.setShowTime( showTime );
				while ( rs.next() ) {
					final String result = rs.getItemAsString( null );
					currentResult.addHit( new Hit( CR_PATTERN.matcher( result ).replaceAll( "" ), "", score, rank ) );
					rank++;
				}
			} finally {
				conn.close();
			}
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
	 * Calls {@link #runQueryBaseXSimple(String)} and wraps the result with the NTCIR XML format.
	 * This adds the result to {@link #currentResult}
	 * @param query XQuery string
	 * @throws XQException when the server xq connection fails
	 * @return NTCIR XML formatted result
	 */
	public Results runQueryNtcirWrap( String query ) throws XQException {
		currentResult = new Result( "NTCIR11-Math-");
		currentResult.setShowTime( showTime );
		runQueryBaseXSimple( query );
		final Results resultsFrame = new Results();
		resultsFrame.setShowTime( showTime );
		if ( currentResult.getNumHits() != 0 ) {
			final Run run = new Run( "", "" );
			run.setShowTime( showTime );
			run.addResult( currentResult );
			resultsFrame.addRun( run );
		}
		return resultsFrame;
	}

	/**
	 * Calls {@link #runQueryNtcirWrap(String)} given a MathML MathWebSearch XML document query
	 * @param mwsQuery Document in MathML MathWebSearch query format
	 * @throws XQException when the server xq connection fails
	 * @return NTCIR XML formatted result
	 */
	public Results runMWSQuery( Document mwsQuery ) throws XQException {
		if ( mwsQuery == null ){
			throw new IllegalArgumentException( "Got empty MathML document" );
		}
		final XQueryGenerator generator = new XQueryGenerator( mwsQuery );
		generator.setHeader( Benchmark.BASEX_HEADER );
		generator.setFooter( Benchmark.BASEX_FOOTER );
		generator.setAddQvarMap( false );
		return runQueryNtcirWrap(generator.toString());
	}

	/**
	 * Calls {@link #runMWSQuery(Document)} given a Tex string.
	 * Converts the Tex string into MathML MathWebSearch XML document query format and then runs the search.
	 * @param tex Tex string
	 *
	 * @throws XQException when the server xq connection fails
	 * @throws IOException when the tex to MathML conversion fails
	 * @return NTCIR XML formatted result
	 */
	public Results runTexQuery( String tex ) throws IOException, XQException {
		if (tex == null || tex.isEmpty()){
			throw new IllegalArgumentException( "Got empty TeX query" );
		}
		final TexQueryGenerator t = new TexQueryGenerator();
		final String mmlString = t.request(tex);
		final Document doc = XMLHelper.String2Doc( mmlString, true );
		return runMWSQuery( doc );
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
		try {
			final XQPreparedExpression xqpe = conn.prepareExpression( query );
			final XQResultSequence rs = xqpe.executeQuery();
			while ( rs.next() ) {
				outputBuilder.append( CR_PATTERN.matcher( rs.getItemAsString( null ) ).replaceAll( "" ) );
			}
		} finally {
			conn.close();
		}
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
			try {
				final XQPreparedExpression xqpe = conn.prepareExpression( xUpdate );
				xqpe.bindNode( new QName( "input" ), n, null );
				xqpe.executeQuery();
			} finally {
				conn.close();
			}
			return true;
		} catch (final XQException e ) {
			e.printStackTrace();
			return false;
		}
	}
}
