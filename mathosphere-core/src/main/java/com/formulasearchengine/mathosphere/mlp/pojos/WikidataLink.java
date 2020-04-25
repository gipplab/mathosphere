package com.formulasearchengine.mathosphere.mlp.pojos;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;


public class WikidataLink implements SpecialToken {
  private static final HashFunction HASHER = Hashing.md5();
  private final List<Position> positions;
  private final String content;
  private String title;

  public String getTitle() {
    return title;
  }

  public WikidataLink setTitle(String title) {
    this.title = title;
    return this;
  }

  public WikidataLink(String content) {
    this.positions = new LinkedList<>();
    this.content = content;
  }

  @Override
  public List<Position> getPositions() {
    return positions;
  }

  @Override
  public void addPosition(Position p) {
    positions.add(p);
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
    return "Link [position=" + positions + ", content=" + content + "]";
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

