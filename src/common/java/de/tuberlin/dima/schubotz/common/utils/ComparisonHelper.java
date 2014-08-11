package de.tuberlin.dima.schubotz.common.utils;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.select.NodeVisitor;

import java.util.Collection;
import java.util.HashSet;

/**
 * Helper class for calculating scores
 */
public class ComparisonHelper {
	private static final SafeLogWrapper LOG = new SafeLogWrapper(ComparisonHelper.class);
	
	/**
	 * @param queryTokens
	 * @param sectionTokens
	 * @param map - map of term to how many documents contain that term
	 * @return
	 */
	public static double calculateTFIDFScore(Multiset<String> queryTokens, HashMultiset<String> sectionTokens,
											 HashMultiset<String> map, int numDocs) {
		/*
		 * NaN possibilities:
		 * -1) The total number of terms in the document is zero (tf = x/0)- 
		 * -2) The total number of documents that contains the term is -1-
		 * -3) The total number of documents is <= 0-
		 * -4) numDocs is so high that it is NaN- 
		 * -5) count(element) is returning NaN or <= -1-
		 * 6) size() is returning NaN or zero 
		 */
		final double termTotal = (double) sectionTokens.size(); //total number of terms in current section
		double termFreqDoc; //frequency in current section
		double termFreqTotal; //number of documents that contain the term
		
		//Calculations based on http://tfidf.com/
		double tf = 0.; //term frequency
		double idf = 0.; //inverse document frequency
		double total = 0.;
				
		for (final String element : queryTokens.elementSet()) { //strips duplicates in query due to multiple formulas
			termFreqDoc = (double) sectionTokens.count(element);
			termFreqTotal = (double) map.count(element);
			tf = termFreqDoc / termTotal; //can be zero but not undefined
			idf = StrictMath.log(((double) numDocs) / (1d + termFreqTotal)); //will never be undefined due to +1
			total += tf * idf;
			LOG.debug("Term: " + element);
			LOG.debug("Freq in Doc: " + termFreqDoc);
			LOG.debug("Num doc with term: " + termFreqTotal);
			LOG.debug("tf: " + tf);
			LOG.debug("idf: " + idf);
			LOG.debug("total: " + total);
		}
		LOG.debug("end total: " + total);
		LOG.debug("END END END END");
		return total;
		
	}

    /**
     * Interface method for calculating MML score.
     * @param wikiMML stringified mathml of wiki
     * @param queryMML stringified mathml of query
     * @return numMatch number of leaf nodes in wiki that also occur in query. does not take into account repeats
     */
    public static int calculateMMLScore(String wikiMML, String queryMML) {
        return cmmlLeafScore(wikiMML, queryMML);
    }

    private static int cmmlLeafScore (String wikiMML, String queryMML) {
                //HashSet containing leaf node text for comparison
        final Collection<String> leafNodes = new HashSet<>();
        final Document wikiDoc = Jsoup.parse(wikiMML, "", Parser.xmlParser());
        final Document queryDoc = Jsoup.parse(queryMML, "", Parser.xmlParser());

        //Drill down to leaf nodes in query, add to hashset
        queryDoc.traverse(new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                if (node.childNodes().isEmpty()) {
                    //Hit leaf element
                    //TODO implement own version of jsoup to fix this nasty workaround?
                    final String text = Jsoup.parseBodyFragment(node.outerHtml()).text().trim();
                    if (text.isEmpty()) {
                    } else {
                        leafNodes.add(text);
                    }
                }
            }
            @Override
            public void tail(Node node, int depth) {
                //Do nothing, already added
            }
        });

        //Java closure workaround
        final int[] score = {0};
        //Drill down to leaf nodes in wiki, if in query hashset add to score
        wikiDoc.traverse(new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                if (node.childNodes().isEmpty()) {
                    //Hit leaf element
                    if (leafNodes.contains(Jsoup.parseBodyFragment(node.outerHtml()).text())) {
                        score[0] = score[0] + 1;
                    }
                }
            }
            @Override
            public void tail(Node node, int depth) {
            }
        });

        return score[0];
    }

    public static double calculatePMMLScore(String wikiPMML, String queryPMML) {
        return 0.;
    }
}
