package de.tuberlin.dima.schubotz.wiki.types;

import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.api.java.tuple.Tuple4;

@SuppressWarnings("serial")
public class WikiTuple extends Tuple4<String,String,String,String>{
	public WikiTuple () {
		this.f0 = "nulldoc";
		this.f1 = "";
	}
	
	/**
	 * @param docID
	 * @param latex
	 */
	public WikiTuple (String docID, String latex, String mml, String pmml) {
		this.f0 = docID;
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
	
	public String getMML() {
		return this.f2;
	}
	
	public String getPMML() {
		return this.f3;
	}
	
	public enum fields {
		id, latex, mml, pmml
	}

}
