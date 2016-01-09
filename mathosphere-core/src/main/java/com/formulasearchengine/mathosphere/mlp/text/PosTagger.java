package com.formulasearchengine.mathosphere.mlp.text;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

import com.alexeygrigorev.rseq.BeanMatchers;
import com.alexeygrigorev.rseq.Match;
import com.alexeygrigorev.rseq.Matchers;
import com.alexeygrigorev.rseq.Pattern;
import com.alexeygrigorev.rseq.TransformerToElement;
import com.alexeygrigorev.rseq.XMatcher;
import com.formulasearchengine.mathosphere.mlp.cli.BaseConfig;
import com.formulasearchengine.mathosphere.mlp.pojos.MathTag;
import com.formulasearchengine.mathosphere.mlp.pojos.Sentence;
import com.formulasearchengine.mathosphere.mlp.pojos.Word;
import com.formulasearchengine.mathosphere.mlp.rus.RusPosAnnotator;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class PosTagger {
  private static BaseConfig config;

  private static final Logger LOGGER = LoggerFactory.getLogger(PosTagger.class);

  private static final Set<String> SYMBOLS = ImmutableSet.of("<", "=", ">", "≥", "≤", "|", "/", "\\", "[",
      "]", "*");
  private static final Map<String, String> BRACKET_CODES = ImmutableMap.<String, String>builder()
      .put("-LRB-", "(").put("-RRB-", ")").put("-LCB-", "{").put("-RCB-", "}").put("-LSB-", "[")
      .put("-RSB-", "]").build();

  public static PosTagger create(BaseConfig cfg) {
    config = cfg;
    Properties props = new Properties();
    props.put("annotators", "tokenize, ssplit");
    props.put("tokenize.options", "untokenizable=firstKeep,strictTreebank3=true,"
        + "ptb3Escaping=true,escapeForwardSlashAsterisk=false");
    props.put("ssplit.newlineIsSentenceBreak", "two");
    props.put("maxLength", 50);
    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

    if ("en".equals(cfg.getLanguage())) {
      POSTaggerAnnotator modelBasedPosAnnotator = new POSTaggerAnnotator(config.getModel(), false);
      pipeline.addAnnotator(modelBasedPosAnnotator);
    } else if ("ru".equals(cfg.getLanguage())) {
      pipeline.addAnnotator(new RusPosAnnotator());
    } else {
      throw new IllegalArgumentException("Cannot deal with language " + config.getLanguage());
    }

    return new PosTagger(pipeline);
  }

  private final StanfordCoreNLP nlpPipeline;

  public PosTagger(StanfordCoreNLP nlpPipeline) {
    this.nlpPipeline = nlpPipeline;
  }

  public List<Sentence> process(String cleanText, List<MathTag> formulas) {
    Map<String, MathTag> formulaIndex = Maps.newHashMap();
    Set<String> allIdentifiers = Sets.newHashSet();

    formulas.forEach(f -> formulaIndex.put(f.getKey(), f));
    formulas.forEach(f -> allIdentifiers.addAll(f.getIdentifiers(config)));

    List<List<Word>> annotated = annotate(cleanText, formulaIndex, allIdentifiers);
    List<List<Word>> concatenated = concatenateTags(annotated);
    return postprocess(concatenated, formulaIndex, allIdentifiers);
  }

  public List<List<Word>> annotate(String cleanText, Map<String, MathTag> formulas,
                                   Set<String> allIdentifiers) {
    Annotation document = new Annotation(cleanText);
    nlpPipeline.annotate(document);

    List<List<Word>> result = Lists.newArrayList();
    for (CoreMap sentence : document.get(SentencesAnnotation.class)) {
      List<Word> words = Lists.newArrayList();

      final List<CoreLabel> coreLabels = sentence.get(TokensAnnotation.class);
      for (CoreLabel token : coreLabels) {
        String textToken = token.get(TextAnnotation.class);
        String pos = token.get(PartOfSpeechAnnotation.class);
        if (textToken.startsWith("FORMULA_")) {
          words.add(new Word(textToken, PosTag.MATH));
        } else if (allIdentifiers.contains(textToken)) {
          words.add(new Word(textToken, PosTag.SYMBOL));
        } else if (SYMBOLS.contains(textToken)) {
          words.add(new Word(textToken, PosTag.SYMBOL));
        } else if (BRACKET_CODES.containsKey(textToken)) {
          words.add(new Word(BRACKET_CODES.get(textToken), pos));
        } else if (textToken.startsWith("LINK_")) {
          words.add(new Word(textToken, PosTag.LINK));
        } else {
          words.add(new Word(textToken, pos));
        }
      }

      result.add(words);
    }

    return result;
  }

  public static List<Sentence> postprocess(List<List<Word>> input, Map<String, MathTag> formulaIndex,
                                           Set<String> allIdentifiers) {
    List<Sentence> result = Lists.newArrayListWithCapacity(input.size());

    for (List<Word> words : input) {
      Sentence sentence = toSentence(words, formulaIndex, allIdentifiers);
      result.add(sentence);
    }

    return result;
  }

  public static Sentence toSentence(List<Word> input, Map<String, MathTag> formulaIndex,
                                    Set<String> allIdentifiers) {
    List<Word> words = Lists.newArrayListWithCapacity(input.size());
    Set<String> sentenceIdentifiers = Sets.newHashSet();
    List<MathTag> formulas = Lists.newArrayList();

    for (Word w : input) {
      String word = w.getWord();
      String pos = w.getPosTag();

      if (allIdentifiers.contains(word) && !PosTag.IDENTIFIER.equals(pos)) {
        words.add(new Word(word, PosTag.IDENTIFIER));
        sentenceIdentifiers.add(word);
        continue;
      }

      if (PosTag.MATH.equals(pos)) {
        String formulaKey = word;
        if (word.length() > 40) {
          formulaKey = word.substring(0, 40);
        }

        MathTag formula = formulaIndex.get(formulaKey);
        if (formula == null) {
          LOGGER.warn("formula {} does not exist", word);
          words.add(w);
          continue;
        }

        formulas.add(formula);

        Multiset<String> formulaIdentifiers = formula.getIdentifiers(config);
        // only one occurrence of one single idendifier
        if (formulaIdentifiers.size() == 1) {
          String id = Iterables.get(formulaIdentifiers, 0);
          LOGGER.debug("convering formula {} to idenfier {}", formula.getKey(), id);
          words.add(new Word(id, PosTag.IDENTIFIER));
          sentenceIdentifiers.add(id);
        } else {
          words.add(w);
        }

        if (word.length() > 40) {
          String rest = word.substring(40, word.length());
          words.add(new Word(rest, PosTag.SUFFIX));
        }

        continue;
      }

      words.add(w);
    }

    return new Sentence(words, sentenceIdentifiers, formulas);
  }

  public static List<List<Word>> concatenateTags(List<List<Word>> sentences) {
    List<List<Word>> results = Lists.newArrayListWithCapacity(sentences.size());

    for (List<Word> sentence : sentences) {
      List<Word> res = postprocessSentence(sentence);
      results.add(res);
    }

    return results;
  }

  private static List<Word> postprocessSentence(List<Word> sentence) {
    // links
    List<Word> result;
    if (config.getUseTeXIdentifiers()) {
      result = sentence;
    } else {
      result = concatenateLinks(sentence);
    }

    // noun phrases
    result = concatenateSuccessiveNounsToNounSequence(result);

    result = contatenateSuccessive2Tags(result, PosTag.ADJECTIVE, PosTag.NOUN, PosTag.NOUN_PHRASE);
    result = contatenateSuccessive2Tags(result, PosTag.ADJECTIVE, PosTag.NOUN_PLURAL, PosTag.NOUN_PHRASE);
    result = contatenateSuccessive2Tags(result, PosTag.ADJECTIVE, PosTag.NOUN_SEQUENCE,
        PosTag.NOUN_SEQUENCE_PHRASE);

    return result;
  }

  public static List<Word> concatenateLinks(List<Word> in) {
    Pattern<Word> linksPattern = Pattern.create(pos(PosTag.QUOTE), anyWord().oneOrMore()
        .captureAs("link"), pos(PosTag.UNQUOTE));

    return linksPattern.replaceToOne(in, new TransformerToElement<Word>() {
      @Override
      public Word transform(Match<Word> match) {
        List<Word> words = match.getCapturedGroup("link");
        return new Word(joinWords(words), PosTag.LINK);
      }
    });
  }

  public static List<Word> concatenateSuccessiveNounsToNounSequence(List<Word> in) {
    XMatcher<Word> noun = posIn(PosTag.NOUN, PosTag.NOUN_PLURAL);
    Pattern<Word> nounPattern = Pattern.create(noun.oneOrMore());

    return nounPattern.replaceToOne(in, new TransformerToElement<Word>() {
      @Override
      public Word transform(Match<Word> match) {
        List<Word> words = match.getMatchedSubsequence();
        if (words.size() == 1) {
          return words.get(0);
        }

        return new Word(joinWords(words), PosTag.NOUN_SEQUENCE);
      }
    });

  }

  public static List<Word> contatenateSuccessive2Tags(List<Word> in, String tag1, String tag2,
                                                      String outputTag) {
    Pattern<Word> pattern = Pattern.create(pos(tag1), pos(tag2));
    return pattern.replaceToOne(in, m -> new Word(joinWords(m.getMatchedSubsequence()), outputTag));
  }

  public static String joinWords(List<Word> list) {
    List<String> toJoin = Lists.newArrayList();
    list.forEach(w -> toJoin.add(w.getWord()));
    return StringUtils.join(toJoin, " ");
  }

  public static XMatcher<Word> pos(String tag) {
    return BeanMatchers.eq(Word.class, "posTag", tag);
  }

  public static XMatcher<Word> posIn(String... tags) {
    return BeanMatchers.in(Word.class, "posTag", ImmutableSet.copyOf(tags));
  }

  public static XMatcher<Word> anyWord() {
    return Matchers.anything();
  }
}
