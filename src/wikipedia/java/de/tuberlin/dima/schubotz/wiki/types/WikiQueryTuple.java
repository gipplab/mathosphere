package de.tuberlin.dima.schubotz.wiki.types;

import eu.stratosphere.api.java.tuple.Tuple4;

/**
 * Stores tuples of {@link WikiQueryTuple#WikiQueryTuple(String,String,String,String)}
 */
@SuppressWarnings("serial")
public class WikiQueryTuple extends Tuple4<String,String,String,String> {
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
	public WikiQueryTuple (String id, String latex, String mml, String pmml) {
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
