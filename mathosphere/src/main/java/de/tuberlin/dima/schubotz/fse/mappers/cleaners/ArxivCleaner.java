package de.tuberlin.dima.schubotz.fse.mappers.cleaners;

import de.tuberlin.dima.schubotz.fse.types.RawDataTuple;
import de.tuberlin.dima.schubotz.fse.utils.SafeLogWrapper;
import org.apache.flink.util.Collector;
import org.springframework.web.util.HtmlUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Generates Arxiv Document cleaner plan.
 */
public class ArxivCleaner extends Cleaner {
    private static final SafeLogWrapper LOG = new SafeLogWrapper(ArxivCleaner.class);
    /**
	 * Pattern for extracting the filename from the ARXIV tag.
	 */
	private static final Pattern FILENAME_PATTERN = Pattern
	         .compile("<ARXIVFILESPLIT.*?Filename=\"\\./\\d+/(.*?)/\\1_(\\d+)_(\\d+)\\.xhtml\">");

    public static final String DELIM = "</ARXIVFILESPLIT>";

    @Override
    public String getDelimiter() {
        return DELIM;
    }

    @Override
    public void flatMap(String in, Collector<RawDataTuple> out) {
        String doc = in;
        if (doc.length() < 200 && doc.trim().isEmpty()) {
            return;
        }

	    String docID = null;
	    try {
		    docID = getDocId( doc );
	    } catch ( Exception e ) {
		    docID = "this_was_null";
		    LOG.warn( "Null docID, assigning this_was_null: ", doc,e );
	    }
	    //Strip Arxiv line
	    try {
		    doc = getDocString( doc );
		    doc = HtmlUtils.htmlUnescape(doc);
		    out.collect(new RawDataTuple(docID, doc));
	    } catch ( Exception e ) {
		    LOG.warn( "Badly formatted xml title, exiting: ", doc, e );
	    }

    }

	public static String getDocString (String doc) throws Exception {
	    doc = doc.substring(doc.indexOf("<?xml")).trim();
		if(! doc.endsWith( "</html>" )){
			throw new Exception( "Missing </html> at doc footer "  );
		}

		return doc;
	}

	public static String getDocId (String doc) throws Exception {
		final Matcher matcher = FILENAME_PATTERN.matcher(doc);

		if (matcher.find()) {
		    return matcher.group(1) + '_' + matcher.group(2) + '_' + matcher.group(3);
		} else {
			throw new Exception( "NullDoc" );
		}
	}

}
