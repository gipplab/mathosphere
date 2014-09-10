package de.tuberlin.dima.schubotz.fse.types;


import org.apache.flink.api.java.tuple.Tuple5;

/**
 * Holds all data.
 */
public final class DatabaseTuple extends Tuple5<String, String, Integer, Short, String> {
    public DatabaseTuple () {
        setFields("", "", 0, (short) 0 , "");
	}

	/**
	 * @param docID
	 * @param latex
     * @param keywords
     * @param mml
     * @param pmml
	 */
	public DatabaseTuple (String docID, String latex, Integer keywords, Short mml, String pmml) {
        setFields(docID, latex, keywords, mml, pmml);
	}

	public <T> T getNamedField(fields field) {
		return (T) getField( field.ordinal() );
	}
	//TODO: Check if we really need this.
	public Boolean getBooleanField(fields fields){
		Short s = getNamedField( fields );
		if(s!=0) return true; else return false;
	}
	public <T> void setNamedField(fields field, T value) {
		setField(value, field.ordinal());
	}

    public enum fields {
	    pageId, formulaName, fId, isEquation,value
    }
}
