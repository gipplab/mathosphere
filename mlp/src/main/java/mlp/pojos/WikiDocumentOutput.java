package mlp.pojos;

import com.google.common.collect.Multiset;

import java.util.List;
import java.util.Set;

public class WikiDocumentOutput {

  private String title;
  private List<Relation> relations;
  private Set<Multiset.Entry<String>> identifiers;

  public WikiDocumentOutput() {
  }

  public WikiDocumentOutput(String title, List<Relation> relations, Multiset<String> identifiers) {
    this.title = title;
    this.relations = relations;
    this.identifiers = identifiers.entrySet();
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

  public Set<Multiset.Entry<String>> getIdentifiers() {
    return identifiers;
  }

  public void setIdentifiers(Set<Multiset.Entry<String>> identifiers) {
    this.identifiers = identifiers;
  }

}
