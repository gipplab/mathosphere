package com.formulasearchengine.mathosphere.mlp.pojos;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formulasearchengine.mathosphere.mlp.cli.BaseConfig;
import com.formulasearchengine.mathosphere.mlp.text.PosTag;
import com.formulasearchengine.mathosphere.mlp.text.WikiTextUtils.MathMarkUpType;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import gov.nist.drmf.interpreter.common.latex.TeXPreProcessor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.formulasearchengine.mathosphere.mlp.text.MathMLUtils.extractIdentifiers;
import static com.formulasearchengine.mathosphere.mlp.text.MathMLUtils.extractIdentifiersFromMathML;


public class MathTag implements SpecialToken {
    private static final Logger logger = LogManager.getLogger(MathTag.class.getName());

    public static final Pattern FORMULA_PATTERN = Pattern.compile("FORMULA_[a-zA-Z0-9]{32}");

    private static final HashFunction HASHER = Hashing.goodFastHash(64);
    private final List<Position> positions;
    private String content;
    private final MathMarkUpType markUpType;
    private Multiset<String> identifiers = null;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public MathTag(String content, MathMarkUpType markUp) {
        this.positions = new LinkedList<>();
        this.content = content.trim();
        if ( content.endsWith("\\") ) this.content = this.content.substring(0, this.content.length()-1);
        this.markUpType = markUp;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @JsonGetter("input")
    @Override
    public String getContent() {
        return content;
    }

    @JsonGetter("inputhash")
    public String getContentHash() {
        return hash(content);
    }

    @Deprecated
    public Multiset<String> getIdentifier(boolean useTeX, boolean useBlacklist) {
        return extractIdentifiersFromMathML(getContent(), useTeX, useBlacklist);
    }

    public Multiset<String> getIdentifiers() {
        if ( identifiers == null )
            throw new NullPointerException("No identifiers found. Use getIdentifiers with config method to retrieve them.");
        return identifiers;
    }

    public Multiset<String> getIdentifiers(BaseConfig config) {
        if (identifiers == null) {
            // TODO we might don't want to differ between tex/mathml anymore. decide on the fly instead
            identifiers = extractIdentifiers(this, config.getUseTeXIdentifiers(), config.getTexvcinfoUrl());
        }
        return identifiers;
    }

    public boolean containsIdentifier(Collection<String> identifier) {
        return getIdentifiers().containsAll(identifier);
    }

    public boolean containsIdentifier(String identifier, BaseConfig config) {
        return getIdentifiers(config).contains(identifier);
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
    public List<Position> getPositions() {
        return positions;
    }

    @JsonIgnore
    public Set<Position> getPositionsInSentence(Sentence s) {
        Set<Position> pos = new HashSet<>();
        if ( s.getWords().isEmpty() ) return pos;
        Position sentencePos = s.getWords().get(0).getPosition();
        return positions.stream().filter( p -> Position.inSameSentence(p, sentencePos) ).collect(Collectors.toSet());
    }

    @JsonIgnore
    public List<Word> getWordsInSentence(Sentence s) {
        List<Word> words = new LinkedList<>();
        if ( s.getWords().isEmpty() ) return words;
        for ( Word w : s.getWords() ) {
            if ( w.getPosTag().equals(PosTag.MATH) && w.getWord().equals(placeholder()) ) {
                words.add(w);
            }
        }
        return words;
    }

    public void addPosition(Position p) {
        this.positions.add(p);
    }

    @JsonIgnore
    public String getTagContent() {
        return clean(content);
    }

    private static final Pattern EOM_PATTERN = Pattern.compile("\\\\([,;.!]|$)");

    private String clean(String in) {
        in = in.replaceAll("<math.*?>", "").replaceAll("</math>", "");
        StringBuilder sb = new StringBuilder();
        Matcher m = EOM_PATTERN.matcher(in);
        while ( m.find() ) {
            m.appendReplacement(sb, "");
        }
        m.appendTail(sb);
        return TeXPreProcessor.preProcessingTeX(sb.toString());
    }

    public void extendContent(String extension) {
        // should be only supported for tex...
        if ( MathMarkUpType.MATHML.equals(markUpType) ) {
            throw new IllegalArgumentException("Extend the content of MathML is not supported.");
        }
        if ( content != null && !content.isBlank() ) {
            if ( content.matches(".*\\\\[a-zA-Z]+$") && extension.matches("^[a-zA-Z].*") )
                extension = " " + extension;
        }
        content += extension;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @JsonIgnore
    public String placeholder() {
        return getID(content);
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
        return "MathTag [position=" + positions + ", content=" + content + "]";
    }



    /**
     * Builds a map from ID to MathTag
     * @param math a list of math tags
     * @return a map where the IDs of the math tags are the keys
     */
    public static Map<String, MathTag> getMathIDMap(List<MathTag> math) {
        Map<String, MathTag> formulaIndex = Maps.newHashMap();
        math.forEach(
                f -> formulaIndex.put(f.getKey(), f)
        );
        return formulaIndex;
    }

    /**
     * Retrieves all identifiers from all math expressions (no duplicates).
     * @param math math tags
     * @param config config specifies how to retrieve the identifier
     * @return the set of retrieved identifiers (no duplicates)
     */
    public static Set<String> getAllIdentifier(Map<String, MathTag> math, BaseConfig config) {
        Set<String> allIdentifiers = Sets.newHashSet();
        math.values().forEach(f -> allIdentifiers.addAll(
                f.getIdentifiers(config)
                        .stream()
                        .map(e -> e.matches(".") ? "\\mathit{" + e + "}" : e)
                        .collect(Collectors.toList())
        ));
        return allIdentifiers;
    }

    public static String hash(String in) {
        // the old version (HASHER come from Hashing.goodFastHash(64); from import com.google.common.hash.HashFunction;)
        // was time dependant (or instance dependant, what ever...) restarting the damn VM generated other hashes... man
        //        return HASHER.hashString(content, StandardCharsets.UTF_8).toString();
        return DigestUtils.md5Hex(in);
    }

    public static String getID(String tex) {
        String hash = hash(tex);
        return "FORMULA_" + hash;
    }
}

