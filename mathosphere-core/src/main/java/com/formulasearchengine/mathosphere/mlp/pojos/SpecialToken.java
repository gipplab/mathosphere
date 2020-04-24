package com.formulasearchengine.mathosphere.mlp.pojos;

public interface SpecialToken {
    int getPosition();

    String getContent();

    String placeholder();

    String getContentHash();

    int hashCode();
}
