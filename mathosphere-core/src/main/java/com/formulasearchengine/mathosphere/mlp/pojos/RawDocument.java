package com.formulasearchengine.mathosphere.mlp.pojos;

/**
 * @author Andre Greiner-Petter
 */
public abstract class RawDocument {

    private String title;
    private int namespace;
    private String content;

    /**
     * It's only purpose is to (de)serialize the class
     */
    RawDocument() {}

    RawDocument(String title, int namespace, String content) {
        this.title = title;
        this.namespace = namespace;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public int getNamespace() {
        return namespace;
    }

    public String getContent() {
        return content;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setNamespace(int namespace) {
        this.namespace = namespace;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
