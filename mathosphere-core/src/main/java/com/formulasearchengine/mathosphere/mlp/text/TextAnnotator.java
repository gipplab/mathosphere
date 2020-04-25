package com.formulasearchengine.mathosphere.mlp.text;

import com.formulasearchengine.mathosphere.mlp.cli.BaseConfig;
import com.formulasearchengine.mathosphere.mlp.pojos.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.LinkedList;
import java.util.List;

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
        List<List<List<Word>>> annotated = posTagger.annotate(text, lib);
        List<List<List<Word>>> concatenated = PosTagger.concatenateTags(annotated);
        return PosTagger.convertToSentences(concatenated, lib.getFormulaLib());
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
