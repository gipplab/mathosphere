package mlp.pojos;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class Formula {
    private String key;
    private String content;
    private Multiset<String> indentifiers = HashMultiset.create();;

    public Formula() {
    }

    public Formula(String key, String content, Multiset<String> identifiers) {
        this.key = key;
        this.content = content;
        this.indentifiers.addAll(identifiers);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Multiset<String> getIndentifiers() {
        return indentifiers;
    }

    @Override
    public String toString() {
        return "{key=" + key + ", indentifiers=" + indentifiers + "}";
    }
}