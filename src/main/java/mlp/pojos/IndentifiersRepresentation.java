package mlp.pojos;

import java.util.List;
import java.util.Set;

public class IndentifiersRepresentation {

    private String title;
    private List<Relation> relations;
    private Set<String> identifiers;

    public IndentifiersRepresentation() {
    }

    public IndentifiersRepresentation(String document, List<Relation> relations, Set<String> identifiers) {
        this.title = document;
        this.relations = relations;
        this.setIdentifiers(identifiers);
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
