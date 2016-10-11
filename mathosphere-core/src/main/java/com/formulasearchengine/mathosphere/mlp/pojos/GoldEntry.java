package com.formulasearchengine.mathosphere.mlp.pojos;

import java.util.List;
import java.util.Set;

/**
 * Created by Leo on 05.10.2016.
 */
public class GoldEntry {
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

  public GoldEntry(String qID, String oldId, String fid, String mathInputTex, String title, List<IdentifierDefinition> definitions) {
    this.qID = qID;
    this.oldId = oldId;
    this.fid = fid;
    this.mathInputTex = mathInputTex;
    this.title = title;
    this.definitions = definitions;
  }

}
