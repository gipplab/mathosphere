package de.tuberlin.dima.schubotz.wiki.mappers;

import org.springframework.web.util.HtmlUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.util.Collector;

/**
 * Cleans up and formats wiki text TODO no known better way
 * Required due to Stratosphere split on {@link de.tuberlin.dima.schubotz.wiki.WikiProgram#WIKI_SEPARATOR}
 */
public class WikiCleaner extends FlatMapFunction<String, String> {
	Log LOG = LogFactory.getLog(WikiCleaner.class);
	
	/**
	 * Search string for funky last document  
	 */
	final String endDoc = System.getProperty("line.separator") + "</mediawiki";
	@Override
	public void flatMap(String in, Collector<String> out) throws Exception {
		if (in.trim().length() <= 0) {
			return;
		}
		//Check for edge cases created from stratosphere split
		if (in.startsWith("<mediawiki")) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Hit mediawiki header document.");
			}
			return;
		}else if (in.startsWith(endDoc)) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Hit mediawiki end doc.");
			}
			return;
		}
		if (!in.endsWith("</page>")) {
			in += "</page>";
		}
		in = HtmlUtils.htmlEscape(in); 
		out.collect(in);
	}
}