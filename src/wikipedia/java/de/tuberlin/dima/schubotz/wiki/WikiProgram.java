package de.tuberlin.dima.schubotz.wiki;

import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Returns search results for NTCIR-11 2014 Wikipedia Subtask
 *
 */
public class WikiProgram {
	private static final Log LOG = LogFactory.getLog(WikiProgram.class);
	
	public static final String STR_SPLIT = "<S>";
	public static final Pattern WORD_SPLIT = Pattern.compile("\\W+", Pattern.UNICODE_CHARACTER_CLASS); 
	
	
	protected static void parseArg(String[] args) throws Exception {
		
	}

	public static void main(String[] args) {
		// FIXME Auto-generated method stub

	}

}
