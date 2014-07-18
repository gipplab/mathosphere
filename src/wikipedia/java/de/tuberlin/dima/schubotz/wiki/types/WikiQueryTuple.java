package de.tuberlin.dima.schubotz.wiki.types;

import org.jsoup.nodes.Document;

import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.api.java.tuple.Tuple4;

/**
 * Stores tuples of {@link WikiQueryTuple#WikiQueryTuple(String,String,Document,Document)}
 */
@SuppressWarnings("serial")
public class WikiQueryTuple extends Tuple4<String,String,Document,Document> {
	public WikiQueryTuple () {
		this.f0 = "nullquery";
		this.f1 = "";
		this.f2 = null;
		this.f3 = null;
	}
	
	/**
	 * @param id
	 * @param latex
	 * @param mml mathML (content)
	 * @param pmml mathML (presentational)
	 */
	public WikiQueryTuple (String id, String latex, Document mml, Document pmml) {
		this.f0 = id;
		this.f1 = latex;
		this.f2 = mml;
		this.f3 = pmml;
	}
	
	public String getID() {
		return this.f0;
	}
	
	public String getLatex() {
		return this.f1;
	}
	
	public Document getMML() {
		return this.f2;
	}
	
	public Document getPMML() {
		return this.f3;
	}
	
	public enum fields {
		id, latex, mml, pmml
	}

}
