package com.formulasearchengine.mathosphere.utils;

import com.formulasearchengine.mathosphere.mlp.cli.BaseConfig;
import com.formulasearchengine.mathosphere.mlp.pojos.Relation;
import com.formulasearchengine.mathosphere.mlp.pojos.Word;
import com.formulasearchengine.mathosphere.mlp.rus.RusPosAnnotator;
import com.formulasearchengine.mathosphere.mlp.text.PosTagger;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import weka.core.*;
import weka.core.tokenizers.NGramTokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.formulasearchengine.mathosphere.mlp.text.MyPatternMatcher.match;

/**
 * Created by Leo on 21.12.2016.
 */
public class WekaUtils {
  public static final String MATCH = "match";
  public static final String NO_MATCH = "no match";
  public static final int ATTRIBUTE_SENTENCE = 37;
  public static final String DEFINIENS_POS = "definiensPos";
  public static final String IDENTIFIER_POS = "identifierPos";
  public static final String DEFINIEN = "definiens";
  public static final String IDENTIFIER = "identifier";
  public static final String Q_ID = "qId";
  public static final String TITLE = "title";
  public static final String WORD_DISTANCE = "wordDistance";
  public static final String WORD_POSITIONING = "wordPositioning";
  public static final String DEFINIENS_LENGTH = "definiensLength";
  public static final String PATTERN_1 = "pattern1";
  public static final String PATTERN_2 = "pattern2";
  public static final String PATTERN_3 = "pattern3";
  public static final String PATTERN_4 = "pattern4";
  public static final String PATTERN_5 = "pattern5";
  public static final String PATTERN_6 = "pattern6";
  public static final String PATTERN_7 = "pattern7";
  public static final String PATTERN_8 = "pattern8";
  public static final String PATTERN_9 = "pattern9";
  public static final String PATTERN_10 = "pattern10";
  public static final String PATTERN_11 = "pattern11";
  public static final String PATTERN_12 = "pattern12";
  public static final String PATTERN_13 = "pattern13";
  public static final String PATTERN_14 = "pattern14";
  public static final String PATTERN_15 = "pattern15";
  public static final String PATTERN_16 = "pattern16";
  public static final String SURFACE_TEXT_AND_POS_TAG_OF_TWO_PRECEDING_AND_FOLLOWING_TOKENS_AROUND_THE_DESC_CANDIDATE = "Surface text and POS tag of two preceding and following tokens around the desc candidate";
  public static final String SURFACE_TEXT_AND_POS_TAG_OF_THREE_PRECEDING_AND_FOLLOWING_TOKENS_AROUND_THE_PAIRED_MATH_EXPR = "Surface text and POS tag of three preceding and following tokens around the paired math expr";
  public static final String SURFACE_TEXT_OF_THE_FIRST_VERB_THAT_APPEARS_BETWEEN_THE_DESC_CANDIDATE_AND_THE_TARGET_MATH_EXPR = "Surface text of the first verb that appears between the desc candidate and the target math expr";
  public static final String NULL_3 = "null3";
  public static final String NULL_4 = "null4";
  public static final String NULL_5 = "null5";
  public static final String NULL_6 = "null6";
  public static final String NULL_7 = "null7";
  public static final String NULL_8 = "null8";
  public static final String NULL_9 = "null9";
  public static final String NULL_10 = "null10";
  public static final String NULL_11 = "null11";
  public static final String SENTENCE = "sentence";
  public static final String CLASSIFICATION = "classification";

