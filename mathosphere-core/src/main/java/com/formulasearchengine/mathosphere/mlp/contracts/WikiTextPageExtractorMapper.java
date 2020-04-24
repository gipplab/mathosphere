package com.formulasearchengine.mathosphere.mlp.contracts;

import com.formulasearchengine.mathosphere.mlp.pojos.RawWikiDocument;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikiTextPageExtractorMapper implements FlatMapFunction<String, RawWikiDocument> {

  private static final Logger LOGGER = LogManager.getLogger(WikiTextPageExtractorMapper.class.getName());

  private static final Pattern PAGE_PATTERN = Pattern.compile("<page>(.+?)</page>", Pattern.DOTALL);

  @Override
  public void flatMap(String content, Collector<RawWikiDocument> out) {
    Matcher pageMatcher = PAGE_PATTERN.matcher(content);

    while(pageMatcher.find()) {
      String page = pageMatcher.group(1);
      RawWikiDocument rwd = getRawWikiDocumentFromSinglePage(page);
      LOGGER.info("Processing document '{}'...", rwd.getTitle());

      if ( rwd.getWikiNamespace() != 0 ) {
        LOGGER.warn("Skip document '{}' because the namespace is different to 0.", rwd.getTitle());
        // skip docs from namespaces other than 0
        continue;
      }

      out.collect(rwd);
    }
  }

  public static RawWikiDocument getRawWikiDocumentFromSinglePage(String singlePage) {
    return new RawWikiDocument(singlePage);
  }
}
