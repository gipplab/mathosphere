package com.formulasearchengine.mathosphere.mlp.pojos;

import java.util.List;
import java.util.Set;

public class WikiDocumentOutput {

  private String title;

  public String getqId() {
    return qId;
  }

  public void setqId(String qId) {
    this.qId = qId;
  }

  private String qId;
  private List<Relation> relations;
  private Set<String> identifiers;

  public boolean isSuccess() {
    return success;
  }

  private boolean success = true;

  public WikiDocumentOutput() {
  }

  public WikiDocumentOutput(boolean s) {
    this.success = s;
  }

  public WikiDocumentOutput(String title, List<Relation> relations, Set<String> identifiers) {
    this(title, null, relations, identifiers);
  }

  public WikiDocumentOutput(String title, String qId, List<Relation> relations, Set<String> identifiers) {
    this.title = title;
    this.qId = qId;
    this.relations = relations;
    this.identifiers = identifiers;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public List<Relation> getRelations() {
    return relations;
  }

  public void setRelations(List<Relation> relations) {
    this.relations = relations;
  }

  public Set<String> getIdentifiers() {
    return identifiers;
  }

  public void setIdentifiers(Set<String> identifiers) {
    this.identifiers = identifiers;
  }

}
