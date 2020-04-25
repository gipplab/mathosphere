package com.formulasearchengine.mathosphere.mlp.pojos;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.translate.AggregateTranslator;
import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.EntityArrays;
import org.apache.commons.text.translate.LookupTranslator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RawWikiDocument extends RawDocument {
  private static final Pattern TITLE_PATTERN = Pattern.compile("<title>(.+?)</title>", Pattern.DOTALL);
  private static final Pattern NAMESPACE_PATTERN = Pattern.compile("<ns>(\\d+)</ns>", Pattern.DOTALL);
  private static final Pattern TEXT_PATTERN = Pattern.compile("<text(.*?)>(.+?)</text>", Pattern.DOTALL);

  private static final CharSequenceTranslator BASIC_PRE_TRANSLATOR = new AggregateTranslator(
          new LookupTranslator(EntityArrays.BASIC_UNESCAPE)
  );

  private static final CharSequenceTranslator TRANSLATOR = new AggregateTranslator(
          new LookupTranslator(EntityArrays.ISO8859_1_UNESCAPE),
          new LookupTranslator(EntityArrays.HTML40_EXTENDED_UNESCAPE)
  );

  private int namespace;

  public RawWikiDocument(String singleDoc) {
    super();
    setMeta(singleDoc);
    setContentInternal(singleDoc);
  }

  public RawWikiDocument(String title, int namespace, String content) {
    super(title, ""+namespace, content);
    this.namespace = namespace;
  }

  private void setMeta(String page) {
    Matcher titleMatcher = TITLE_PATTERN.matcher(page);
    if ( titleMatcher.find() ) {
      setTitle(titleMatcher.group(1));
    } else {
      setTitle("unknown-title");
    }

    Matcher nsMatcher = NAMESPACE_PATTERN.matcher(page);
    if ( nsMatcher.find() ) {
      setNamespace(nsMatcher.group(1));
    } else {
      setNamespace(""+Integer.MIN_VALUE);
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
  private void setContentInternal(String wikitext) {
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
    setContent(sb.toString());
  }

  @Override
  public void setNamespace(String namespace) {
    try {
      this.namespace = Integer.parseInt(namespace);
    } catch (NumberFormatException nfe) {
      throw new IllegalArgumentException("Wiki documents have only " +
              "integer namespaces but received " + namespace);
    }
  }

  public int getWikiNamespace() {
    return namespace;
  }

  public static String unescapeText(String content) {
    return TRANSLATOR.translate(BASIC_PRE_TRANSLATOR.translate(content));
  }

  @Override
  public String toString() {
    return "[title=" + getTitle() + ", text=" + StringUtils.abbreviate(getContent(), 100) + "]";
  }
}
