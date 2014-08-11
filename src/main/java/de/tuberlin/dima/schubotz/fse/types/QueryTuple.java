package de.tuberlin.dima.schubotz.fse.types;


import eu.stratosphere.api.java.tuple.Tuple4;

/**
 * Tuple storing data extracted from queries from main task.
 */
@SuppressWarnings("serial")
public class QueryTuple extends Tuple4<String,String,String,String> {
	private String str_split;
	
	/**
	 * Fields for QueryTuple
	 */
	public enum fields {
		queryid,
		latex,
		keywords
	}
	
	/**
	 * Blank constructor required for Stratosphere execution.
	 * Defaults to "<S>" for str_split. 
	 */
	public QueryTuple() {
		this.f0 = "";
		this.f1 = "";
		this.f2 = "";
		this.str_split = "<S>";
	}
	/**
	 * @param id
	 * @param latex string containing latex tokens delimited by str_split
	 * @param keywords string containing keyword tokens delimited by str_split
	 * @param STR_SPLIT 
	 */
	public QueryTuple(String id, String latex, String keywords, String STR_SPLIT) {
		this.f0 = id;
		this.f1 = latex;
		this.f2 = keywords;
		this.str_split = STR_SPLIT;
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
			this.f2 = this.f2.concat(str_split.concat(keyword));
		}else {
			this.f2 = keyword;
		}
	}
    // TODO: fix
    public String getMML() {
        return this.f2;
    }
    public String getPMML() {
        return this.f3;
    }
	@Override
	public String toString() {
		return this.f0 + "," + this.f1 + "," + this.f2;
	}

}
