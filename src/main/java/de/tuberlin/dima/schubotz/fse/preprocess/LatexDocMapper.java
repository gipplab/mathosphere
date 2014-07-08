package de.tuberlin.dima.schubotz.fse.preprocess;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import de.tuberlin.dima.schubotz.fse.LatexHelper;
import de.tuberlin.dima.schubotz.fse.QueryTuple;
import de.tuberlin.dima.schubotz.fse.XMLHelper;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.util.Collector;

public class LatexDocMapper extends FlatMapFunction<String, Tuple2<String,Integer>>{
	HashSet<String> latex;
	String STR_SPLIT;
	
	public LatexDocMapper(String STR_SPLIT) {
		this.STR_SPLIT = STR_SPLIT;
	}
	
	@Override
	public void open(Configuration parameters) {
		//Get latex from queries
		latex = new HashSet<String>();
		Collection<QueryTuple> queries = getRuntimeContext().getBroadcastVariable( "Queries" );
		for (QueryTuple query : queries) {
			String[] tokens = query.getLatex().split(STR_SPLIT); //get list of latex
			for ( String token : tokens ) {
				if (!token.equals("")) {
					latex.add(token);
				}
			}
		}
	}
	
	@Override
	public void flatMap (String value, Collector<Tuple2<String,Integer>> out) {
		//Takes in document, outputs tuple of <latex token, 1> for every document containing a token contained in set of query latex
		//Remove <ARXIV> and <?xml
		NodeList LatexElements;
		String[] lines = value.trim().split( "\\n", 2 );
		if ( lines.length < 2 ) { 
			System.out.println("Null document (LatexDocMapper): " + value); //DEBUG output null document
			return;
		}
		try {
			//Parse string as XML
			Document doc = XMLHelper.String2Doc( lines[1], false );
			LatexElements= XMLHelper.getElementsB( doc, "//annotation" ); //get all annotation tags
		} catch ( Exception e ){
			System.out.println("Could not parse document:"+lines[0] );
			LatexElements = null;
		}
		//Extract latex
		String sectionLatex = LatexHelper.extract(LatexElements);
		if (!sectionLatex.equals("")) {
			String[] tokens = sectionLatex.split(STR_SPLIT); 
			Set<String> tokenSet = new HashSet<String>(Arrays.asList(tokens)); //remove repeats (only want number of documents)
			//Loop through and output
			for (String token : tokenSet) {
				if (latex.contains(token)) {
					out.collect(new Tuple2<String,Integer>(token, 1));
				}
			}
		}
	}

}
