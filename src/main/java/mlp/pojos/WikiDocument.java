package mlp.pojos;

import java.util.List;
import java.util.Set;

public class WikiDocument {

    private String title;
    private Set<String> identifiers;
    private List<Formula> formulas;
    private List<Sentence> sentences;

    public WikiDocument() {
    }

    public WikiDocument(String title, Set<String> identifiers, List<Formula> formulas,
            List<Sentence> sentences) {
        this.title = title;
        this.identifiers = identifiers;
        this.formulas = formulas;
        this.sentences = sentences;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Sentence> getSentences() {
        return sentences;
    }

    public Set<String> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Set<String> identifiers) {
        this.identifiers = identifiers;
    }

    public List<Formula> getFormulas() {
        return formulas;
    }

    public void setFormulas(List<Formula> formulas) {
        this.formulas = formulas;
    }

    public void setSentences(List<Sentence> sentences) {
        this.sentences = sentences;
    }

}
