package de.tuberlin.dima.schubotz.wiki.mappers;

import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.util.Collector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.util.HtmlUtils;

/**
 * Cleans wiki queries TODO find if stratosphere can do this better.
 * Required due to Stratosphere split on {@link de.tuberlin.dima.schubotz.wiki.WikiProgram#QUERY_SEPARATOR}
 * Returns HTML/XML with unescaped entities in them.
 */
public class WikiQueryCleaner extends FlatMapFunction<String, String> {
	Log LOG = LogFactory.getLog(WikiQueryCleaner.class);
	final String endQuery = System.getProperty("line.separator") + "</topics>"; //used to search for last query weirdness
	@Override
	public void flatMap(String in, Collector<String> out) throws Exception {
		//Dealing with badly formatted html as a result of Stratosphere split
		if (in.trim().length() == 0 || in.startsWith(endQuery)) { 
			if (LOG.isWarnEnabled()) {
				LOG.warn("Corrupt query: " + in);
			}
			return;
		}
		if (in.startsWith("<?xml ")) {
			in += "</topic></topics>";
		}else if (!in.endsWith("</topic>")) {
			in += "</topic>";
		}
        //For consistency's sake with WikiCleaner#flatMap
        in = HtmlUtils.htmlUnescape(in);
		out.collect(in);
	}

}
