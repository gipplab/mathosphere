package de.tuberlin.dima.schubotz.wiki.preprocess;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import de.tuberlin.dima.schubotz.common.utils.LatexHelper;
import de.tuberlin.dima.schubotz.wiki.types.WikiQueryTuple;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.util.Collector;

/**
 * Maps wiki document to a collector of 
 * Tuple2<LatexToken,1> for aggregation
 * later.
 */
@SuppressWarnings("serial")
public class LatexWikiMapper extends FlatMapFunction<String,Tuple2<String,Integer>>{
	private String STR_SPLIT;
	private HashSet<String> latex;
	
	Log LOG = LogFactory.getLog(LatexWikiMapper.class);
	
	@SuppressWarnings("hiding")
	public LatexWikiMapper(String STR_SPLIT) {
		this.STR_SPLIT = STR_SPLIT;
	}
	@Override
	public void open(Configuration parameters) {
		latex = new HashSet<String>();
		Collection<WikiQueryTuple> queries = getRuntimeContext().getBroadcastVariable( "Queries" );
		for (WikiQueryTuple query : queries) {
			String[] tokens = query.getLatex().split(STR_SPLIT); //get list of latex
			for ( String token : tokens ) {
				if (!token.equals("")) {
					latex.add(token);
				}
			}
		}
	}
	@Override
	public void flatMap(String in, Collector<Tuple2<String,Integer>> out) {
		//Check for edge cases created from stratosphere split
		if (in.startsWith("<mediawiki")) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Hit mediawiki header document.");
			}
			return;
		}else if (in.startsWith("</mediawiki")) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Hit mediawiki end doc.");
			}
			return;
		}
		if (!in.endsWith("</page>")) {
			in += "</page>";
		}
		in = StringEscapeUtils.unescapeHtml(in); //WATCH cpu bottleneck?
		Document doc;
		Elements LatexElements;
		try {
			doc = Jsoup.parse(in); //using jsoup b/c wiki html is TERRIBLE
			LatexElements = doc.select("annotation[encoding=application/x-tex]");
		} catch (Exception e) {
			if (LOG.isWarnEnabled()) {
				LOG.warn("Unable to parse wiki using Jsoup: " + in);
			}
			return;
		}
		String sectionLatex = LatexHelper.extract(LatexElements, STR_SPLIT);
		if (!sectionLatex.equals("")) {
			String[] tokens = sectionLatex.split(STR_SPLIT); 
			Set<String> tokenSet = new HashSet<String>(Arrays.asList(tokens)); //remove repeats (only want number of documents)
			for (String token : tokenSet) {
				if (latex.contains(token)) {
					out.collect(new Tuple2<String,Integer>(token, 1));
				}
			}
		}
	}
}
