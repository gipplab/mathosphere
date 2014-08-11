package de.tuberlin.dima.schubotz.fse.types;

import eu.stratosphere.api.java.tuple.Tuple5;

/**
 * Holds all data.
 */
public class DataTuple extends Tuple5<String, String, String, String, String> {
    public DataTuple () {
		this.f0 = "nulldoc";
		this.f1 = "";
        this.f2 = "";
        this.f3 = "";
        this.f4 = "";
	}

	/**
	 * @param docID
	 * @param latex
     * @param plaintext
     * @param mml
     * @param pmml
	 */
	public DataTuple (String docID, String latex, String plaintext, String mml, String pmml) {
		this.f0 = docID;
		this.f1 = latex;
        this.f2 = plaintext;
		this.f3 = mml;
		this.f4 = pmml;
	}

    public DataTuple(DataTuple wikiTuple) {
    }

    public DataTuple(String docID, String s, String s1, String s2) {

    }

    public String getID() {
		return this.f0;
	}

	public String getLatex() {
		return this.f1;
	}

    public String getPlaintext() {
        return this.f2;
    }

	public String getMML() {
		return this.f3;
	}

	public String getPMML() {
		return this.f4;
	}
    @Override
	public String toString() {
		return getID() + "," + getLatex() + "," + getPlaintext() + "," + getMML() + "," + getPMML();
	}
}
