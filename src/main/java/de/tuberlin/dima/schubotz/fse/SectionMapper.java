package de.tuberlin.dima.schubotz.fse;


import eu.stratosphere.api.java.functions.MapFunction;
import eu.stratosphere.configuration.Configuration;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SectionMapper extends MapFunction<String, SectionTuple> {
	/**
	 * The Constant GRP_ID.
	 */
	public final static int GRP_ID = 2;
	/**
	 * The Constant GRP_TEX.
	 */
	public final static int GRP_TEX = 4;
	/**
	 * The Constant mathpattern.
	 */
	final static Pattern mathpattern = Pattern
		.compile( "(<m:math|<math) (?:xmlns=\"http://www.w3.org/1998/Math/MathML\" )?id=\"(.*?)\"( alttext=\"(.*?)\")?(.*?)(</math>|</m:math>)" );
	/**
	 * The Constant FILENAME_INDICATOR.
	 */
	final static String FILENAME_INDICATOR = "Filename";
	final static Pattern filnamePattern = Pattern
		.compile( "<ARXIVFILESPLIT\\\\n" + FILENAME_INDICATOR + "=\"\\./\\d+/(.*?)/\\1_(\\d+)_(\\d+)\\.xhtml\">" );
	//maps queryid_formulaid, formula node
	private Map<String, Node> formulae;
	//maps keyword, keywordid
	private Map<String, String> keywords;

	/**
	 * The Constant FILENAME_INDICATOR.
	 */

	public static String getPlainText (InputStream is) throws IOException, MalformedURLException {
		//Using Jericho HTMLParser
		Source source = new Source( is );
		source.fullSequentialParse();
		TextExtractor textExtractor = new TextExtractor( source );
		return textExtractor.setIncludeAttributes( false ).toString();
	}


	public String observationsFromMml (Node mml) {
		return "";

	}

	@Override
	public void open (Configuration parameters) throws Exception {
		//Setup formulae, keywords from queries
		formulae = new HashMap<>();
		keywords = new HashMap<>();
		Collection<Query> queries = getRuntimeContext().getBroadcastVariable( "Queries" );
		for ( Query query : queries ) {
			for ( Map.Entry<String, String> formula : query.formulae.entrySet() ) {
				Node node = XMLHelper.String2Doc( formula.getValue(), false );
				formulae.put( query.name + formula.getKey(), node );
			}
			for ( Map.Entry<String, String> keyword : query.keywords.entrySet() ) {
				String[] tokens = keyword.getValue().toLowerCase().split( "\\W+" ); //What does this match?
				Integer i = 0;
				for ( String token : tokens ) {
					i++;
					keywords.put( token, query.name + keyword.getKey() + i.toString() );
				}
			}
		}

		super.open( parameters );
	}

	/**
	 * The core method of the MapFunction. Takes an element from the input data set and transforms
	 * it into another element.
	 *
	 * @param value The input value.
	 * @return The value produced by the map function from the input value.
	 * @throws Exception This method may throw exceptions. Throwing an exception will cause the operation
	 *                   to fail and may trigger recovery.
	 */
	@Override
	public SectionTuple map (String value) throws Exception {
		SectionTuple sectionTuple = new SectionTuple();
		//Split into lines 0: ARXIVFILENAME, 1: HTML
		String[] lines = value.trim().split( "\\n", 2 );
		if ( lines.length < 2 )
			return null;
		sectionTuple.setNamedField( SectionTuple.fields.name, getDocName( lines[0] ) );
		/* Formula counter */
		sectionTuple.setNamedField( SectionTuple.fields.keywords, extractKeywords( lines[1] ) );
		sectionTuple.setNamedField( SectionTuple.fields.formulae, getFormulaeHits( lines[1] ) );


		return sectionTuple;
	}

	private explicitDataSet<ResultTuple> getFormulaeHits (String line) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
		explicitDataSet<ResultTuple> formulaTuples = new explicitDataSet<>();
		Document doc = XMLHelper.String2Doc( line, false );
		NodeList MathMLElements = XMLHelper.getElementsB( doc, "//math" );
		for ( int i = 0; i < MathMLElements.getLength(); i++ ) {
			for ( Map.Entry<String, Node> entry : formulae.entrySet() ) {
				Map<String, Node> qvars = null;
				if ( XMLHelper.compareNode( entry.getValue(), MathMLElements.item( i ), true, qvars ) ) {
					ResultTuple resultTuple = new ResultTuple();
					HitTuple hitTuple = new HitTuple();
					if ( qvars != null ) {
						explicitDataSet<QVarTuple> qvarDataSet = new explicitDataSet<>();
						for ( Map.Entry<String, Node> nodeEntry : qvars.entrySet() ) {
							QVarTuple tuple = new QVarTuple();
							tuple.setQVar( nodeEntry.getKey() );
							tuple.setXRef( nodeEntry.getValue().getAttributes().getNamedItem( "id" ).getNodeValue() );
							qvarDataSet.add( tuple );
						}
					}
					hitTuple.setQueryID( entry.getKey() );
					hitTuple.setScore( 100. );
					resultTuple.addHit( hitTuple );
					formulaTuples.add( resultTuple );
				}
			}
		}
		return formulaTuples;
	}

	private explicitDataSet<KeyWordTuple> extractKeywords (String line) {
		String plainText = Jsoup.parse( line ).text();
		String[] tokens = plainText.toLowerCase().split( "\\W+" );
		Integer j = 0;
		for ( String token : tokens ) {
			j++;
			if ( keywords.containsKey( token ) ) {
				System.out.println( "match for keyword " + j.toString() + token + keywords.get( token ) );
			}
		}
		return null;
	}

	private SectionNameTuple getDocName (String line) {
		Matcher matcher = filnamePattern.matcher( line );
		if ( matcher.find() ) {
			return new SectionNameTuple(
				matcher.group( 1 ),
				Integer.parseInt( matcher.group( 2 ) ),
				Integer.parseInt( matcher.group( 3 ) )
			);
		} else {
			return null;
		}

	}
}
