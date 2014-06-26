package de.tuberlin.dima.schubotz.fse;

import eu.stratosphere.api.java.tuple.Tuple2;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
// TODO: figure out if there is a better way to create named tuples?

/**
 * Schema according to @url{https://svn.mathweb.org/repos/NTCIR-Math/topics/ntcir11/lib/NTCIR11-results.rnc}
 * qvar = element qvar {for.att & xref.att}
 */
public class QVarTuple extends Tuple2<String, String> {
	public String getQVar () {
		return getField( fields.qvar.ordinal() );
	}

	public void setQVar (String in) {
		setField( in, fields.qvar.ordinal() );
	}

	public String getXRef () {
		return getField( fields.xref.ordinal() );
	}

	public void setXRef (String in) {
		setField( in, fields.qvar.ordinal() );
	}

	public Element getNode () throws ParserConfigurationException {
		Document doc = XMLHelper.getNewDocument();
		Element result = doc.createElement( "qvar" );
		result.setAttribute( "for", getQVar() );
		result.setAttribute( "xref", getXRef() );
		return result;
	}

	@Override
	public String toString () {
		return "<qvar for=\"" + getQVar() + "\" xref=\"" + getXRef() + "\" />";
	}

	public enum fields {
		qvar, xref
	}
}