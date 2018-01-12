package com.formulasearchengine.mathosphere.mlp.contracts;

import com.formulasearchengine.mathosphere.mlp.pojos.ContentType;
import com.formulasearchengine.mathosphere.mlp.pojos.RawWikiDocument;
import org.apache.flink.util.Collector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotNull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This text extractor is for custom HTML pages. Which usually don't use
 * the <ns></ns> nor <text></text> block. In HTML pages the text block
 * is in <body></body> and we ignore the namespaces.
 *
 * However, it automatically detects if the given input is an HTML website or
 * not. If the given content in the {@link #flatMap(String, Collector)} is not
 * HTML, it will just call and return the superclass.
 *
 * Furthermore, notice that HTML is just escaped if necessary but MML blocks
 * are not escaped. Therefore, we don't need to unescape something here.
 *
 * Note also, it assumes only one page in content. Don't mix up different styles.
 * See for example {@link com.formulasearchengine.mathosphere.mlp.FlinkMlpRelationFinder#readWikiDump}
 * to split documents on the <page></page> tag.
 *
 * @author Andre Greiner-Petter
 */
public class HtmlTextExtractorMapper extends TextExtractorMapper {

    private static final Logger LOG = LogManager.getLogger(HtmlTextExtractorMapper.class.getName());

    private static final Pattern BODY_PATTERN = Pattern.compile("<body.*?>(.*?)</body>", Pattern.DOTALL);
    private static final Pattern HTML_PATTERN = Pattern.compile("<html>");

    @Override
    public void flatMap(String content, Collector<RawWikiDocument> out ){
        Matcher htmlMatcher = HTML_PATTERN.matcher( content );
        if ( !htmlMatcher.find() ){
            LOG.trace("No html block found, assume it's not html and call superclass.");
            RawWikiDocument rawWikiDocument = super.internalFlatMap( content );
            if ( rawWikiDocument != null ){
                // try to additional content type
                setContentType( rawWikiDocument, content );
                out.collect( rawWikiDocument );
            }
            return;
        }

        Matcher titleMatcher = TITLE_PATTERN.matcher(content);
        if ( !titleMatcher.find() ){
            LOG.warn("No title tag in content but it is mandatory => Skip this content!");
            return;
        }

        String title = titleMatcher.group(1);
        LOG.debug("Create raw html from '{}'...", title);
        Matcher bodyMatcher = BODY_PATTERN.matcher( content );
        if ( !bodyMatcher.find() ){
            LOG.warn("No body in html content, therefore no text to analyze. => Skip this content!");
            return;
        }

        RawWikiDocument rawWikiDocument = new RawWikiDocument( title, 0, bodyMatcher.group(1) );
        setContentType( rawWikiDocument, content );
        out.collect( rawWikiDocument );
    }

    private void setContentType( @NotNull RawWikiDocument raw, String content ){
        ContentType ct = ContentType.getContentTypeByText( content );
        raw.setType( ct );
    }

}