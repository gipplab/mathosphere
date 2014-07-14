package de.tuberlin.dima.schubotz.wiki.types;

import eu.stratosphere.api.java.tuple.Tuple2;

public class WikiTuple extends Tuple2<String,String>{
	private String split;
	
	public WikiTuple () {
		this.f0 = "nulldoc";
		this.f1 = "";
	}
	
	/**
	 * @param docID
	 * @param latex
	 */
	public WikiTuple (String docID, String latex) {
		this.f0 = docID;
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
