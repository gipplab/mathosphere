package de.tuberlin.dima.schubotz.fse;

import eu.stratosphere.api.java.tuple.Tuple3;

/**
 * Tuple storing data extracted from queries.
 * In form of ID,Latex,Keywords. Latex and
 * keywords are strings, with their tokens
 * split by MainProgram.STR_SPLIT.
 */

public class QueryTuple extends Tuple3<String,String,String> {
	private String split = MainProgram.STR_SPLIT;
	
	/**
	 * Constructor for this class. Default 
	 * to "" for all fields.
	 * @param id
	 * @param latex
	 * @param keywords
	 */
	public QueryTuple() {
		this.f0 = "";
		this.f1 = "";
		this.f2 = "";
	}
	public QueryTuple(String id, String latex, String keywords) {
		this.f0 = id;
		this.f1 = latex;
		this.f2 = keywords;
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
		this.f2 = this.f2.concat(split.concat(keyword));
	}
	public enum fields {
		queryid,latex,keywords
	}

}
