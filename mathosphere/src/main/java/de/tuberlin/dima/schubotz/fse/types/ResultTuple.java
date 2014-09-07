package de.tuberlin.dima.schubotz.fse.types;

import org.apache.flink.api.java.tuple.Tuple3;

/**
 * Tuple that stores scores and justifications for each query and document combo. 
 */
public class ResultTuple extends Tuple3<String,String,Double> {
	/**
	 * Blank constructor required for Stratosphere execution.
	 */
	public ResultTuple() {
        f0 = "";
        f1 = "";
        f2 = 0.0;
	}
	/**
	 * @param queryId
	 * @param docId
	 * @param score
	 */
	public ResultTuple(String queryId, String docId, Double score) {
        f0 = queryId;
        f1 = docId;
        f2 = score;
	}
	public enum fields {
		queryId,docId,score
	}
}
