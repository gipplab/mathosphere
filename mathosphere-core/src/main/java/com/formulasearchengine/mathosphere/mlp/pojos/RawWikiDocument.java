package com.formulasearchengine.mathosphere.mlp.pojos;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.translate.AggregateTranslator;
import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.EntityArrays;
import org.apache.commons.text.translate.LookupTranslator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RawWikiDocument {
  private static final Pattern TITLE_PATTERN = Pattern.compile("<title>(.+?)</title>", Pattern.DOTALL);
  private static final Pattern NAMESPACE_PATTERN = Pattern.compile("<ns>(\\d+)</ns>", Pattern.DOTALL);
  private static final Pattern TEXT_PATTERN = Pattern.compile("<text(.*?)>(.+?)</text>", Pattern.DOTALL);

  private static final CharSequenceTranslator BASIC_PRE_TRANSLATOR = new AggregateTranslator(
          new LookupTranslator(EntityArrays.BASIC_UNESCAPE)
  );

  private static final CharSequenceTranslator TRANSLATOR = new AggregateTranslator(
//          new LookupTranslator(EntityArrays.BASIC_UNESCAPE),
          new LookupTranslator(EntityArrays.ISO8859_1_UNESCAPE),
          new LookupTranslator(EntityArrays.HTML40_EXTENDED_UNESCAPE)
  );

  private String title;
  private int namespace;
  private String pageContent;

  public RawWikiDocument(){}

  public RawWikiDocument(String singleDoc) {
    setMeta(singleDoc);
    setContent(singleDoc);
  }

  public RawWikiDocument(String title, int namespace, String text) {
    this.title = title;
    this.namespace = namespace;
    this.pageContent = text;
  }

  private void setMeta(String page) {
    Matcher titleMatcher = TITLE_PATTERN.matcher(page);
    if ( titleMatcher.find() ) {
      this.title = titleMatcher.group(1);
    } else {
      this.title = "unknown-title";
    }

    Matcher nsMatcher = NAMESPACE_PATTERN.matcher(page);
    if ( nsMatcher.find() ) {
      this.namespace = Integer.parseInt(nsMatcher.group(1));
    } else {
      this.namespace = Integer.MIN_VALUE;
    }

    if ( titleMatcher.find() || nsMatcher.find() )
      throw new IllegalArgumentException("RawWikiDocument cannot handle multiple pages. " +
              "Use TextExtractorMapper instead.");
  }

  /**
   * The standard wiki dump escapes xml tags in <text> (which is the content of a page).
   * However, when escaped, the AstVisitor is not able to discover them as xml-tags.
   * This method unescapes all xml tags only within the <text></text> block.
   * @param wikitext with escaped xml strings in <text></text>
   */
  private void setContent(String wikitext) {
    Matcher textMatcher = TEXT_PATTERN.matcher(wikitext);
    StringBuffer sb = new StringBuffer();
    if ( textMatcher.find() ) {
      String attributes = textMatcher.group(1);
      String content = textMatcher.group(2);
      content = unescapeText(content);
      String newText = "<text" + attributes + ">" + content + "</text>";
      // if we append directly newText, it may through an IndexOutOfBoundsException, strange...
      textMatcher.appendReplacement(sb, "");
      sb.append(newText);
    } else {
      throw new NullPointerException("No text in this page.");
    }

    if ( textMatcher.find() )
      throw new IllegalArgumentException("Multiple text tags in a single page are not supported." +
              " Use TextExtractorMapper instead.");

    textMatcher.appendTail(sb);
    this.pageContent = sb.toString();
  }

  public String getTitle() {
    return title;
  }

  public int getNamespace() {
    return namespace;
  }

  public String getPageContent() {
    return pageContent;
  }

  public static String unescapeText(String content) {
    return TRANSLATOR.translate(BASIC_PRE_TRANSLATOR.translate(content));
  }

  @Override
  public String toString() {
    return "[title=" + title + ", text=" + StringUtils.abbreviate(pageContent, 100) + "]";
  }

}
