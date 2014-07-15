package de.tuberlin.dima.schubotz.wiki.mappers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.tuberlin.dima.schubotz.common.utils.LatexHelper;
import de.tuberlin.dima.schubotz.common.utils.XMLHelper;
import de.tuberlin.dima.schubotz.fse.MainProgram;
import de.tuberlin.dima.schubotz.wiki.types.WikiQueryTuple;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.util.Collector;


@SuppressWarnings("serial")
public class WikiQueryMapper extends FlatMapFunction<String,WikiQueryTuple>{
	Log LOG = LogFactory.getLog(WikiQueryMapper.class);
	/**
	 * See {@link MainProgram#STR_SPLIT}
	 */
	String STR_SPLIT;
	
	/**
	 * @param STR_SPLIT {@link MainProgram#STR_SPLIT} sent as parameter to ensure serializability
	 */
	@SuppressWarnings("hiding")
	public WikiQueryMapper(String STR_SPLIT) {
		this.STR_SPLIT = STR_SPLIT;
	}
	
	/**
	 * Takes in a query, parses query ID and latex
	 */
	@Override
	public void flatMap(String in, Collector<WikiQueryTuple> out) {
		Document doc;
		Node main;
		try {
			doc = XMLHelper.String2Doc(in,false);
			main = XMLHelper.getElementB(doc, "//num"); //Xpath expression for <num> tag
		} catch (Exception e) {
			if (LOG.isWarnEnabled()) {
				LOG.warn("Unable to parse XML in query: " + in);
			}
			return;
		}
		String queryID = main.getTextContent();
		NodeList LatexElements = null;
		try {
			LatexElements = XMLHelper.getElementsB(doc, "//*[name()='m:annotation']"); //get all annotation tags
		} catch (Exception e) {
			if (LOG.isWarnEnabled()) {
				LOG.warn("Unable to find annotation tags in query: " + in);
			}
		}
		String latex = LatexHelper.extract(LatexElements, STR_SPLIT);
		
		out.collect(new WikiQueryTuple(queryID, latex));
	}
	
}
