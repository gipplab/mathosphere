package de.tuberlin.dima.schubotz.fse.types;

import eu.stratosphere.api.java.tuple.Tuple3;

/**
 * Tuple that stores scores and justifications for each query and document combo. 
 */
public class ResultTuple extends Tuple3<String,String,Double> {
	/**
	 * Blank constructor required for Stratosphere execution.
	 */
	public ResultTuple() {
		this.f0 = "";
		this.f1 = "";
		this.f2 = 0.0;
	}
	/**
	 * @param queryId
	 * @param docId
	 * @param score
	 */
	public ResultTuple(String queryId, String docId, Double score) {
		this.f0 = queryId;
		this.f1 = docId;
		this.f2 = score;//Double.valueOf(String.format("%.3f%n",score));
	}
	public enum fields {
		queryId,docId,score
	}
}
