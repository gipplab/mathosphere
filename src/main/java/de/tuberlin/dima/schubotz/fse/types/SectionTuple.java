package de.tuberlin.dima.schubotz.fse.types;

import eu.stratosphere.api.java.tuple.Tuple4;

import java.util.ArrayList;

/**
 * Tuple storing data extracted from documents.
 */
@SuppressWarnings("serial")
public class SectionTuple extends Tuple4<String,String,String,ArrayList<String>> {
	private String STR_SPLIT;
	
	/**
	 * Blank constructor required for Stratosphere execution.
	 * Defaults to "<S>" for str_split. 
	 */
	public SectionTuple() {
		this.f0 = "";
		this.f1 = "";
		this.f2 = "";
        this.f3 = new ArrayList<>();
		this.STR_SPLIT = "<S>";
	}
	/**
	 * @param id
	 * @param latex string containing latex tokens delimited by str_split
	 * @param plaintext string containing word tokens delimited by str_split
	 * @param STR_SPLIT 
	 */
	@SuppressWarnings("hiding")
	public SectionTuple(String id, String latex, String plaintext, String STR_SPLIT, ArrayList<String> CmmlList) {
		this.f0 = id;
		this.f1 = latex;
		this.f2 = plaintext;
        this.f3 = CmmlList;
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
		return this.f0 + "," + this.f1 + "," + this.f2+","+this.f3;
	}
	public enum fields {
        queryId,latex,plaintext,cmmlList
	}

}
