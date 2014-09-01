package de.tuberlin.dima.schubotz.fse.mappers.cleaners;

import de.tuberlin.dima.schubotz.fse.types.RawDataTuple;
import de.tuberlin.dima.schubotz.fse.utils.SafeLogWrapper;
import eu.stratosphere.util.Collector;
import org.springframework.web.util.HtmlUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Generates Arxiv Document cleaner plan.
 */
public class ArxivCleaner extends Cleaner {
    private static final SafeLogWrapper LOG = new SafeLogWrapper(ArxivCleaner.class);
    private static final Pattern TAG_SPLIT = Pattern.compile("\\n");
    /**
	 * Pattern for extracting the filename from the ARXIV tag.
	 */
	private static final Pattern FILENAME_PATTERN = Pattern
	         .compile("<ARXIVFILESPLIT\\\\nFilename=\"\\./\\d+/(.*?)/\\1_(\\d+)_(\\d+)\\.xhtml\">");

    private static final String DELIM = "</ARXIVFILESPLIT>";

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

        final String[] lines = TAG_SPLIT.split(doc, 2);
        final Matcher matcher = FILENAME_PATTERN.matcher(lines[0]);
        String docID = "this_was_null";
        if (matcher.find()) {
            docID = matcher.group(1) + '_' + matcher.group(2) + '_' + matcher.group(3) + ".xhtml";
        } else {
            LOG.warn("Null docID, assigning this_was_null: ", doc);
        }
        //Strip Arxiv line
        doc = doc.substring(doc.indexOf("<?xml"));
        doc = HtmlUtils.htmlUnescape(doc);
        out.collect(new RawDataTuple(docID, doc));
    }

}
