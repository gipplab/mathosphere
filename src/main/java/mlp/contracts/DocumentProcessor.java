/*        __
 *        \ \
 *   _   _ \ \  ______
 *  | | | | > \(  __  )
 *  | |_| |/ ^ \| || |
 *  | ._,_/_/ \_\_||_|
 *  | |
 *  |_|
 * 
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <rob ∂ CLABS dot CC> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return.
 * ----------------------------------------------------------------------------
 */
package mlp.contracts;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mlp.types.Identifiers;
import mlp.types.WikiDocument;
import mlp.utils.StringUtils;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.types.StringValue;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author rob
 */
public class DocumentProcessor implements FlatMapFunction<String, Tuple2<String, WikiDocument>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentProcessor.class);

    private final Pattern titleRegexp = Pattern.compile("(?:<title>)(.*?)(?:</title>)");
    private final Pattern nsRegexp = Pattern.compile("(?:<ns>)(.*?)(?:</ns>)");
    private final Pattern idRegexp = Pattern.compile("(?:<revision>.*?<id>)(\\d+)(?:</id>)", Pattern.DOTALL);
    private final Pattern textRegexp = Pattern.compile("(?:<text.*?>)(.*?)(?:</text>)", Pattern.DOTALL);

    private final StringValue plaintext = new StringValue();
    private final Identifiers list = new Identifiers();

    @Override
    public void flatMap(String content, Collector<Tuple2<String, WikiDocument>> out) throws Exception {
        // parse title
        Matcher titleMatcher = titleRegexp.matcher(content);
        if (!titleMatcher.find()) {
            return;
        }

        String title = titleMatcher.group(1);
        LOGGER.debug("processing document '{}'...", title);

        // parse namespace
        Matcher namespaceMatcher = nsRegexp.matcher(content);
        if (!namespaceMatcher.find()) {
            return;
        }

        int ns = Integer.parseInt(namespaceMatcher.group(1));

        // parse revision id
        Matcher idMatcher = idRegexp.matcher(content);
        if (!idMatcher.find()) {
            return;
        }

        int id = Integer.parseInt(idMatcher.group(1));

        // parse text
        Matcher textMatcher = textRegexp.matcher(content);
        if (!textMatcher.find()) {
            return;
        }

        String rawText = textMatcher.group(1);
        String text = StringUtils.unescapeEntities(rawText);

        // otherwise create a WikiDocument object from the xml
        WikiDocument doc = new WikiDocument();
        doc.setId(id);
        doc.setTitle(title);
        doc.setNS(ns);
        doc.setText(text);

        // skip docs from namespaces other than 0
        if (doc.getNS() != 0) {
            return;
        }

        // populate the list of known identifiers
        list.clear();
        for (StringValue var : doc.getKnownIdentifiers()) {
            list.add(var);
        }

        // generate a plaintext version of the document
        plaintext.setValue(doc.getPlainText());

        LOGGER.info("Analyzed Page '{}' (id: {}), found identifiers: {}", doc.getTitle(), doc.getId(),
                list.toString());

        out.collect(new Tuple2<>(doc.getTitle(), doc));
    }

}
