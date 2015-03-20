package mlp.text;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import mlp.pojos.Formula;
import mlp.pojos.Sentence;
import mlp.pojos.Word;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class PosTagger {

    private static final Logger LOGGER = LoggerFactory.getLogger(PosTagger.class);
    
    private static final Set<String> SYMBOLS = ImmutableSet.of("<", "=", ">", "≥", "≤", "|", "/", "\\", "[",
            "]");
    private static final Map<String, String> BRACKET_CODES = ImmutableMap.<String, String> builder()
            .put("-LRB-", "(").put("-RRB-", ")").put("-LCB-", "{").put("-RCB-", "}").put("-LSB-", "[")
            .put("-RSB-", "]").build();

    private static final String LINK_START = "``";
    private static final String LINK_END = "''";



    public static PosTagger create(String posModelName) {
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos");
        props.put("pos.model", posModelName);
        props.put("tokenize.options", "untokenizable=firstKeep,strictTreebank3=true,"
                + "ptb3Escaping=true,escapeForwardSlashAsterisk=false");
        props.put("ssplit.newlineIsSentenceBreak", "two");
        return new PosTagger(new StanfordCoreNLP(props));
    }

    private final StanfordCoreNLP nlpPipeline;

    public PosTagger(StanfordCoreNLP nlpPipeline) {
        this.nlpPipeline = nlpPipeline;
    }

    public List<Sentence> process(String cleanText, List<Formula> formulas) {
        Map<String, Formula> formulaIndex = Maps.newHashMap();
        formulas.forEach(f -> formulaIndex.put(f.getKey(), f));

        List<List<Word>> annotated = annotate(cleanText, formulaIndex);
        List<List<Word>> processed = postprocess(annotated);
        return toSentences(processed, formulaIndex);
    }

    public List<List<Word>> annotate(String cleanText, Map<String, Formula> formulas) {
        Annotation document = new Annotation(cleanText);
        nlpPipeline.annotate(document);

        List<List<Word>> result = Lists.newArrayList();
        for (CoreMap sentenceAnnotation : document.get(SentencesAnnotation.class)) {
            List<Word> sentence = processSentence(sentenceAnnotation);
            result.add(sentence);
        }

        return result;
    }

    public List<Word> processSentence(CoreMap sentence) {
        List<Word> words = Lists.newArrayList();

        for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
            String textToken = token.get(TextAnnotation.class);
            String pos = token.get(PartOfSpeechAnnotation.class);
            Word word = reAnnotateWord(textToken, pos);
            words.add(word);
        }

        return words;
    }

    private static Word reAnnotateWord(String word, String pos) {
        if (word.startsWith("FORMULA_")) {
            return new Word(word, PosTag.MATH);
        }

        // some signs (<, >, etc) are tagged JJ or NN, need to fix this
        if (SYMBOLS.contains(word)) {
            return new Word(word, PosTag.SYMBOL);
        }

        if (BRACKET_CODES.containsKey(word)) {
            return new Word(BRACKET_CODES.get(word), pos);
        }

        return new Word(word, pos);
    }

    public static List<Sentence> toSentences(List<List<Word>> input, Map<String, Formula> formulaIndex) {
        List<Sentence> result = Lists.newArrayListWithCapacity(input.size());

        Set<String> allIdentifiers = Sets.newHashSet();
        formulaIndex.forEach((k, v) -> allIdentifiers.addAll(v.getIndentifiers()));
        
        for (List<Word> words : input) {
            Sentence sentence = toSentence(words, formulaIndex, allIdentifiers);
            result.add(sentence);
        }
        return result;
    }

    public static Sentence toSentence(List<Word> input, Map<String, Formula> formulaIndex, Set<String> allIdentifiers) {
        List<Word> words = Lists.newArrayListWithCapacity(input.size());
        Set<String> sentenceIdentifiers = Sets.newHashSet();
        List<Formula> formulas = Lists.newArrayList();

        for (Word word : input) {
            if (allIdentifiers.contains(word.word)) {
                words.add(new Word(word.word, PosTag.IDENTIFIER));
                sentenceIdentifiers.add(word.word);
                continue;
            }

            if (PosTag.MATH.equals(word.posTag)) {
                Formula formula = formulaIndex.get(word.word);
                formulas.add(formula);

                Set<String> formulaIdentifiers = formula.getIndentifiers();
                sentenceIdentifiers.addAll(formulaIdentifiers);

                // maybe better to do it in preprocessing
                if (formulaIdentifiers.size() == 1) {
                    String id = Iterables.get(formulaIdentifiers, 0);
                    LOGGER.debug("convering formula {} to idenfier {}", formula.getKey(), id);
                    words.add(new Word(id, PosTag.IDENTIFIER));
                } else {
                    words.add(word);
                }
                continue;
            }

            words.add(word);
        }

        return new Sentence(words, sentenceIdentifiers, formulas);
    }

    public static List<List<Word>> postprocess(List<List<Word>> sentences) {
        List<List<Word>> results = Lists.newArrayListWithCapacity(sentences.size());

        for (List<Word> sentence : sentences) {
            List<Word> res = postprocessSentence(sentence);
            results.add(res);
        }

        return results;
    }

    private static List<Word> postprocessSentence(List<Word> sentence) {
        // links
        List<Word> result = concatenateLinks(sentence, PosTag.LINK);

        // aggregate noun phrases
        result = concatenateSuccessive(result, PosTag.NOUN, PosTag.NOUN_SEQUENCE);
        result = contatenateSuccessiveBy2Tags(result, PosTag.ADJECTIVE, PosTag.NOUN, PosTag.NOUN_PHRASE);
        result = contatenateSuccessiveBy2Tags(result, PosTag.ADJECTIVE, PosTag.NOUN_SEQUENCE,
                PosTag.NOUN_SEQUENCE_PHRASE);

        return result;
    }

    public static List<Word> concatenateLinks(List<Word> in, String tag) {
        List<Word> result = Lists.newArrayList();
        List<String> accumulator = Lists.newArrayList();
        boolean insideLink = false;

        for (Word word : in) {
            if (insideLink) {
                if (LINK_END.equals(word.posTag)) {
                    String joined = StringUtils.join(accumulator, " ");
                    result.add(new Word(joined, tag));
                    accumulator.clear();
                    insideLink = false;
                } else {
                    accumulator.add(word.word);
                }
            } else {
                if (LINK_START.equals(word.posTag)) {
                    insideLink = true;
                } else {
                    result.add(word);
                }
            }
        }

        if (!accumulator.isEmpty()) {
            throw new IllegalArgumentException("There is no closing quote");
        }

        return result;
    }

    public static List<Word> concatenateSuccessive(List<Word> in, String tagIn, String tagOut) {
        List<Word> result = Lists.newArrayList();
        List<String> accumulator = Lists.newArrayList();

        boolean accumulate = false;

        for (Word word : in) {
            if (accumulate) {
                if (Objects.equals(word.posTag, tagIn)) {
                    accumulator.add(word.word);
                } else {
                    if (accumulator.size() == 1) {
                        result.add(new Word(accumulator.get(0), tagIn));
                    } else {
                        String joined = StringUtils.join(accumulator, " ");
                        result.add(new Word(joined, tagOut));
                    }
                    accumulator.clear();
                    accumulate = false;
                    result.add(word);
                }
            } else {
                if (Objects.equals(word.posTag, tagIn)) {
                    accumulator.add(word.word);
                    accumulate = true;
                } else {
                    result.add(word);
                }
            }
        }

        return result;
    }

    public static List<Word> contatenateSuccessiveBy2Tags(List<Word> in, String tag1, String tag2,
            String outputTag) {
        List<Word> result = Lists.newArrayList();

        int len = in.size();
        for (int i = 0; i < len; i++) {
            Word word = in.get(i);
            if (word.posTag.equals(tag1) && i < len - 1) {
                Word next = in.get(i + 1);
                if (next.posTag.equals(tag2)) {
                    result.add(new Word(word.word + " " + next.word, outputTag));
                    i++;
                    continue;
                }
            }
            result.add(word);
        }

        return result;
    }

}
