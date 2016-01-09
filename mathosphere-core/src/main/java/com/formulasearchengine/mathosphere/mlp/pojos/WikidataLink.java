package com.formulasearchengine.mathosphere.mlp.pojos;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.nio.charset.StandardCharsets;


public class WikidataLink {
  private static final HashFunction HASHER = Hashing.md5();
  private final int position;
  private final String content;
  private String title;

  public String getTitle() {
    return title;
  }

  public WikidataLink setTitle(String title) {
    this.title = title;
    return this;
  }

  public WikidataLink(int position, String content) {
    this.position = position;
    this.content = content;
  }

  public WikidataLink(String linkName) {
    content = linkName;
    position = 0;
  }

  public int getPosition() {
    return position;
  }

  public String getContent() {
    return content;
  }

  public String getTagContent() {
    return content.replaceAll("<math.*?>", "").replaceAll("</math>", "");
  }

  public String getContentHash() {
    return HASHER.hashString(content, StandardCharsets.UTF_8).toString();
  }

  public String placeholder() {
    return "LINK_" + getContentHash();
  }

  @Override
  public String toString() {
    return "Link [position=" + position + ", content=" + content + "]";
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }
}

