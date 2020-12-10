package com.formulasearchengine.mathosphere.mlp.text;

import com.formulasearchengine.mathosphere.mlp.cli.BaseConfig;
import com.formulasearchengine.mathosphere.mlp.pojos.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.GrammaticalStructure;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Andre Greiner-Petter
 */
public class TextAnnotator {

    private final PosTagger posTagger;

    public TextAnnotator(BaseConfig config) {
        this.posTagger = PosTagger.create(config);
    }

    protected List<Sentence> annotate(String text, DocumentMetaLib lib) {
        List<String> l = Lists.newArrayListWithCapacity(1);
        l.add(text);
        return annotate(l, lib);
    }

    /**
     * Annotates the given text (with PoS-tags) and returns a list of sentences,
     * including math information.
     * @param text clean text
     * @param lib ..
     * @return annotated sentences
     */
    public List<Sentence> annotate(List<String> text, DocumentMetaLib lib) {
        return posTagger.annotate(text, lib);
    }

    public String undoPosTag(String posTag) {
        String output = posTag;

        if ( posTag.matches(PosTag.ANY_NOUN_REGEX) ) return PosTag.NOUN;
        else if ( posTag.matches(PosTag.ANY_ADJECTIVE_REGEX) ) return PosTag.ADJECTIVE;

        switch (posTag) {
            case PosTag.LINK:
            case PosTag.SYMBOL:
            case PosTag.MATH:
                output = PosTag.NOUN;
                break;
        }
        return output;
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
