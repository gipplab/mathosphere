package de.tuberlin.dima.schubotz.fse.mappers.cleaners;

import de.tuberlin.dima.schubotz.fse.types.RawDataTuple;
import de.tuberlin.dima.schubotz.fse.utils.SafeLogWrapper;
import eu.stratosphere.util.Collector;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.web.util.HtmlUtils;

/**
 * Created by Jimmy on 8/30/2014.
 */
public class WikiCleaner extends Cleaner {
    private static final SafeLogWrapper LOG = new SafeLogWrapper(WikiCleaner.class);
    private static final String DELIM = "</page>";

    @Override
    public String getDelimiter() {
        return DELIM;
    }

    @Override
    public void flatMap(String in, Collector<RawDataTuple> out) {
        String doc = in;
        //Check for edge cases created from stratosphere split
        //Do length checks to avoid calling trim() or contains() too much
        if (doc.startsWith("<mediawiki")) {
            LOG.debug("Hit mediawiki header document.");
            return;
        }else if (doc.length() < 200 && doc.contains("</mediawiki")) {
            LOG.debug("Hit mediawiki end doc.");
            return;
        }else if (doc.length() < 200 && doc.trim().isEmpty()) {
            return;
        }
        //Add end tags for html parsing
        if (!doc.endsWith(DELIM)) {
            doc += DELIM;
        }
        final Document parsedDoc = Jsoup.parse(doc);
        final Element titleElement = parsedDoc.select("title").first();
        String title = titleElement.text();
        if (title.isEmpty()) {
            LOG.warn("Wiki did not have a title tag/text, assigning this_was_null: ", in);
            title = "this_was_null";
        }
        //Strip title tag
        titleElement.remove();

        String strippedDoc = parsedDoc.toString();
        //Articles come with all math in escaped form (e.g. &lt;math&gt;)
        strippedDoc = HtmlUtils.htmlUnescape(strippedDoc);
        out.collect(new RawDataTuple(title, strippedDoc));
    }

}
