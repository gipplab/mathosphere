package de.tuberlin.dima.schubotz.fse;

import eu.stratosphere.api.java.tuple.Tuple3;

/**
 * Tuple that stores scores and justifications for each query and document combo. 
 * In format queryid,docid,score
 */
public class ResultTuple extends Tuple3<String,String,Integer> {
	public ResultTuple() {
		this.f0 = "";
		this.f1 = "";
		this.f2 = 0;
	}
	/**
	 * @param queryId
	 * @param docId
	 * @param score
	 */
	public ResultTuple(String queryId, String docId, Integer score) {
		this.f0 = queryId;
		this.f1 = docId;
		this.f2 = score;
	}
	public enum fields {
		queryId,docId,score
	}
}
