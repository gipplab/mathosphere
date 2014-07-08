package de.tuberlin.dima.schubotz.fse;

import eu.stratosphere.api.java.tuple.Tuple3;
/**
 * Tuple storing data extracted from documents.
 * In form of ID,Latex,Plaintext. Latex and
 * plaintext are strings, with their tokens
 * split by parameter STR_SPLIT.
 */
public class SectionTuple extends Tuple3<String,String,String> {
	private String STR_SPLIT;
	
	/**
	 * Constructor for this class. Default 
	 * to "" for all fields.
	 * @param id
	 * @param latex
	 * @param plaintext
	 */
	public SectionTuple() {
		this.f0 = "";
		this.f1 = "";
		this.f2 = "";
		this.STR_SPLIT = "<S>";
	}
	public SectionTuple(String id, String latex, String plaintext, String STR_SPLIT) {
		this.f0 = id;
		this.f1 = latex;
		this.f2 = plaintext;
		this.STR_SPLIT = STR_SPLIT;
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
	public void addPlaintext (String token) {
		if (!this.f2.equals("")) {
			this.f2 = this.f2.concat(STR_SPLIT + token);
		}else {
			this.f2 = token;
		}
	}
	@Override
	public String toString() {
		//Debug purposes
		return this.f0 + "," + this.f1 + "," + this.f2;
	}
	public enum fields {
		queryid,latex,plaintext
	}

}
