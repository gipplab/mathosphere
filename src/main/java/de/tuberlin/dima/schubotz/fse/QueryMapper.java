package de.tuberlin.dima.schubotz.fse;

import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.util.Collector;

public class QueryMapper extends FlatMapFunction<String, QueryTuple> {
	Pattern WORD_SPLIT;
	String STR_SPLIT;
	private static final Log LOG = LogFactory.getLog(QueryMapper.class);
	
	public QueryMapper(Pattern WORD_SPLIT, String STR_SPLIT) {
		this.WORD_SPLIT = WORD_SPLIT;
		this.STR_SPLIT = STR_SPLIT;
	}
	
	
	/**
	 * The core method of the MapFunction. Takes an element from the input data set and transforms
	 * it into another element.
	 *
	 * @param value Query input
	 * @return QueryTuple
	 * @throws Exception This method may throw exceptions. Throwing an exception will cause the operation
	 *                   to fail and may trigger recovery.
	 */
	@Override
	public void flatMap (String value, Collector<QueryTuple> out) throws Exception {
		Node node; 
		String[] tokens;
		
		//Deal with edge cases left by Stratosphere split on <topic>
		if ( value.trim().length() == 0 || value.startsWith("\r\n</topics>")) {
			LOG.warn("Corrupt query " + value);  
			return; 
		}
		if ( (!value.endsWith( "</topic>" )) ) {
			value += "</topic>";
		}
		if ( value.startsWith("<?xml")) {
			value += "</topics>";
		}
		//Parse string as XML
		Document doc = XMLHelper.String2Doc(value,false); 
		
		//Extract query id from XML
		Node main = XMLHelper.getElementB(doc, "//num");
		String queryID = main.getTextContent();
		
		//Extract latex
		NodeList LatexElements = XMLHelper.getElementsB(doc, "//*[name()='m:annotation']"); //get all annotation tags 
		String latex = LatexHelper.extract(LatexElements);
		
		
		//Extract keywords from query
		NodeList KeyWordElements = XMLHelper.getElementsB(doc, "//*[name()='keyword']"); //get all keyword tags
		QueryTuple tup = new QueryTuple(queryID,latex,"",STR_SPLIT);
		for (int i = 0; i < KeyWordElements.getLength(); i++ ) {
			node = KeyWordElements.item(i); 
			try {
				tokens = WORD_SPLIT.split(node.getFirstChild().getNodeValue().toLowerCase());
				for (String token : tokens) {
					if (!token.equals("")) { 
						tup.addKeyword(token); //WILL generate repeats (due to multiformula search in queries)
					}
				}
			} catch (NullPointerException e) {
				continue;
			}
		}
		out.collect(tup);
		
	}

}
