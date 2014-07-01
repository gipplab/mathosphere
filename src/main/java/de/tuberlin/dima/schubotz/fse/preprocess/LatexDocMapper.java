package de.tuberlin.dima.schubotz.fse.preprocess;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import de.tuberlin.dima.schubotz.fse.LatexHelper;
import de.tuberlin.dima.schubotz.fse.QueryTuple;
import de.tuberlin.dima.schubotz.fse.SectionTuple;
import de.tuberlin.dima.schubotz.fse.XMLHelper;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.util.Collector;

public class LatexDocMapper extends FlatMapFunction<String, Tuple2<String,Integer>>{
	HashSet<String> latex;
	
	@Override
	public void open(Configuration parameters) {
		//Get latex from queries
		latex = new HashSet<String>();
		Collection<QueryTuple> queries = getRuntimeContext().getBroadcastVariable( "Queries" );
		for (QueryTuple query : queries) {
			String[] tokens = query.getLatex().split( "<S>" ); //get list of latex
			for ( String token : tokens ) {
				latex.add(token);
			}
		}
	}
	
	@Override
	public void flatMap (String value, Collector<Tuple2<String,Integer>> out) throws Exception {
		//Takes in document, outputs tuple of <latex token, 1> for every token in document contained in set of query latex
		
		//Remove <ARXIV> and <?xml
		String[] lines = value.trim().split( "\\n", 2 );
		if ( lines.length < 2 ) { 
			System.out.println(value); //DEBUG
			return;
		}
		//Parse string as XML
		Document doc = XMLHelper.String2Doc(lines[1], false); 
		NodeList LatexElements = XMLHelper.getElementsB(doc, "//annotation"); //get all annotation tags
		
		//Extract latex
		String latex = LatexHelper.extract(LatexElements);
		String[] tokens = latex.split( "<S>" );
		Set<String> tokenSet = new HashSet<String>(Arrays.asList(tokens)); //remove repeats
		
		//Loop through and output
		for (String token : tokenSet) {
			if (latex.contains(token)) {
				out.collect(new Tuple2<String,Integer>(token, 1));
			}
		}
	}

}
