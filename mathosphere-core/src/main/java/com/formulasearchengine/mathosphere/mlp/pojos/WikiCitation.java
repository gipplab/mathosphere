package com.formulasearchengine.mathosphere.mlp.pojos;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.formulasearchengine.mathosphere.mlp.text.PlaceholderLib;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.nio.charset.StandardCharsets;

/**
 * @author Andre Greiner-Petter
 */
public class WikiCitation implements SpecialToken {
    private static final HashFunction HASHER = Hashing.goodFastHash(64);

    private String key;
    private String content;
    private int position;

    public WikiCitation(int position, String content) {
        this(position, "", content);
    }

    public WikiCitation(int position, String key, String content) {
        this.position = position;
        this.key = key;
        this.content = content;
    }

    public String getCiteKey() {
        return key;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public String placeholder() {
        return PlaceholderLib.PREFIX_CITE+getContentHash();
    }

    @Override
    @JsonGetter("inputhash")
    public String getContentHash() {
        if ( key != null && !key.isEmpty() )
            return HASHER.hashString(key, StandardCharsets.UTF_8).toString();
        return HASHER.hashString(content, StandardCharsets.UTF_8).toString();
    }

    @Override
    public int hashCode() {
        if ( key != null && !key.isEmpty() )
            return HashCodeBuilder.reflectionHashCode(key);
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
