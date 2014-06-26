package de.tuberlin.dima.schubotz.fse;

import java.io.Serializable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.util.Collector;

public class QueryLatexMapper extends FlatMapFunction<String, Tuple2<String,String>> implements Serializable{
	
	/**
	 * The core method of the MapFunction. Takes an element from the input data set and transforms
	 * it into another element.
	 *
	 * @param value The input value.
	 * @return The value produced by the map function from the input value.
	 * @throws Exception This method may throw exceptions. Throwing an exception will cause the operation
	 *                   to fail and may trigger recovery.
	 */
	@Override
	public void flatMap (String value, Collector<Tuple2<String,String>> out) throws Exception {
		//Given query input, return <queryid,filename>
		if ( value.trim().length() == 0 || value.startsWith("\r\n</topics>")) return; //TODO fix these special cases
		if ( (!value.endsWith( "</topic>" )) ) {
			value += "</topic>";
		}
		if ( value.startsWith("<?xml")) {
			value += "</topics>";
		}
		String latex = "";
		Node node;
		//Parse string as XML
		Document doc = XMLHelper.String2Doc(value,false); //string, not namespace aware
		XMLHelper.printDocument(doc);
		Node main = XMLHelper.getElementB(doc, "//num");
		String queryID = main.getTextContent();
		//Extract latex
		NodeList LatexElements = XMLHelper.getElementsB(doc, "//*[name()='m:annotation']"); //get all annotation tags 
		for (int i = 0; i < LatexElements.getLength(); i++ ) {
			node = LatexElements.item(i); 
			if (node.getAttributes().getNamedItem("encoding").getNodeValue().equals(new String("application/x-tex"))){ //check if latex
				latex=latex.concat("<SPLIT>" + node.getFirstChild().getNodeValue()); //ArrayLists non serializable so make do with this...
			}
		}

		out.collect(new Tuple2<String,String>(queryID,latex));
		
	}

}
