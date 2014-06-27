package de.tuberlin.dima.schubotz.fse;

import eu.stratosphere.api.java.tuple.Tuple5;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * Schema according to @url{https://svn.mathweb.org/repos/NTCIR-Math/topics/ntcir11/lib/NTCIR11-results.rnc}
 * formula = element formula {id.att & score.att & xref.att & for.att & qvar*}
 */
public class FormulaTuple extends Tuple5<Integer, String, String, Double, explicitDataSet<QVarTuple>> {
	public explicitDataSet<QVarTuple> getQVar () {
		return getField( fields.qvar.ordinal() );
	}

	public void setQVar (explicitDataSet<QVarTuple> QVar) {
		setField( QVar, fields.qvar.ordinal() );
	}

	public Integer getId () {
		return getField( fields.id.ordinal() );
	}

	public void setId (Integer id) {
		setField( id, fields.id.ordinal() );
	}

	public String getFor () {
		return getField( fields.aFor.ordinal() );
	}

	public void setFor (String aFor) {
		setField( aFor, fields.aFor.ordinal() );
	}

	public String getXRef () {
		return getField( fields.xref.ordinal() );
	}

	public void setXRef (String XRef) {
		setField( XRef, fields.xref.ordinal() );
	}

	public Double getScore () {
		Double score = getField( fields.score.ordinal() );
		if (score == null )
			score =0.;
		return score;
	}

	public void setScore (Double score) {
		setField( score, fields.score.ordinal() );
	}
	public Element getNode () throws ParserConfigurationException {
		Document doc = XMLHelper.getNewDocument();
		Element result = doc.createElement( "formula" );
		result.setAttribute( "id", "1" );
		result.setAttribute( "for", getFor() );
		result.setAttribute( "xref", getXRef() );
		result.setAttribute( "score", getScore().toString() );
		for ( QVarTuple tuple : getQVar() ) {
			//TODO: Check if this works
			result.appendChild(  tuple.getNode() );
		}
		return result;
	}

	@Override
	public String toString () {
		try {
			return XMLHelper.printDocument( getNode() );
		} catch ( IOException e ) {
			e.printStackTrace();
		} catch ( TransformerException e ) {
			e.printStackTrace();
		} catch ( ParserConfigurationException e ) {
			e.printStackTrace();
		}
		return "Invalid XML element";
	}

	public enum fields {
		id, aFor, xref, score, qvar
	}

}
