package com.formulasearchengine.mathosphere.mlp.pojos;

/**
 * @author Andre Greiner-Petter
 */
public abstract class RawDocument {

    private String title;
    private String namespace;
    private String content;

    /**
     * It's only purpose is to (de)serialize the class
     */
    RawDocument() {}

    RawDocument(String title, String namespace, String content) {
        this.title = title;
        this.namespace = namespace;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getContent() {
        return content;
    }

    void setTitle(String title) {
        this.title = title;
    }

    void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    void setContent(String content) {
        this.content = content;
    }
}
