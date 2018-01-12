package com.formulasearchengine.mathosphere.mlp.pojos;

import org.apache.commons.lang3.StringUtils;

public class RawWikiDocument {

  public String title;
  public int namespace;
  public String text;

  // the content type - might be null but that's fine.
  private ContentType type;

  public RawWikiDocument() {}

  public RawWikiDocument(String title, int namespace, String text) {
    this.title = title;
    this.namespace = namespace;
    this.text = text;
  }

  @Override
  public String toString() {
    return "[title=" + title + ", text=" + StringUtils.abbreviate(text, 100) + "]";
  }

  public ContentType getType() {
    return type;
  }

  public void setType(ContentType type) {
    this.type = type;
  }
}
