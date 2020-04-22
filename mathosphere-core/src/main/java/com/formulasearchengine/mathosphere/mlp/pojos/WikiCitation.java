package com.formulasearchengine.mathosphere.mlp.pojos;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;

/**
 * @author Andre Greiner-Petter
 */
public class WikiCitation {
    private static final HashFunction HASHER = Hashing.goodFastHash(64);

    private String key;
    private String content;

    public WikiCitation(String key, String content) {
        this.key = key;
        this.content = content;
    }

    public WikiCitation(String content) {
        this("", content);
    }

    public String getKey() {
        return key;
    }

    public String getContent() {
        return content;
    }

    @Override
    public int hashCode() {
        return HASHER.hashString(key+content, StandardCharsets.UTF_8).asInt();
    }
}
