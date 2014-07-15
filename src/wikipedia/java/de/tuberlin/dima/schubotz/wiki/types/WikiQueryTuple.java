package de.tuberlin.dima.schubotz.wiki.types;

import eu.stratosphere.api.java.tuple.Tuple2;

/**
 * Stores tuples of QueryID, Latex
 */
@SuppressWarnings("serial")
public class WikiQueryTuple extends Tuple2<String,String> {
	public WikiQueryTuple () {
		this.f0 = "nullquery";
		this.f1 = "";
	}
	
	public WikiQueryTuple (String id, String latex) {
		this.f0 = id;
		this.f1 = latex;
	}
	
	public String getID() {
		return this.f0;
	}
	
	public String getLatex() {
		return this.f1;
	}
	
	public enum fields {
		id, latex
	}

}
