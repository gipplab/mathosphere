package com.formulasearchengine.mlp.evaluation;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Created by Leo on 05.10.2016.
 */
public class GoldEntry implements Serializable {
  private final String qID;
  private final String oldId;
  private final String fid;
  private final String mathInputTex;
  private final String title;
  private final List<IdentifierDefinition> definitions;


  public String getqID() {
    return qID;
  }

  public String getOldId() {
    return oldId;
  }

  public String getFid() {
    return fid;
  }

  public String getMathInputTex() {
    return mathInputTex;
  }

  public String getTitle() {
    return title;
  }

  public List<IdentifierDefinition> getDefinitions() {
    return definitions;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    GoldEntry goldEntry = (GoldEntry) o;

    if (!getqID().equals(goldEntry.getqID())) return false;
    if (!getOldId().equals(goldEntry.getOldId())) return false;
    if (!getFid().equals(goldEntry.getFid())) return false;
    if (!getMathInputTex().equals(goldEntry.getMathInputTex())) return false;
    if (!getTitle().equals(goldEntry.getTitle())) return false;
    return getDefinitions().equals(goldEntry.getDefinitions());

  }

  @Override
  public int hashCode() {
    int result = getqID().hashCode();
    result = 31 * result + getOldId().hashCode();
    result = 31 * result + getFid().hashCode();
    result = 31 * result + getMathInputTex().hashCode();
    result = 31 * result + getTitle().hashCode();
    result = 31 * result + getDefinitions().hashCode();
    return result;
  }

  public GoldEntry(String qID, String oldId, String fid, String mathInputTex, String title, List<IdentifierDefinition> definitions) {
    this.qID = qID;
    this.oldId = oldId;
    this.fid = fid;
    this.mathInputTex = mathInputTex;
    this.title = title;
    this.definitions = definitions;
  }
}
