package com.formulasearchengine.mathosphere.mlp.contracts;

import com.formulasearchengine.mathosphere.mlp.pojos.RawWikiDocument;

import org.apache.commons.lang3.text.translate.AggregateTranslator;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.EntityArrays;
import org.apache.commons.lang3.text.translate.LookupTranslator;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextExtractorMapper implements FlatMapFunction<String, RawWikiDocument> {

  private static final Logger LOGGER = LogManager.getLogger(TextExtractorMapper.class.getName());

  private static final Pattern TITLE_PATTERN = Pattern.compile("(?:<title>)(.*?)(?:</title>)");
  private static final Pattern NAMESPACE_PATTERN = Pattern.compile("(?:<ns>)(.*?)(?:</ns>)");
  private static final Pattern TEXT_PATTERN = Pattern.compile("(?:<text.*?>)(.*?)(?:</text>)",
      Pattern.DOTALL);

  private static final CharSequenceTranslator TRANSLATOR = new AggregateTranslator(
      new LookupTranslator(EntityArrays.ISO8859_1_UNESCAPE()),
      new LookupTranslator(EntityArrays.BASIC_UNESCAPE()),
      new LookupTranslator(EntityArrays.HTML40_EXTENDED_UNESCAPE()));

  @Override
  public void flatMap(String content, Collector<RawWikiDocument> out) throws Exception {
    Matcher titleMatcher = TITLE_PATTERN.matcher(content);
    if (!titleMatcher.find()) {
      return;
    }

    String title = titleMatcher.group(1);
    LOGGER.info("processing document '{}'...", title);

    Matcher namespaceMatcher = NAMESPACE_PATTERN.matcher(content);
    if (!namespaceMatcher.find()) {
      return;
    }

    int ns = Integer.parseInt(namespaceMatcher.group(1));
    if (ns != 0) {
      // skip docs from namespaces other than 0
      return;
    }

    // parse text
    Matcher textMatcher = TEXT_PATTERN.matcher(content);
    if (!textMatcher.find()) {
      return;
    }

    String rawText = textMatcher.group(1);
    String text = unescape(rawText);

    out.collect(new RawWikiDocument(title, ns, text));

  }

  /**
   * Unescapes special entity char sequences like &lt; to its UTF-8 representation. All ISO-8859-1,
   * HTML4 and Basic entities will be translated.
   *
   * @param text the text that will be unescaped
   * @return the unescaped version of the string text
   */
  public static String unescape(String text) {
    return TRANSLATOR.translate(text);
  }

}
