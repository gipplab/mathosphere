package com.formulasearchengine.mathosphere.mlp.pojos;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RawWikiDocument {

  private static final Pattern PAGE_TOTAL_PATTERN = Pattern.compile("<page>(.+?)<revision>(.+?)</revision>.*?</page>", Pattern.DOTALL);
  private static final Pattern TEXT_PATTERN = Pattern.compile("<text.*?>(.+?)</text>", Pattern.DOTALL);
  private static final Pattern TITLE_PATTERN = Pattern.compile("<title>(.+?)</title>", Pattern.DOTALL);
  private static final Pattern NAMESPACE_PATTERN = Pattern.compile("<ns>(\\d+)</ns>", Pattern.DOTALL);

  public String title;
  public int namespace;
  public String text;

  public RawWikiDocument(){}

  public RawWikiDocument(String raw) {
    Matcher pageMatcher = PAGE_TOTAL_PATTERN.matcher(raw);
    if ( pageMatcher.find() ) {
      setMeta(pageMatcher.group(1));
      setContent(pageMatcher.group(2));
    } else {
      throw new IllegalArgumentException("Unable to parse wikitext document. Cannot find page/revision.");
    }
  }

  public RawWikiDocument(String title, int namespace, String text) {
    this.title = title;
    this.namespace = namespace;
    this.text = text;
  }

  private void setMeta(String miniPage) {
    Matcher titleMatcher = TITLE_PATTERN.matcher(miniPage);
    if ( titleMatcher.find() ) {
      this.title = titleMatcher.group(1);
    } else {
      this.title = "unknown-title";
    }

    Matcher nsMatcher = NAMESPACE_PATTERN.matcher(miniPage);
    if ( nsMatcher.find() ) {
      this.namespace = Integer.parseInt(nsMatcher.group(1));
    } else {
      this.namespace = 0;
    }
  }

  private void setContent(String page) {
    Matcher contentMatcher = TEXT_PATTERN.matcher(page);
    if ( contentMatcher.find() ) {
      this.text = contentMatcher.group(1);
    } else {
      this.text = "";
    }
  }

  @Override
  public String toString() {
    return "[title=" + title + ", text=" + StringUtils.abbreviate(text, 100) + "]";
  }

}