  public static Instances createInstances(String title) {
    ArrayList<Attribute> atts = new ArrayList<>();
    //meta information
    atts.add(new Attribute(TITLE, (FastVector) null));//0
    atts.add(new Attribute(Q_ID, (FastVector) null));//0
    atts.add(new Attribute(IDENTIFIER, (FastVector) null));//1
    atts.add(new Attribute(DEFINIEN, (FastVector) null));//2
    atts.add(new Attribute(IDENTIFIER_POS));//3
    atts.add(new Attribute(DEFINIENS_POS));//4
    atts.add(new Attribute(WORD_DISTANCE));//5
    atts.add(new Attribute(WORD_POSITIONING));//6
    atts.add(new Attribute(DEFINIENS_LENGTH));//7
    atts.add(new Attribute(PATTERN_1));//8
    atts.add(new Attribute(PATTERN_2));//9
    atts.add(new Attribute(PATTERN_3));//10
    atts.add(new Attribute(PATTERN_4));//11
    atts.add(new Attribute(PATTERN_5));//12
    atts.add(new Attribute(PATTERN_6));//13
    atts.add(new Attribute(PATTERN_7));//14
    atts.add(new Attribute(PATTERN_8));//15
    atts.add(new Attribute(PATTERN_9));//16
    atts.add(new Attribute(PATTERN_10));//17
    atts.add(new Attribute(PATTERN_11));//18
    atts.add(new Attribute(PATTERN_12));//19
    atts.add(new Attribute(PATTERN_13));//20
    atts.add(new Attribute(PATTERN_14));//21
    atts.add(new Attribute(PATTERN_15));//22
    atts.add(new Attribute(PATTERN_16));//23
    atts.add(new Attribute(SURFACE_TEXT_AND_POS_TAG_OF_TWO_PRECEDING_AND_FOLLOWING_TOKENS_AROUND_THE_DESC_CANDIDATE, (FastVector) null));//24
    atts.add(new Attribute(SURFACE_TEXT_AND_POS_TAG_OF_THREE_PRECEDING_AND_FOLLOWING_TOKENS_AROUND_THE_PAIRED_MATH_EXPR, (FastVector) null));//25
    atts.add(new Attribute(SURFACE_TEXT_OF_THE_FIRST_VERB_THAT_APPEARS_BETWEEN_THE_DESC_CANDIDATE_AND_THE_TARGET_MATH_EXPR, (FastVector) null));//26
    atts.add(new Attribute(NULL_3));//27
    atts.add(new Attribute(NULL_4));//28
    atts.add(new Attribute(NULL_5));//29
    atts.add(new Attribute(NULL_6));//30
    atts.add(new Attribute(NULL_7));//31
    atts.add(new Attribute(NULL_8));//32
    atts.add(new Attribute(NULL_9));//33
    atts.add(new Attribute(NULL_10));//34
    atts.add(new Attribute(NULL_11));//35
    //this is where the real attrs begin
    atts.add(new Attribute(SENTENCE, (FastVector) null));//36
    //TODO expand
    //classification
    ArrayList nominal = new ArrayList();
    nominal.add(MATCH);
    nominal.add(NO_MATCH);
    atts.add(new Attribute(CLASSIFICATION, nominal));//36
    Instances result = new Instances(title, atts, 0);
    result.setClassIndex(result.numAttributes() - 1);
    return result;
  }

