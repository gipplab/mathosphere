package de.tuberlin.dima.schubotz.wiki.mappers;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import de.tuberlin.dima.schubotz.utils.LatexHelper;
import de.tuberlin.dima.schubotz.wiki.types.WikiQueryTuple;
import de.tuberlin.dima.schubotz.wiki.types.WikiTuple;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.util.Collector;

@SuppressWarnings("serial")
public class WikiMapper extends FlatMapFunction<String, WikiTuple> {
	HashSet<String> latex;
	String STR_SPLIT;
	Log LOG = LogFactory.getLog(WikiMapper.class);
	
	@SuppressWarnings("hiding")
	public WikiMapper (String STR_SPLIT) {
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
	public void flatMap (String in, Collector<WikiTuple> out) throws Exception {
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
		try {
			String docID = doc.select("title").first().text();
			out.collect(new WikiTuple(docID, sectionLatex));
		} catch (NullPointerException e) {
			LOG.warn("Null title encountered: " + in, e);
			return;
		}
	}


	

}
