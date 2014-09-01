package de.tuberlin.dima.schubotz.fse.mappers.cleaners;

import de.tuberlin.dima.schubotz.fse.types.RawDataTuple;
import de.tuberlin.dima.schubotz.fse.utils.SafeLogWrapper;
import eu.stratosphere.util.Collector;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.web.util.HtmlUtils;


/**
 * Cleans main task queries.
 */
public class QueryCleaner extends Cleaner {
    private static final SafeLogWrapper LOG = new SafeLogWrapper(QueryCleaner.class);
    private static final String DELIM = "</topic>";

    /**
     * Delimiter for Stratosphere to split on
     * @return delimiter
     */
    @Override
    public String getDelimiter() {
        return DELIM;
    }
    @Override
    public void flatMap(String in, Collector<RawDataTuple> out) throws Exception {
        String doc = in;
        //Check if empty query or query is end document
        //Do length check so it will not be running contains() or trim() too much
        if (doc.length() < 200 && doc.contains("</topics>")) {
            LOG.warn("Endpiece of split ", in);
            return;
        } else if (doc.length() < 200 && doc.trim().isEmpty()) {
            return;
        }
        if (doc.startsWith("<?xml")) { //Header document
            doc += "</topic></topics>";
        } else if (!doc.endsWith(DELIM)) { //Add end tag
            doc += DELIM;
        }
        final Document parsedDoc = Jsoup.parse(doc);
        final Element titleElement = parsedDoc.select("num").first();
        String title = titleElement.text();
        if (title.isEmpty()) {
            LOG.warn("Query did not have num title, assigning this_was_null:", in);
            title = "this_was_null";
        }
        //Strip <num> tag
        titleElement.remove();

        String strippedDoc = parsedDoc.toString();
        strippedDoc = HtmlUtils.htmlUnescape(strippedDoc);
        out.collect(new RawDataTuple(title, strippedDoc));
    }

}