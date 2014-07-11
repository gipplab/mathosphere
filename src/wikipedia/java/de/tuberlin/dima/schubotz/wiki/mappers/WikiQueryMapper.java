package de.tuberlin.dima.schubotz.wiki.mappers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.tuberlin.dima.schubotz.utils.LatexHelper;
import de.tuberlin.dima.schubotz.utils.XMLHelper;
import de.tuberlin.dima.schubotz.wiki.types.WikiQueryTuple;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.util.Collector;


public class WikiQueryMapper extends FlatMapFunction<String,WikiQueryTuple>{
	Log LOG = LogFactory.getLog(WikiQueryMapper.class);
	String STR_SPLIT;	
	
	public WikiQueryMapper(String STR_SPLIT) {
		this.STR_SPLIT = STR_SPLIT;
	}
	
	@Override
	public void flatMap(String in, Collector<WikiQueryTuple> out) {
		//Dealing with badly formatted html as a result of Stratosphere split
		StringBuilder endDoc = new StringBuilder(); //used to search for weird doc at the end of file
		endDoc.append(System.getProperty("line.separator"));
		endDoc.append("</topics>");
		if (in.trim().length() == 0 || in.startsWith(endDoc.toString())) {
			LOG.warn("Corrupt query: " + in);
			return;
		}
		if (in.startsWith("<?xml ")) {
			in += "</topic></topics>";
		}else if (!in.endsWith("</topic>")) {
			in += "</topic>";
		}
		
		Document doc;
		Node main;
		try {
			doc = XMLHelper.String2Doc(in,false);
			main = XMLHelper.getElementB(doc, "//num"); //Query ID tag is <num>
		} catch (Exception e) {
			LOG.warn("Unable to parse XML in query: " + in);
			return;
		}
		String queryID = main.getTextContent();
		NodeList LatexElements = null;
		try {
			LatexElements = XMLHelper.getElementsB(doc, "//*[name()='m:annotation']"); //get all annotation tags
		} catch (Exception e) {
			LOG.warn("Unable to find annotation tags in query: " + in);
		}
		String latex = LatexHelper.extract(LatexElements, STR_SPLIT);
		
		out.collect(new WikiQueryTuple(queryID, latex));
	}
	
}
