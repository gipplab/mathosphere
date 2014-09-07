package de.tuberlin.dima.schubotz.fse.types;


import org.apache.flink.api.java.tuple.Tuple5;

/**
 * Holds all data.
 */
public final class DataTuple extends Tuple5<String, String, String, String, String> {
    public DataTuple () {
        setFields("this_was_null", "", "", "", "");
	}

	/**
	 * @param docID
	 * @param latex
     * @param keywords
     * @param mml
     * @param pmml
	 */
	public DataTuple (String docID, String latex, String keywords, String mml, String pmml) {
        setFields(docID, latex, keywords, mml, pmml);
	}

    public String getNamedField(fields field) {
        return getField(field.ordinal());
    }

    private void setNamedField(fields field, String value) {
        setField(value, field.ordinal());
    }

    public enum fields {
        docID, latex, keywords, mml, pmml
    }
}
