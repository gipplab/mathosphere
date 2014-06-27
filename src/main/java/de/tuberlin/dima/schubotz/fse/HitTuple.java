package de.tuberlin.dima.schubotz.fse;

import eu.stratosphere.api.java.tuple.Tuple5;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.Random;

/**
 * Schema according to @url{https://svn.mathweb.org/repos/NTCIR-Math/topics/ntcir11/lib/NTCIR11-results.rnc}
 * hit = element hit {id.att & score.att & xref.att & rank.att& formula*}
 */
public class HitTuple extends Tuple5<String, String, String, Double, explicitDataSet<FormulaTuple>> implements Comparable<HitTuple> {
	public String getQueryID () {
		return getField( fields.id.ordinal() );
	}

	public void setQueryID (String queryID) {
		setField( queryID, fields.id.ordinal() );
	}

	public String getXRef () {
		return getField( fields.xref.ordinal() );
	}

	public void setXref (String xref) {
		setField( xref, fields.xref.ordinal() );
	}

	public Double getScore () {
		return getField( fields.score.ordinal() );
	}

	public void setScore (Double score) {
		setField( score, fields.score.ordinal() );
	}

	public explicitDataSet<FormulaTuple> getFormulae(){
		return getField( fields.formula.ordinal() );
	}
	public void setFormulae (explicitDataSet<FormulaTuple> in) {
		setField( in, fields.formula.ordinal() );
	}
	public int compareTo (HitTuple hit) {
		if ( this.getScore() > hit.getScore() ) {
			return 1;
		} else if ( this.getScore() < hit.getScore() ) {
			return -1;
		} else {
			return 0;
		}
	}
	public void addFormula (FormulaTuple formulaTuple) {
		if ( getFormulae() == null ) {
			setFormulae( new explicitDataSet<FormulaTuple>() );
		}
		getFormulae().add( formulaTuple );
	}
	public enum fields {
		id, xref, score, rank, formula
	}
	public Element getNode () throws ParserConfigurationException {
		Document doc = XMLHelper.getNewDocument();
		Random random = new Random();
		Element result = doc.createElement( "hit" );
		result.setAttribute( "id", (new Integer( random.nextInt())).toString() );
		result.setAttribute( "xref", getXRef() );
		result.setAttribute( "score", getScore().toString() );
		result.setAttribute( "rank", "unique-rank-string" );
		for ( FormulaTuple tuple : getFormulae() ) {
			//TODO: Check if this works
			Node importedNode = doc.importNode( tuple.getNode(), true );
			result.appendChild(  importedNode );
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

}
