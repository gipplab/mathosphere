package de.tuberlin.dima.schubotz.fse.mappers;

import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.tuberlin.dima.schubotz.common.utils.ExtractHelper;
import de.tuberlin.dima.schubotz.common.utils.XMLHelper;
import de.tuberlin.dima.schubotz.fse.types.QueryTuple;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.util.Collector;

@SuppressWarnings("serial")
public class QueryMapper extends FlatMapFunction<String, QueryTuple> {
	/**
	 * {@link de.tuberlin.dima.schubotz.fse.MainProgram#WORD_SPLIT}.
	 */
	Pattern WORD_SPLIT;
	/**
	 * {@link de.tuberlin.dima.schubotz.fse.MainProgram#STR_SPLIT}.
	 */
	String STR_SPLIT;
	/**
	 * Logger for QueryMapper.
	 */
	private static final Log LOG = LogFactory.getLog(QueryMapper.class);
	
	/**
	 * @param WORD_SPLIT {@link de.tuberlin.dima.schubotz.fse.MainProgram#WORD_SPLIT} sent as parameter to ensure serializability. 
	 * @param STR_SPLIT {@link de.tuberlin.dima.schubotz.fse.MainProgram#STR_SPLIT} sent as parameter to ensure serializability.
	 */
	@SuppressWarnings("hiding")
	public QueryMapper(Pattern WORD_SPLIT, String STR_SPLIT) {
		this.WORD_SPLIT = WORD_SPLIT;
		this.STR_SPLIT = STR_SPLIT;
	}
	
	/**
	 * The core method of the MapFunction. Takes an element from the input data set and transforms
	 * it into another element.
	 *
	 * @param value query string
	 * @return QueryTuple
	 * @throws Exception This method may throw exceptions. Throwing an exception will cause the operation
	 *                   to fail and may trigger recovery.
	 */
	@Override
	public void flatMap (String value, Collector<QueryTuple> out) throws Exception {
		Node node; 
		String[] tokens;
		Document doc;
		Node main;
		try {
			//Parse string as XML
			doc = XMLHelper.String2Doc(value,false); 
			//Extract query id from XML
			main = XMLHelper.getElementB(doc, "//num");
		} catch (Exception e) {
			if (LOG.isWarnEnabled()) {
				LOG.warn("Unable to parse XML in query: " + value);
			}
			return;
		}
		String queryID = main.getTextContent();
		
		//Extract latex
		NodeList LatexElements = null;
		try {
			LatexElements = XMLHelper.getElementsB(doc, "//*[name()='m:annotation']"); //get all annotation tags
		} catch (Exception e) {
			if (LOG.isWarnEnabled()) {
				LOG.warn("Unable to find annotation tags in query: " + value);
			}
		}
		String latex = ExtractHelper.extractLatexXMLHelper(LatexElements, STR_SPLIT);
		
		
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
