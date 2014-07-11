package de.tuberlin.dima.schubotz.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.HashMultiset;

public class TFIDFHelper {
	private static Log LOG = LogFactory.getLog(TFIDFHelper.class);
	
	/**
	 * @param queryTokens
	 * @param sectionTokens
	 * @param map - map of term to how many documents contain that term
	 * @return
	 */
	public static double calculateTFIDFScore(HashMultiset<String> queryTokens, HashMultiset<String> sectionTokens,
											 HashMultiset<String> map, int numDocs, boolean debug) {
		/*
		 * NaN possibilities:
		 * -1) The total number of terms in the document is zero (tf = x/0)- 
		 * -2) The total number of documents that contains the term is -1-
		 * -3) The total number of documents is <= 0-
		 * -4) numDocs is so high that it is NaN- 
		 * -5) count(element) is returning NaN or <= -1-
		 * 6) size() is returning NaN or zero 
		 */
		double termTotal = sectionTokens.size(); //total number of terms in current section
		double termFreqDoc; //frequency in current section
		double termFreqTotal; //number of documents that contain the term
		
		//Calculations based on http://tfidf.com/
		double tf = 0d; //term frequency
		double idf = 0d; //inverse document frequency
		double total = 0d;
				
		for (String element : queryTokens.elementSet()) { //strips duplicates in query due to multiple formulas
			termFreqDoc = sectionTokens.count(element);
			termFreqTotal = map.count(element);
			tf = termFreqDoc / termTotal; //can be zero but not undefined
			idf = Math.log(((double) numDocs) / (1d + termFreqTotal)); //will never be undefined due to +1
			total += tf * idf;
			if (debug) {
				LOG.debug("Term: " + element);
				LOG.debug("Freq in Doc: " + termFreqDoc);
				LOG.debug("Num doc with term: " + termFreqTotal);
				LOG.debug("tf: " + tf);
				LOG.debug("idf: " + idf);
				LOG.debug("total: " + total);
			}
		}
		if (debug) {
			LOG.debug("end total: " + total);
			LOG.debug("END END END END");
		}
		return total;
		
	}

}
