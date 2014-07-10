package de.tuberlin.dima.schubotz.fse.types;

import eu.stratosphere.api.java.tuple.Tuple3;

/**
 * Tuple storing data extracted from queries.
 * In form of ID,Latex,Keywords. Latex and
 * keywords are strings, with their tokens
 * split by parameter STR_SPLIT.
 */

public class QueryTuple extends Tuple3<String,String,String> {
	private String split;
	
	/**
	 * Constructor for this class. Default 
	 * to "" for all fields.
	 * @param id
	 * @param latex
	 * @param keywords
	 * @param STR_SPLIT
	 */
	public QueryTuple() {
		this.f0 = "";
		this.f1 = "";
		this.f2 = "";
		this.split = "<S>";
	}
	public QueryTuple(String id, String latex, String keywords, String STR_SPLIT) {
		this.f0 = id;
		this.f1 = latex;
		this.f2 = keywords;
		this.split = STR_SPLIT;
	}
	public void setNamedField (fields f, Object value) {
		setField( value, f.ordinal() );
	}
	public Object getNamedField (fields f) {
		return getField( f.ordinal() );
	}	
	public String getID() {
		return this.f0;
	}
	public String getLatex() {
		return this.f1;
	}
	public String getKeywords() {
		return this.f2;
	}
	public void addKeyword (String keyword) {
		if (!this.f2.equals("")) {
			this.f2 = this.f2.concat(split.concat(keyword));
		}else {
			this.f2 = keyword;
		}
	}
	@Override
	public String toString() {
		//Debug purposes
		return this.f0 + "," + this.f1 + "," + this.f2;
	}
	public enum fields {
		queryid,latex,keywords
	}

}
