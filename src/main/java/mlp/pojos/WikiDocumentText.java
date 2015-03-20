package mlp.pojos;

import org.apache.commons.lang3.StringUtils;

public class WikiDocumentText {

    public int id;
    public String title;
    public int namespace;
    public String text;

    public WikiDocumentText() {
    }

    public WikiDocumentText(int id, String title, int namespace, String text) {
        this.id = id;
        this.title = title;
        this.namespace = namespace;
        this.text = text;
    }

    @Override
    public String toString() {
        return "[id=" + id + ", title=" + title + ", text=" + StringUtils.abbreviate(text, 100) + "]";
    }

}
