package mlp.pojos;

import java.util.Set;

import com.google.common.collect.Sets;

public class Formula {
    private String key;
    private String content;
    private Set<String> indentifiers = Sets.newLinkedHashSet();

    public Formula() {
    }

    public Formula(String key, String content, Set<String> identifiers) {
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

    public Set<String> getIndentifiers() {
        return indentifiers;
    }

    @Override
    public String toString() {
        return "{key=" + key + ", indentifiers=" + indentifiers + "}";
    }
}