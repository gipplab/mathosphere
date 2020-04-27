package com.formulasearchengine.mathosphere.mlp.text;

import com.formulasearchengine.mathosphere.mlp.pojos.*;
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
import com.formulasearchengine.mathosphere.mlp.rus.RusPosAnnotator;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

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

  private static final Logger LOGGER = LogManager.getLogger(PosTagger.class.getName());

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

  /**
   * Performs POS-Tagging via CoreNLP lib and returns a list of sentences (a sentence is a list of words).
   * @param sections the cleaned wikitext (formulae replaced by FORMULA tags etc)
   * @param lib the meta information library will be updated with new positions according to their appearance after
   *            tokenize by the StanfordNLP tagger.
   * @return POS-tagged list of sentences.
   */
  public List<List<List<Word>>> annotate(List<String> sections, DocumentMetaLib lib) {
    List<List<List<Word>>> result = Lists.newArrayList();
    for ( int sec = 0; sec < sections.size(); sec++ ) {
      String cleanText = sections.get(sec);
      Annotation document = new Annotation(cleanText);
      nlpPipeline.annotate(document);
      List<List<Word>> sentences = Lists.newArrayList();

      for (CoreMap sentence : document.get(SentencesAnnotation.class)) {
        List<Word> words = Lists.newArrayList();

        final List<CoreLabel> coreLabels = sentence.get(TokensAnnotation.class);
        for ( int i = 0; i < coreLabels.size(); i++ ) {
          CoreLabel token = coreLabels.get(i);
          String textToken = token.get(TextAnnotation.class);
          String pos = token.get(PartOfSpeechAnnotation.class);
          SpecialToken specialToken = null;
          Position p = new Position(result.size(), sentences.size(), words.size());
          p.setDocumentLib(lib);
          // underscores are split in v3.9.2, see: https://github.com/stanfordnlp/CoreNLP/issues/942
          if (textToken.startsWith(PlaceholderLib.FORMULA)) {
            String underscore = handleUnderscore(coreLabels, i);
            if ( underscore != null ){
              i = i+2; // skip underscore tokens
              textToken += underscore;
            }
            specialToken = lib.getFormulaLib().get(textToken);
            words.add(new Word(p, textToken, PosTag.MATH));
          } else if (SYMBOLS.contains(textToken)) {
            words.add(new Word(p, textToken, PosTag.SYMBOL));
          } else if (BRACKET_CODES.containsKey(textToken)) {
            words.add(new Word(p, BRACKET_CODES.get(textToken), pos));
          } else if (textToken.startsWith(PlaceholderLib.LINK)) {
            String underscore = handleUnderscore(coreLabels, i);
            if ( underscore != null ){
              i = i+2; // skip underscore tokens
              textToken += underscore;
            }
            specialToken = lib.getLinkLib().get(textToken);
            words.add(new Word(p, specialToken.getContent(), PosTag.LINK));
          } else if (textToken.startsWith(PlaceholderLib.CITE)) {
            String underscore = handleUnderscore(coreLabels, i);
            if ( underscore != null ){
              i = i+2; // skip underscore tokens
              textToken += underscore;
            }
            specialToken = lib.getCiteLib().get(textToken);
            words.add(new Word(p, textToken, PosTag.CITE));
          } else if ( textToken.toLowerCase().matches("polynomials?") ) {
            words.add(new Word(p, textToken, PosTag.NOUN));
          } else {
            words.add(new Word(p, textToken, pos));
          }
          if ( specialToken != null ) {
            specialToken.addPosition(p);
          }
        }
        sentences.add(words);
      }
      result.add(sentences);
    }

    lib.setDocumentLength(result);
    return result;
  }

  /**
   * Since 3.9.2, underscore expressions are split (e.g., LINK_3 is split into three tokens LINK, _, 3).
   * It was promised to be fixed in an issue: https://github.com/stanfordnlp/CoreNLP/issues/942
   * However, the latest release is still 3.9.2, even though they working on release 4.0.0
   */
  private String handleUnderscore(List<CoreLabel> coreLabels, int i) {
    if ( i >= coreLabels.size()-2 ) return null;
    CoreLabel next = coreLabels.get(i+1);
    if ( next.get(TextAnnotation.class).equals("_") ) {
      next = coreLabels.get(i+2);
      return "_" + next.get(TextAnnotation.class);
    }
    return null;
  }

  protected static List<Sentence> convertToSentences(
          List<List<List<Word>>> input,
          Map<String, MathTag> formulaIndex
  ) {
    List<Sentence> result = Lists.newArrayListWithCapacity(input.size());

    for ( int i = 0; i < input.size(); i++ ) {
      List<List<Word>> sentence = input.get(i);
      for (List<Word> words : sentence) {
        Sentence s = toSentence(i, words, formulaIndex);
        result.add(s);
      }
    }

    return result;
  }

  /**
   * Converts a given list of words (considered as a single sentence) to a list of sentences.
   * @param input
   * @param formulaIndex
   * @return
   */
  protected static Sentence toSentence(
          int section,
          List<Word> input,
          Map<String, MathTag> formulaIndex
  ) {
    List<Word> words = Lists.newArrayListWithCapacity(input.size());
    Set<String> sentenceIdentifiers = Sets.newHashSet();
    Set<MathTag> formulas = Sets.newHashSet();

    for (int position = 0; position < input.size(); position++) {
      Word w = input.get(position);
      String word = w.getWord();
      String pos = w.getPosTag();

      // there are no identifiers anymore, only MOI/complex math
//      if (allIdentifiers.contains(word) && !PosTag.IDENTIFIER.equals(pos)) {
//        words.add(new Word(word, PosTag.IDENTIFIER));
//        sentenceIdentifiers.add(word);
//        continue;
//      }

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
        sentenceIdentifiers.addAll(formulaIdentifiers);
        words.add(w);
        // only one occurrence of one single idendifier
//        if (formulaIdentifiers.size() == 1) {
//          String id = Iterables.get(formulaIdentifiers, 0);
//          LOGGER.debug("Converting formula {} to identifier {}", formula.getKey(), id);
//          words.add(new Word(id, PosTag.IDENTIFIER));
//          sentenceIdentifiers.add(id);
//        } else {
//          words.add(w);
//        }

        if (word.length() > 40) {
          String rest = word.substring(40, word.length());
          words.add(new Word(rest, PosTag.SUFFIX));
        }

        continue;
      }

      words.add(w);
    }

    return new Sentence(section, words, sentenceIdentifiers, formulas);
  }

  /**
   * Concatenate words according to their POS-tags (eg multiple nouns are merged to noun phrases).
   * @param sentences list of sentences
   * @return concatenated version of the input sentences
   */
  public static List<List<List<Word>>> concatenateTags(List<List<List<Word>>> sentences) {
    List<List<List<Word>>> results = Lists.newArrayListWithCapacity(sentences.size());

    for ( List<List<Word>> sec : sentences ) {
      List<List<Word>> innerRes = Lists.newArrayListWithCapacity(sec.size());
      for (List<Word> sentence : sec) {
        List<Word> res = concatenatePhrases(sentence);
        innerRes.add(res);
      }
      results.add(innerRes);
    }

    return results;
  }

  /**
   * See {@link #concatenateLinks(List, Set)}. Use {@link #concatenatePhrases(List)} directly.
   * @param sentence ..
   * @param allIdentifiers ..
   * @return ..
   */
  @Deprecated
  private static List<Word> concatenateSingleWordList(List<Word> sentence, Set<String> allIdentifiers) {
    // links
    List<Word> result;
    if (config.getUseTeXIdentifiers()) {
      result = sentence;
    } else {
      result = concatenateLinks(sentence, allIdentifiers);
    }

    // noun phrases
    return concatenatePhrases(result);
  }

  protected static List<Word> concatenatePhrases(List<Word> result) {
    result = concatenateSuccessiveNounsToNounSequence(result);
    result = concatenateSuccessiveAdjectives(result);
    result = concatenateTwoSuccessiveRegexTags(result, PosTag.ANY_ADJECTIVE_REGEX, PosTag.ANY_NOUN_REGEX, PosTag.NOUN_PHRASE);
    return result;
  }

  /**
   * With the new sweble parser {@link WikiTextParser}, we do not need to take of links or identifiers in quotes
   * anymore. Please do not use this function anymore.
   *
   * @param in ...
   * @param allIdentifiers ...
   * @return ...
   */
  @Deprecated
  protected static List<Word> concatenateLinks(List<Word> in, Set<String> allIdentifiers) {
    Pattern<Word> linksPattern = Pattern.create(
            pos(PosTag.QUOTE),
            anyWord().oneOrMore().captureAs("link"),
            pos(PosTag.UNQUOTE)
    );

    return linksPattern.replaceToOne(in, new TransformerToElement<Word>() {
      @Override
      public Word transform(Match<Word> match) {
        List<Word> words = match.getCapturedGroup("link");
        if (words.size() == 1 && allIdentifiers.contains("\\mathit{" + words.get(0).getWord() + "}")) {
          return new Word(joinWords(words), PosTag.IDENTIFIER);
        } else {
          return new Word(joinWords(words), PosTag.LINK);
        }
      }
    });
  }

  protected static List<Word> concatenateSuccessiveNounsToNounSequence(List<Word> in) {
    XMatcher<Word> noun = posIn(PosTag.FOREIGN_WORD, PosTag.NOUN, PosTag.NOUN_PHRASE, PosTag.NOUN_PROPER, PosTag.NOUN_PLURAL, PosTag.NOUN_PROPER_PLURAL);
    return concatenateSuccessiveTags(in, noun, PosTag.NOUN_PHRASE);
  }

  protected static List<Word> concatenateSuccessiveAdjectives(List<Word> in) {
    XMatcher<Word> adjs = posIn(PosTag.ADJECTIVE, PosTag.ADJECTIVE_COMPARATIVE, PosTag.ADJECTIVE_SUPERLATIVE);
    return concatenateSuccessiveTags(in, adjs, PosTag.ADJECTIVE_SEQUENCE);
  }

  private static List<Word> concatenateSuccessiveTags(List<Word> in, XMatcher<Word> matcher, String newTag) {
    Pattern<Word> nounPattern = Pattern.create(matcher.oneOrMore());
    return nounPattern.replaceToOne(in, new TransformerToElement<Word>() {
      @Override
      public Word transform(Match<Word> match) {
        List<Word> words = match.getMatchedSubsequence();
        if (words.size() == 1) {
          return words.get(0);
        }

        return new Word(joinWords(words), newTag);
      }
    });
  }

  protected static List<Word> concatenateTwoSuccessiveRegexTags(List<Word> in, String tag1, String tag2,
                                                      String outputTag) {
    Pattern<Word> pattern = Pattern.create(pos(tag1), pos(tag2));
    return pattern.replaceToOne(in, m -> new Word(joinWords(m.getMatchedSubsequence()), outputTag));
  }

  private static String joinWords(List<Word> list) {
    List<String> toJoin = Lists.newArrayList();
    list.forEach(w -> toJoin.add(w.getWord()));
    return StringUtils.join(toJoin, " ");
  }

  private static XMatcher<Word> pos(String tag) {
    return BeanMatchers.regex(Word.class, "posTag", tag);
  }

  private static XMatcher<Word> posIn(String... tags) {
    return BeanMatchers.in(Word.class, "posTag", ImmutableSet.copyOf(tags));
  }

  private static XMatcher<Word> anyWord() {
    return Matchers.anything();
  }
}
