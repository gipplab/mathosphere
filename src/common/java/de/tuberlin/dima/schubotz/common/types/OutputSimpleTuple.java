package de.tuberlin.dima.schubotz.common.types;

import eu.stratosphere.api.java.tuple.Tuple6;

/**
 * Tuple that stores simple output for printing.
 */
@SuppressWarnings("serial")
public class OutputSimpleTuple extends Tuple6<String,Integer,String,Integer,Double,String> {
	private String runtag;
	/**
	 * Blank constructor required for Stratosphere execution
	 */
	public OutputSimpleTuple() {
		this.f0 = "";
		this.f1 = 1;
		this.f2 = "";
		this.f3 = 1;
		this.f4 = 0.0;
		this.f5 = "DEFAULT_RUNTAG";
	}
	/**
	 * @param queryId
	 * @param docId
	 * @param rank
	 * @param score
	 * @param runtag
	 */
	@SuppressWarnings("hiding")
	public OutputSimpleTuple(String queryId,String docId,Integer rank,Double score,String runtag) {
		this.f0 = queryId;
		this.f1 = 1;
		this.f2 = docId;
		this.f3 = rank;
		this.f4 = score;
		this.f5 = runtag;
	}
}
