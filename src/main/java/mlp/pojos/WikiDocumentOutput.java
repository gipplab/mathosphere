package mlp.pojos;

import java.util.List;

import com.google.common.collect.Multiset;

public class WikiDocumentOutput {

    private String title;
    private List<Relation> relations;
    private Multiset<String> identifiers;

    public WikiDocumentOutput() {
    }

    public WikiDocumentOutput(String document, List<Relation> relations, Multiset<String> identifiers) {
        this.title = document;
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

    public Multiset<String> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Multiset<String> identifiers) {
        this.identifiers = identifiers;
    }

}
