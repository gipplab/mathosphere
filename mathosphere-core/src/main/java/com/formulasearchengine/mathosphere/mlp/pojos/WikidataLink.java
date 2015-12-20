package com.formulasearchengine.mathosphere.mlp.pojos;

import com.formulasearchengine.mathosphere.mlp.text.WikiTextUtils.MathMarkUpType;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.nio.charset.StandardCharsets;


public class WikidataLink {
  private static final HashFunction HASHER = Hashing.md5();
  private final int position;
  private final String content;

  public WikidataLink(int position, String content, MathMarkUpType markUp) {
    this.position = position;
    this.content = content;
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
