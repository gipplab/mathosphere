package com.formulasearchengine.mathosphere.basex;


import com.formulasearchengine.mathmlquerygenerator.NtcirPattern;
import com.formulasearchengine.mathmlquerygenerator.NtcirTopicReader;
import org.apache.commons.cli.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Parses user input and starts benchmark. This class contains the main method
 *
 * @author Tobias Uhlich
 * @author Thanh Phuong Luu
 * @author Moritz Schubotz
 */

public class Benchmark {
	//By default, MathMLQueryGenerator constructs XQueries for a DB2 instance, so we need to change that into basex format
	//Search <mws:expr> elements
	public static final String BASEX_HEADER = "declare default element namespace \"http://www.w3.org/1998/Math/MathML\";\n" +
		"for $m in //*:expr return \n";
	//Return URL of <mws:expr> element containing matches for queries
	public static final String BASEX_FOOTER = " data($m/@url) \n";
	//Return hit as XML with required NTCIR data and highlighting
	public static final String NTCIR_FOOTER =
			"<hit id=\"{generate-id()}\" xref=\"{base-uri($m)}\" score=\"\" rank=\"\"><formula id=\"{generate-id()}\" for=\"\" xref=\"{base-uri($m)}#{data($x/@xml:id)}\">{map:for-each($q,function($k,$v){for $value in $v return <qvar for=\"{$k}\" xref=\"{$value}\"/>})}</formula></hit>";

	private final CommandLine line;

	public Benchmark( CommandLine line ) {
		this.line = line;
	}

	/**
	 * Program entry point
	 */
	public static void main( String[] args ) {
		Options options = new Options();
		Option help = new Option( "help", "print this message" );
		//Option projecthelp = new Option( "projecthelp", "print project help information" );
		//Option version = new Option( "version", "print the version information and exit" );
		//Option quiet = new Option( "quiet", "be extra quiet" );
		//Option verbose = new Option( "verbose", "be extra verbose" );
		//Option debug = new Option( "debug", "print debugging information" );
		Option dataSource = OptionBuilder.withArgName( "file" )
			.hasArg()
			.isRequired()
			.withDescription( "use given file for data source" )
			.withLongOpt( "datasource" )
			.create( "d" );
		Option querySource = OptionBuilder.withArgName( "file" )
			.hasArg()
			.isRequired()
			.withDescription( "use given file for query source" )
			.withLongOpt( "querysource" )
			.create( "q" );
		Option resultSink = OptionBuilder.withArgName( "file" )
			.hasArg()
			.withDescription( "specify file for the output" )
			.withLongOpt( "output" )
			.create( "o" );
		options.addOption( dataSource )
			.addOption( querySource )
			.addOption( resultSink )
			.addOption( help )
			.addOption( "c", "CSV", false, "Print CSV instead of XML output" )
			.addOption( "i", "ignoreLength", false, "Includes matches were the matching is tree is longer" +
				"than the search pattern. For example $x+y+z$ for the pattern $x+y$." );
		CommandLineParser parser = new GnuParser();
		try {
			CommandLine line = parser.parse( options, args );
			if ( line.hasOption( "help" ) ) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "java -jar FILENAME.jar", options );
			} else {
				(new Benchmark( line )).run();
			}

		} catch ( ParseException exp ) {
			System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
			return;
		} catch ( IOException e ) {
			System.err.println( "IO Error: " + e.getMessage() );
			e.printStackTrace();
		} catch ( ParserConfigurationException e ) {
			System.err.println( "Error parsing query file: " + e.getMessage() );
			e.printStackTrace();
		} catch ( SAXException e ) {
			System.err.println( "XML Error in query file: " + e.getMessage() );
			e.printStackTrace();
		} catch ( XPathExpressionException e ) {
			System.err.println( "XPath Error in query file: " + e.getMessage() );
			e.printStackTrace();
		}
	}

	private void run() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
		File f = new File( line.getOptionValue( "datasource" ) );
		Server srv = Server.getInstance();
		srv.startup(f);
		File queries = new File( line.getOptionValue( "querysource" ) );
		final NtcirTopicReader ntcirTopicReader = new NtcirTopicReader( queries );
		ntcirTopicReader.setFooter( BASEX_FOOTER );
		ntcirTopicReader.setHeader( BASEX_HEADER );
		ntcirTopicReader.setRestrictLength( !line.hasOption( "i" ) );
		List<NtcirPattern> patterns = ntcirTopicReader.extractPatterns();
		final Client client = new Client( patterns );
		srv.shutdown();
		srv = null;
		String result;
		if ( line.hasOption( "c" ) ) {
			result = "CSV option has been disabled for now. Use https://github.com/physikerwelt/xstlprocJ/blob/master/test/transform.xsl";
		} else {
			result = client.getXML();
		}
		boolean written = false;
		if ( line.hasOption( "output" ) ) {
			try {
				File dest = new File( line.getOptionValue( "output" ) );
				org.apache.commons.io.FileUtils.writeStringToFile( dest, result );
				written = true;
			} catch ( Exception e ) {
				System.out.println( "Could not print to file" + e.getMessage() );
			}
		}
		if ( !written ) {
			System.out.println( result );
		}
	}

}
