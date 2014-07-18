package de.tuberlin.dima.schubotz.wiki.types;

import org.jsoup.nodes.Document;

import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.api.java.tuple.Tuple4;

@SuppressWarnings("serial")
public class WikiTuple extends Tuple4<String,String,Document,Document>{
	public WikiTuple () {
		this.f0 = "nulldoc";
		this.f1 = "";
	}
	
	/**
	 * @param docID
	 * @param latex
	 */
	public WikiTuple (String docID, String latex, Document mml, Document pmml) {
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
