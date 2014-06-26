package de.tuberlin.dima.schubotz.fse;

import eu.stratosphere.api.java.functions.GroupReduceFunction;
import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.util.Collector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class SingleQueryOutput extends GroupReduceFunction<HitTuple, Tuple2<String, String>> {
	private static int resultID = 0;
	private static int hitID = 0;
	private static int formulaID = 0;
	private Document doc;
	private DocumentBuilder builder;

	private static String getID (String tag) {
		if ( tag.equals( "result" ) ) {
			return Integer.toString( resultID++ );
		} else if ( tag.equals( "hit" ) ) {
			return Integer.toString( hitID++ );
		} else {
			return Integer.toString( formulaID++ );
		}
	}

	private Node getResult (Document doc, ArrayList<String> resultData) {
		Element result = doc.createElement( "result" );
		result.setAttribute( "id", getID( "result" ) );
		result.setAttribute( "for", resultData.get( 0 ) ); //set query id
		result.setAttribute( "runtime", "FOO" ); //TODO set RUNTIME
		return result;
	}

	private Node getHit (Document doc, HitTuple resultData, String rank) {
		//TODO rename to id, xref, score, rank
		Element hit = doc.createElement( "hit" );
		hit.setAttribute( "id", getID( "hit" ) );
		hit.setAttribute( "f1", resultData.getXRef() ); //set filename
		hit.setAttribute( "f3", resultData.getScore().toString() ); //set f3
		hit.setAttribute( "rank", rank ); //set rank
		return hit;
	}

	private Node getFormula (Document doc, ArrayList<String> formulaData) {
		//formula: formula id, for, f1, f3, qvar(id, for, f1), qvar...?
		//TODO rename to id, for, xref, score
		Element formula = doc.createElement( "formula" );
		formula.setAttribute( "id", formulaData.get( 0 ) ); //set id
		formula.setAttribute( "for", formulaData.get( 1 ) ); //set for
		formula.setAttribute( "f1", formulaData.get( 2 ) ); //set f1
		formula.setAttribute( "f3", formulaData.get( 3 ) ); //set f3
		//TODO: ADD QVAR TREE
		return formula;
	}

	/**
	 * Core method of the reduce function. It is called one per group of elements. If the reducer
	 * is not grouped, than the entire data set is considered one group.
	 *
	 * @param values The iterator returning the group of values to be reduced.
	 * @param out    The collector to emit the returned values.
	 * @throws Exception This method may throw exceptions. Throwing an exception will cause the operation
	 *                   to fail and may trigger recovery.
	 */
	@Override
	public void reduce (Iterator<HitTuple> values, Collector<Tuple2<String, String>> out) throws Exception {
		//takes in HitTuples, outputs f0, documents
		//TODO: Implementing XML output
		Document doc = builder.newDocument();
		Integer rank = 0;
		doc.setXmlStandalone( true );
		Element result = doc.createElement( "result" );
		result.setAttribute( "id", getID( "result" ) );
		result.setAttribute( "for", values.next().f0 ); //set query id
		result.setAttribute( "runtime", "FOO" ); //TODO set RUNTIME
		while ( values.hasNext() ) {
			rank++;
			result.appendChild( getHit( doc, values.next(), rank.toString() ) );
		}
		doc.appendChild( result );
		String documentString = "error printing document";
		try {
//DEBUG OUTPUT
			documentString = XMLHelper.printDocument( doc );
		} catch ( IOException e ) {
			e.printStackTrace();
		} catch ( TransformerException e ) {
			e.printStackTrace();
		}
		out.collect( new Tuple2<String, String>( values.next().f0, documentString ) );
	}

	@Override
	public void open (Configuration parameters) throws Exception {
		//setup XML document builder
		super.open( parameters );
		final DocumentBuilderFactory dbf;
		dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating( true );
		dbf.setNamespaceAware( true );
		dbf.setIgnoringElementContentWhitespace( true );
		builder = dbf.newDocumentBuilder();

	}
}
