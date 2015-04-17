package mlp.pojos;

import java.util.List;

import com.google.common.collect.Multiset;

public class ParsedWikiDocument {

    private String title;
    private Multiset<String> identifiers;
    private List<Formula> formulas;
    private List<Sentence> sentences;

    public ParsedWikiDocument() {
    }

    public ParsedWikiDocument(String title, Multiset<String> identifiers, List<Formula> formulas,
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

    public Multiset<String> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Multiset<String> identifiers) {
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
