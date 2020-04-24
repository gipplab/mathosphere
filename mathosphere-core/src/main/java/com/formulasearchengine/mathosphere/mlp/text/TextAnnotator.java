package com.formulasearchengine.mathosphere.mlp.text;

import com.formulasearchengine.mathosphere.mlp.cli.BaseConfig;
import com.formulasearchengine.mathosphere.mlp.pojos.*;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Andre Greiner-Petter
 */
public class TextAnnotator {

    private final PosTagger posTagger;
    private final BaseConfig config;

    public TextAnnotator(BaseConfig config) {
        this.config = config;
        this.posTagger = PosTagger.create(config);
    }

    public List<Sentence> annotate(String text, List<MathTag> mathTags) {
        Map<String, MathTag> formulaIndex = MathTag.getMathIDMap(mathTags);
        return annotate(text, formulaIndex);
    }

    /**
     * Annotates the given text (PoS-Tags) and returns a list of sentences,
     * including identifier.
     * @param text clean text
     * @param formulaIndex list of all math expressions
     * @return list of annotated sentences
     */
    public List<Sentence> annotate(String text, Map<String, MathTag> formulaIndex) {
        Set<String> identifiers = MathTag.getAllIdentifier(formulaIndex, config);
        return annotate(text, formulaIndex, identifiers);
    }

    /**
     * Annotates the given text (with PoS-tags) and returns a list of sentences,
     * including math information.
     * @param text clean text
     * @param formulaIndex all formulae (keys are MathTag IDs) that appear in the text
     * @param identifiers a set of all existing identifiers in the text
     * @return annotated sentences
     */
    public List<Sentence> annotate(String text, Map<String, MathTag> formulaIndex, Set<String> identifiers) {
        List<List<Word>> annotated = posTagger.annotate(text);
        List<List<Word>> concatenated = PosTagger.concatenateTags(annotated, identifiers);
        return PosTagger.convertToSentences(concatenated, formulaIndex, identifiers);
    }

    /**
     * Replaces all placeholder tokens (eg FORMULA_1...) by its content
     * @param words list of words
     * @param lib lib of all placeholders
     * @return list of words all replaced by its actual content
     */
    public static List<Word> unwrapPlaceholder(List<Word> words, DocumentMetaLib lib) {
        List<Word> replaced = new LinkedList<>();
        words.forEach(
                w -> {
                    String s = w.getWord();
                    SpecialToken t = null;
                    Word newWord;
                    if ( s.startsWith(PlaceholderLib.PREFIX_FORMULA) ) {
                        t = lib.getFormulaLib().get(s);
                    } else if ( s.startsWith(PlaceholderLib.PREFIX_LINK) ) {
                        t = lib.getLinkLib().get(s);
                    } else if ( s.startsWith(PlaceholderLib.PREFIX_CITE) ) {
                        t = lib.getCiteLib().get(s);
                    }
                    if ( t != null )
                        newWord = new Word(t.getContent(), w.getPosTag());
                    else newWord = w;
                    replaced.add(newWord);
                }
        );
        return replaced;
    }
}
