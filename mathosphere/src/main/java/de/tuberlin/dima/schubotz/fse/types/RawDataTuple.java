package de.tuberlin.dima.schubotz.fse.types;


import org.apache.flink.api.java.tuple.Tuple2;

/**
 * Contains title, data. The data should be cleaned
 * of title text.
 */
public class RawDataTuple extends Tuple2<String, String> {
    public RawDataTuple () {
        setFields("this_was_null", "");
    }

	/**
	 * @param ID
	 * @param data
	 */
	public RawDataTuple (String ID, String data) {
        setFields(ID,data);
	}

    public String getNamedField(fields field) {
        return getField(field.ordinal());
    }

    private void setNamedField(fields field, String value) {
        setField(value, field.ordinal());
    }

    public enum fields {
        ID, rawData
    }

}
