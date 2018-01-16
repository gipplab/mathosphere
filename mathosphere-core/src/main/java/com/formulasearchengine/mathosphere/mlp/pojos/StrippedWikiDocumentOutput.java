package com.formulasearchengine.mathosphere.mlp.pojos;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StrippedWikiDocumentOutput {

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public List<IdentifierDefinition> getRelations() {
    return relations;
  }

  public void setRelations(List<IdentifierDefinition> relations) {
    this.relations = relations;
  }

  public Set<StringEntry> getIdentifiers() {
    return identifiers;
  }

  public void setIdentifiers(Set<StringEntry> identifiers) {
    this.identifiers = identifiers;
  }

  public String title;
  public List<IdentifierDefinition> relations;
  public Set<StringEntry> identifiers;

  public StrippedWikiDocumentOutput() {}

  public StrippedWikiDocumentOutput(WikiDocumentOutput doc) {
    this.title = doc.getTitle();
    this.identifiers = doc.getIdentifiers();
    this.relations = doc
            .getRelations()
            .stream()
            .map(r -> new IdentifierDefinition(
                    r.getIdentifier(),
                    r.getDefinition(),
                    r.getScore(),
                    r.getDistanceFromFirstIdentifierOccurence(),
                    r.getIdentifierPosition(),
                    r.getWordPosition())
            )
            .collect(Collectors.toList());
  }
}
