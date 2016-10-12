package com.formulasearchengine.mathosphere.mlp.pojos;

import com.formulasearchengine.mathosphere.mlp.evaluation.EvaluatedRelation;

/**
 * Created by Leo on 04.10.2016.
 */
public class IdentifierDefinition {
  private String identifier;

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public String getDefinition() {
    return definition;
  }

  public void setDefinition(String definition) {
    this.definition = definition;
  }

  private String definition;

  public IdentifierDefinition(String identifier, String definition) {
    this.identifier = identifier;
    this.definition = definition.toLowerCase();
  }

  public IdentifierDefinition(EvaluatedRelation evaluatedRelation){
    this.identifier = evaluatedRelation.getIdentifier();
    this.definition = evaluatedRelation.getDefinition();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    IdentifierDefinition that = (IdentifierDefinition) o;

    if (getIdentifier() != null ? !getIdentifier().equals(that.getIdentifier()) : that.getIdentifier() != null)
      return false;
    return getDefinition() != null ? getDefinition().equals(that.getDefinition()) : that.getDefinition() == null;

  }

  @Override
  public int hashCode() {
    int result = getIdentifier() != null ? getIdentifier().hashCode() : 0;
    result = 31 * result + (getDefinition() != null ? getDefinition().hashCode() : 0);
    return result;
  }
}