  public static Instances addRelationsToInstances(List<Relation> relations, String title, String qId, Instances instances) {
    List nominal = new ArrayList();
    nominal.add(MATCH);
    nominal.add(NO_MATCH);
    for (Relation relation : relations) {
      int[] patternMatches = match(relation.getSentence(), relation.getIdentifier(), relation.getDefinition(), relation.getIdentifierPosition(), relation.getWordPosition());
      double[] values = new double[instances.numAttributes()];
      addStringValue(values, instances, TITLE, title);
      addStringValue(values, instances, Q_ID, qId);
      addStringValue(values, instances, IDENTIFIER, relation.getIdentifier());
      addStringValue(values, instances, DEFINIEN, relation.getDefinition());
      values[instances.attribute(IDENTIFIER_POS).index()] = relation.getIdentifierPosition();
      values[instances.attribute(DEFINIENS_POS).index()] = relation.getWordPosition();
      //distance between identifier and definiens candidate
      values[instances.attribute(WORD_DISTANCE).index()] = Math.abs(relation.getIdentifierPosition() - relation.getWordPosition());
      //weather or not the definiens is before or after the identifier
      values[instances.attribute(WORD_POSITIONING).index()] = Math.signum(relation.getIdentifierPosition() - relation.getWordPosition());
      values[instances.attribute(DEFINIENS_LENGTH).index()] = relation.getDefinition().length();
      for (int i = 0; i < patternMatches.length; i++) {
        values[instances.attribute(PATTERN_1).index() + i] = patternMatches[i];
      }
      //Surface text and POS tag of two preceding and following tokens around the desc candidate
      String twoBeforeAndAfter =
        wordListToSimpleString(relation.getSentence().getWords().subList(Math.max(0, relation.getWordPosition() - 2), relation.getWordPosition())
        )
          + " " +
          wordListToSimpleString(relation.getSentence().getWords().subList(relation.getWordPosition() + 1, Math.min(relation.getWordPosition() + 2, relation.getSentence().getWords().size()))
          );
      addStringValue(values, instances, SURFACE_TEXT_AND_POS_TAG_OF_TWO_PRECEDING_AND_FOLLOWING_TOKENS_AROUND_THE_DESC_CANDIDATE, twoBeforeAndAfter);
      //Surface text and POS tag of three preceding and following tokens around the paired math expr
      String threePrecedingAndFollowing =
        wordListToSimpleString(relation.getSentence().getWords()
          .subList(Math.max(0, relation.getIdentifierPosition() - 3), relation.getIdentifierPosition())
        ) + " " +
          wordListToSimpleString(relation.getSentence().getWords()
            .subList(relation.getIdentifierPosition() + 1, Math.min(relation.getIdentifierPosition() + 3, relation.getSentence().getWords().size()))
          );
      addStringValue(values, instances, SURFACE_TEXT_AND_POS_TAG_OF_THREE_PRECEDING_AND_FOLLOWING_TOKENS_AROUND_THE_PAIRED_MATH_EXPR, threePrecedingAndFollowing);
      List<Word> wordsInbetween;
      if (relation.getIdentifierPosition() > relation.getWordPosition()) {
        wordsInbetween = relation.getSentence().getWords().
          subList(relation.getWordPosition() + 1, relation.getIdentifierPosition());
      } else {
        wordsInbetween = relation.getSentence().getWords().
          subList(relation.getIdentifierPosition() + 1, relation.getWordPosition());
      }
      //Surface text of the first verb that appears between the desc candidate and the target math expr
      Optional<Word> firstVerb = wordsInbetween.stream().filter(w -> w.getPosTag().startsWith("VB")).findFirst();
      if (firstVerb.isPresent()) {
        addStringValue(values, instances, SURFACE_TEXT_OF_THE_FIRST_VERB_THAT_APPEARS_BETWEEN_THE_DESC_CANDIDATE_AND_THE_TARGET_MATH_EXPR, firstVerb.get().getWord());
      }
      //text between the tokens
      values[ATTRIBUTE_SENTENCE] = instances.attribute(ATTRIBUTE_SENTENCE).addStringValue(wordListToSimpleString(wordsInbetween));
      //values[ATTRIBUTE_SENTENCE] = instances.attribute(ATTRIBUTE_SENTENCE).addStringValue("");
      values[values.length - 1] = relation.getRelevance() > 1 ? nominal.indexOf(MATCH) : nominal.indexOf(NO_MATCH);
      instances.add(new DenseInstance(1.0, values));
    }
    return instances;
  }

  private static void addStringValue(double[] data, Instances instances, String field, String string) {
    data[instances.attribute(field).index()] = instances.attribute(field).addStringValue(string);
  }

  private static String wordListToSimpleString(List<Word> words) {
    StringBuilder stringBuilder = new StringBuilder();
    for (Word w : words) {
      stringBuilder.append(w.toString());
      stringBuilder.append(" ");
    }
    return stringBuilder.toString();
  }
}
