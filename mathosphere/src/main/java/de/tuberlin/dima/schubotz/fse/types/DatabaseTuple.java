package de.tuberlin.dima.schubotz.fse.types;


import org.apache.flink.api.java.tuple.Tuple5;

/**
 * Holds all data.
 */
public final class DatabaseTuple extends Tuple5<String, String, Integer, Boolean, String> {
    public DatabaseTuple () {
        setFields("", "", 0, false, "");
	}

	/**
	 * @param docID
	 * @param latex
     * @param keywords
     * @param mml
     * @param pmml
	 */
	public DatabaseTuple (String docID, String latex, Integer keywords, Boolean mml, String pmml) {
        setFields(docID, latex, keywords, mml, pmml);
	}

    public String getNamedField(fields field) {
        return getField(field.ordinal());
    }

    private void setNamedField(fields field, String value) {
        setField(value, field.ordinal());
    }

    public enum fields {
	    pageId, formulaName, fId, isEquation,value
    }
}
