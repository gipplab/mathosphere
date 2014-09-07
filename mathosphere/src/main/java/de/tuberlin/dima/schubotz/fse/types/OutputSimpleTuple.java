package de.tuberlin.dima.schubotz.fse.types;


import org.apache.flink.api.java.tuple.Tuple6;

/**
 * Tuple that stores simple output for printing.
 */
public class OutputSimpleTuple extends Tuple6<String,Integer,String,Integer,Double,String> {
	private String runtag;
	/**
	 * Blank constructor required for Stratosphere execution
	 */
	public OutputSimpleTuple() {
        f0 = "";
        f1 = 1;
        f2 = "";
        f3 = 1;
        f4 = 0.0;
        f5 = "DEFAULT_RUNTAG";
	}
	/**
	 * @param queryId
	 * @param docId
	 * @param rank
	 * @param score
	 * @param runtag
	 */
	public OutputSimpleTuple(String queryId,String docId,Integer rank,Double score,String runtag) {
        f0 = queryId;
        f1 = 1;
        f2 = docId;
        f3 = rank;
        f4 = score;
        f5 = runtag;
	}
}
