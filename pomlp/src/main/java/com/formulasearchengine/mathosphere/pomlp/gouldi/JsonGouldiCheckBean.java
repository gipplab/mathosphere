package com.formulasearchengine.mathosphere.pomlp.gouldi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Andre Greiner-Petter
 */
@JsonPropertyOrder({"tree", "qid"})
public class JsonGouldiCheckBean {
    @JsonProperty("tree")
    private boolean tree;

    @JsonProperty("qid")
    private boolean qid;

    public boolean isTree() {
        return tree;
    }

    public void setTree(boolean tree) {
        this.tree = tree;
    }

    public boolean isQid() {
        return qid;
    }

    public void setQid(boolean qid) {
        this.qid = qid;
    }
}
