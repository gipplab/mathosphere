package com.formulasearchengine.mathosphere.mlp.pojos;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formulasearchengine.mathosphere.mlp.cli.BaseConfig;
import com.formulasearchengine.mathosphere.mlp.text.WikiTextUtils.MathMarkUpType;
import com.google.common.collect.Multiset;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import static com.formulasearchengine.mathosphere.mlp.text.MathMLUtils.extractIdentifiers;
import static com.formulasearchengine.mathosphere.mlp.text.MathMLUtils.extractIdentifiersFromMathML;


public class MathTag {
    private static final Logger logger = LogManager.getLogger(MathTag.class.getName());

    public final static Pattern FORMULA_PATTERN =
            Pattern.compile("FORMULA_[0-9a-f+]");
    private static final HashFunction HASHER = Hashing.goodFastHash(64);
    private final int position;
    private final String content;
    private final MathMarkUpType markUpType;
    private Multiset<String> indentifiers = null;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public MathTag(int position, String content, MathMarkUpType markUp) {
        this.position = position;
        this.content = content;
        this.markUpType = markUp;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @JsonGetter("input")
    public String getContent() {
        return content;
    }

    @JsonGetter("inputhash")
    public String getContentHash() {
        return HASHER.hashString(content, StandardCharsets.UTF_8).toString();
    }

    @Deprecated
    public Multiset<String> getIdentifier(boolean useTeX, boolean useBlacklist) {
        return extractIdentifiersFromMathML(getContent(), useTeX, useBlacklist);
    }

    public Multiset<String> getIdentifiers(BaseConfig config) {
        if (indentifiers == null || indentifiers.size() == 0) {
            indentifiers = extractIdentifiers(this, config.getUseTeXIdentifiers(), config.getTexvcinfoUrl());
        }
        return indentifiers;
    }

    @JsonIgnore
    public String getKey() {
        return placeholder();
    }

    @JsonGetter("type")
    public MathMarkUpType getMarkUpType() {
        return markUpType;
    }

    @JsonIgnore
    public int getPosition() {
        return position;
    }

    @JsonIgnore
    public String getTagContent() {
        return content.replaceAll("<math.*?>", "").replaceAll("</math>", "");
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @JsonIgnore
    public String placeholder() {
        return "FORMULA_" + getContentHash();
    }

    public String toJson()  {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            logger.error("Can't serialize to JSON object: " + this, e);
            return "";
        }
    }

    @Override
    public String toString() {
        return "MathTag [position=" + position + ", content=" + content + "]";
    }
}

