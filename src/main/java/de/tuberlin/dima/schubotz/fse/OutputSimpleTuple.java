package de.tuberlin.dima.schubotz.fse;

import eu.stratosphere.api.java.tuple.Tuple6;

/**
 * Tuple that stores simple output for printing.
 * In format: queryId,delim(1),docId,rank,score,runtag
 */
public class OutputSimpleTuple extends Tuple6<String,Integer,String,Integer,Double,String> {
	final static String runtag = MainProgram.RUNTAG_LATEX; //WATCH for errors accessing global variables
	/**
	 * @param queryId
	 * @param docId
	 * @param rank
	 * @param score
	 * @param runtag
	 */
	public OutputSimpleTuple() {
		this.f0 = "";
		this.f1 = 1;
		this.f2 = "";
		this.f3 = 1;
		this.f4 = 0.0;
		this.f5 = runtag;
	}
	public OutputSimpleTuple(String queryId,String docId,Integer rank,Double score) {
		this.f0 = queryId;
		this.f1 = 1;
		this.f2 = docId;
		this.f3 = rank;
		this.f4 = score;
		this.f5 = runtag;
	}
	public enum fields {
		queryId,delim,docId,rank,score,runtag
	}
}
