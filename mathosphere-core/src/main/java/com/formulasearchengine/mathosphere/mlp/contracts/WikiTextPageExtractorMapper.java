package com.formulasearchengine.mathosphere.mlp.contracts;

import com.formulasearchengine.mathosphere.mlp.pojos.RawWikiDocument;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.util.ListCollector;
import org.apache.flink.util.Collector;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class WikiTextPageExtractorMapper implements FlatMapFunction<String, RawWikiDocument> {

  private static final Logger LOGGER = LogManager.getLogger(WikiTextPageExtractorMapper.class.getName());

  private static final Pattern PAGE_PATTERN = Pattern.compile("<page>(.+?)</page>", Pattern.DOTALL);

  @Override
  public void flatMap(String content, Collector<RawWikiDocument> out) {
    Matcher pageMatcher = PAGE_PATTERN.matcher(content);

    boolean foundAtLeastOnePage = false;
    while(pageMatcher.find()) {
      foundAtLeastOnePage = true;
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

    if ( foundAtLeastOnePage ) return;

    LOGGER.info("Page extractor did not identified any pages. Consider the entire given content as one single pure wikitext instead.");
    RawWikiDocument rwd = getRawWikiDocumentFromSinglePage(content);
    out.collect(rwd);
  }

  /**
   * To support usual java streams, we have another flat map method that can be used
   * in Java's internal {@link Stream#flatMap(Function)}
   * @param content the content
   * @return the stream of elements
   */
  public Stream<RawWikiDocument> streamFlatMap(String content) {
    List<RawWikiDocument> listOfDocs = new LinkedList<>();
    Collector<RawWikiDocument> collector = new ListCollector<>(listOfDocs);
    flatMap(content, collector);
    return listOfDocs.stream();
  }

  public static RawWikiDocument getRawWikiDocumentFromSinglePage(String singlePage) {
    return new RawWikiDocument(singlePage);
  }
}
