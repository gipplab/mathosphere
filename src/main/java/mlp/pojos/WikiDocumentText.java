package mlp.pojos;

import org.apache.commons.lang3.StringUtils;

public class WikiDocumentText {

    public String title;
    public int namespace;
    public String text;

    public WikiDocumentText() {
    }

    public WikiDocumentText(String title, int namespace, String text) {
        this.title = title;
        this.namespace = namespace;
        this.text = text;
    }

    @Override
    public String toString() {
        return "[title=" + title + ", text=" + StringUtils.abbreviate(text, 100) + "]";
    }

}
