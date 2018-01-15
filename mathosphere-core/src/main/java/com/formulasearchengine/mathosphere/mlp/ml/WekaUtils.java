package com.formulasearchengine.mathosphere.mlp.ml;

import com.beust.jcommander.internal.Lists;
import com.formulasearchengine.mathosphere.mlp.pojos.Relation;
import com.formulasearchengine.mathosphere.mlp.pojos.Sentence;
import com.formulasearchengine.mathosphere.mlp.pojos.WikiDocumentOutput;
import com.formulasearchengine.mathosphere.mlp.pojos.Word;
import com.formulasearchengine.mathosphere.mlp.text.MachineLearningPatternMatcher;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.GrammaticalStructure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import weka.core.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Leo on 21.12.2016.
 * Source [1]: Extracting Textual Descriptions of Mathematical Expressions in Scientific Papers;Giovanni Yoko Kristianto, Goran Topić, Akiko Aizawa
 */
public class WekaUtils {
  private static final Logger LOG = LogManager.getLogger( WekaUtils.class.getName() );

  public static final String MATCH = "match";
  public static final String NO_MATCH = "no match";
  public static final String DEFINIEN = "definiens";
  public static final String IDENTIFIER = "identifier";
  public static final String Q_ID = "qId";
  public static final String TITLE = "title";
  /**
   * Feature 11 of [1]
   * Relative distance identifier - definiens in words. Normalized by {@link WikiDocumentOutput#maxSentenceLength}
   */
  public static final String WORD_DISTANCE = "wordDistance";
  /**
   * Feature 12 of [1]
   * weather or not the definiens is before or after the identifier
   */
  public static final String WORD_POSITIONING = "wordPositioning";
  public static final String PATTERN_1 = "pattern 1 identifier, definition";
  public static final String PATTERN_2 = "pattern 2 definition, identifier";
  public static final String PATTERN_3 = "pattern 3 identifier, isOrAre, definition";
  public static final String PATTERN_4 = "pattern 4 identifier, isOrAre, the, definition";
  public static final String PATTERN_5 = "pattern 5 let, identifier, be, denoted, by, definition";
  public static final String PATTERN_6 = "pattern 6 let, identifier, be, denoted, by, the_one_or_more, definition";
  public static final String PATTERN_7 = "pattern 7 definition, isOrAre, denoted, by, identifier";
  public static final String PATTERN_8 = "pattern 8 definition, isOrAre, denoted, by, the_one_or_more, identifier";
  public static final String PATTERN_9 = "pattern 9 identifier, denotes, definition";
  public static final String PATTERN_10 = "pattern 10 identifier, denotes, the_one_or_more, definition";
  /**
   * colon between
   */
  public static final String COLON_BETWEEN = "colon between";
  /**
   * comma between
   */
  public static final String COMMMA_BETWEEN = "commma between";
  /**
   * othermath or identifier between
   */
  public static final String OTHER_MATH_BETWEEN = "other math between";
  /**
   * patentheses
   */
  public static final String IDENTIFIER_IN_PARENTHESES = "identifier in parentheses in sentence";
  public static final String DEFINIENS_IN_PARENTHESES = "definiens in parentheses in sentence";
  //Feature 13 of [1]
  public static final String SURFACE_TEXT_AND_POS_TAG_OF_TWO_PRECEDING_AND_FOLLOWING_TOKENS_AROUND_THE_DESC_CANDIDATE = "Surface text and POS tag of two preceding and following tokens around the desc candidate";
  //Feature 15 of [1]
  public static final String SURFACE_TEXT_AND_POS_TAG_OF_THREE_PRECEDING_AND_FOLLOWING_TOKENS_AROUND_THE_PAIRED_MATH_EXPR = "Surface text and POS tag of three preceding and following tokens around the paired math expr";
  //Feature 17 of [1]
  public static final String SURFACE_TEXT_OF_THE_FIRST_VERB_THAT_APPEARS_BETWEEN_THE_DESC_CANDIDATE_AND_THE_TARGET_MATH_EXPR = "Surface text of the first verb that appears between the desc candidate and the target math expr";
  //Feature 18 of [1]
  public static final String GRAPH_DISTANCE = "graphDistance";
  //Feature 19 of [1]
  public static final String SURFACE_TEXT_AND_POS_TAG_OF_DEPENDENCY_WITH_LENGTH_3_FROM_DEFINIEN = "dependency with length 3 from definien";
  //Feature 21 in [1]
  public static final String SURFACE_TEXT_AND_POS_TAG_OF_DEPENDENCY_WITH_LENGTH_3_FROM_IDENTIFIER = "dependency with length 3 from identifier";
  //Feature 20 of [1]
  public static final String INCOMING_TO_DEFINIEN = "direction of " + SURFACE_TEXT_AND_POS_TAG_OF_DEPENDENCY_WITH_LENGTH_3_FROM_DEFINIEN;
  //Feature 22 of [1]
  public static final String INCOMING_TO_IDENTIFIER = "direction of " + SURFACE_TEXT_AND_POS_TAG_OF_DEPENDENCY_WITH_LENGTH_3_FROM_IDENTIFIER;
  public static final String DISTANCE_FROM_FIRST_OCCURRENCE = "distance_from_first_occurence";
  public static final String RELATIVE_TERM_FREQUENCY = "relative_term_frequency";

  //String constants
  public static final String CLASSIFICATION = "classification";
  private static final String DEFINIENS_TEXT = "definiens_candidate";
  private static final String IDENTIFIER_TEXT = "identifier_candidate";
  /**
   * Assumed length of the longest possible word (30) times three to accommodate for noun phrases. For normalisation.
   */
  public static final double LONGEST_NNP_IN_ENGLISH = 100d;
  /**
   * According to https://en.wikipedia.org/wiki/Longest_English_sentence
   */
  public static final double LONGEST_SENTENCE_IN_ENGISH = 300d;

  public static final List<String> nominal = Lists.newArrayList(MATCH, NO_MATCH);
  //TODO: move hack to CLI
  private static final boolean NO_STRING = false;
  private static final boolean NO_DEP = false;
  private static final boolean NO_STAT = false;
  private static final boolean NO_PM = false;
  private static final boolean NO_BASIC = false;

  public Instances createInstances(String title) {
    ArrayList<Attribute> atts = new ArrayList<>();
    //meta information
    atts.add(new Attribute(TITLE, (FastVector) null));
    atts.add(new Attribute(Q_ID, (FastVector) null));
    atts.add(new Attribute(IDENTIFIER, (FastVector) null));
    atts.add(new Attribute(DEFINIEN, (FastVector) null));

    atts.add(new Attribute(PATTERN_1));
    atts.add(new Attribute(PATTERN_2));
    atts.add(new Attribute(PATTERN_3));
    atts.add(new Attribute(PATTERN_4));
    atts.add(new Attribute(PATTERN_5));
    atts.add(new Attribute(PATTERN_6));
    atts.add(new Attribute(PATTERN_7));
    atts.add(new Attribute(PATTERN_8));
    atts.add(new Attribute(PATTERN_9));
    atts.add(new Attribute(PATTERN_10));

    atts.add(new Attribute(COLON_BETWEEN));
    atts.add(new Attribute(COMMMA_BETWEEN));
    atts.add(new Attribute(OTHER_MATH_BETWEEN));

    atts.add(new Attribute(DEFINIENS_IN_PARENTHESES));
    atts.add(new Attribute(IDENTIFIER_IN_PARENTHESES));

    atts.add(new Attribute(WORD_DISTANCE));
    atts.add(new Attribute(WORD_POSITIONING));

    atts.add(new Attribute(SURFACE_TEXT_AND_POS_TAG_OF_TWO_PRECEDING_AND_FOLLOWING_TOKENS_AROUND_THE_DESC_CANDIDATE, (FastVector) null));
    atts.add(new Attribute(SURFACE_TEXT_AND_POS_TAG_OF_THREE_PRECEDING_AND_FOLLOWING_TOKENS_AROUND_THE_PAIRED_MATH_EXPR, (FastVector) null));
    atts.add(new Attribute(SURFACE_TEXT_OF_THE_FIRST_VERB_THAT_APPEARS_BETWEEN_THE_DESC_CANDIDATE_AND_THE_TARGET_MATH_EXPR, (FastVector) null));
    atts.add(new Attribute(GRAPH_DISTANCE));
    atts.add(new Attribute(SURFACE_TEXT_AND_POS_TAG_OF_DEPENDENCY_WITH_LENGTH_3_FROM_IDENTIFIER, (FastVector) null));
    atts.add(new Attribute(INCOMING_TO_IDENTIFIER));
    atts.add(new Attribute(SURFACE_TEXT_AND_POS_TAG_OF_DEPENDENCY_WITH_LENGTH_3_FROM_DEFINIEN, (FastVector) null));
    atts.add(new Attribute(INCOMING_TO_DEFINIEN));
    atts.add(new Attribute(DISTANCE_FROM_FIRST_OCCURRENCE));
    atts.add(new Attribute(RELATIVE_TERM_FREQUENCY));
    atts.add(new Attribute(CLASSIFICATION, nominal));
    Instances result = new Instances(title, atts, 0);
    result.setClassIndex(result.numAttributes() - 1);
    return result;
  }

  /**
   * Extract the features from the relations and add them to the instances.
   *
   * @param parser            for dependency graph features.
   * @param relations         the relations to process
   * @param title             title of the document
   * @param qId               qid of the document
   * @param instances         where to add the relations see {@link #createInstances(String)} for the definition.
   * @param maxSentenceLength length of the longest sentence in the document, for normalisation.
   * @return Instances where all provided relations have been added
   */
  public Instances addRelationsToInstances(DependencyParser parser, List<Relation> relations, String title, String qId, Instances instances, double maxSentenceLength) {
    for (Relation relation : relations) {
      //LOG.debug("Add relation: " + relation);
      addRelationToInstances(parser, getPrecomputedGraphStore(), title, qId, instances, maxSentenceLength, relation);
    }
    return instances;
  }

  public void addRelationToInstances(DependencyParser parser, Map<Sentence, GrammaticalStructure> precomputedGraphs, String title, String qId, Instances instances, double maxSentenceLength, Relation relation) {
    double[] patternMatches = new MachineLearningPatternMatcher().match(relation.getSentence(), relation.getIdentifier(), relation.getDefinition(), relation.getIdentifierPosition(), relation.getWordPosition());
    double[] values = new double[instances.numAttributes()];
    addStringValue(values, instances, TITLE, title);
    addStringValue(values, instances, Q_ID, qId);
    addStringValue(values, instances, IDENTIFIER, relation.getIdentifier());
    addStringValue(values, instances, DEFINIEN, relation.getDefinition());

    LOG.debug("Calculate distances for qID {} between identifier '{}' and definien '{}'.",
            qId, relation.getIdentifier(), relation.getDefinition());

    //distance between identifier and definiens candidate
    int wordDistance = relation.getIdentifierPosition() - relation.getWordPosition();
    values[instances.attribute(WORD_DISTANCE).index()] = (double) Math.abs(wordDistance) / maxSentenceLength;

    //weather or not the definiens is before or after the identifier
    if (!NO_PM) {
      for (int i = 0; i < patternMatches.length - 5; i++) {
        values[instances.attribute(PATTERN_1).index() + i] = patternMatches[i];
      }
    }

    if (!NO_BASIC) {
      values[instances.attribute(WORD_POSITIONING).index()] = wordDistance > 0 ? 1 : 0;
      values[instances.attribute(COLON_BETWEEN).index()] = patternMatches[10];
      values[instances.attribute(COMMMA_BETWEEN).index()] = patternMatches[11];
      values[instances.attribute(OTHER_MATH_BETWEEN).index()] = patternMatches[12];
      values[instances.attribute(DEFINIENS_IN_PARENTHESES).index()] = patternMatches[13];
      values[instances.attribute(IDENTIFIER_IN_PARENTHESES).index()] = patternMatches[14];
    }

    LOG.trace("Start to add String features.");
    addStringFeatures(values, instances, relation);

    LOG.trace("Start to add dependency tree features.");
    addDependencyTreeFeatures(parser, precomputedGraphs, values, instances, relation, maxSentenceLength);

    values[instances.attribute(DISTANCE_FROM_FIRST_OCCURRENCE).index()] = relation.getDistanceFromFirstIdentifierOccurence();

    values[instances.attribute(RELATIVE_TERM_FREQUENCY).index()] = relation.getRelativeTermFrequency();
    if (NO_STAT) {
      values[instances.attribute(DISTANCE_FROM_FIRST_OCCURRENCE).index()] = 0;
      values[instances.attribute(RELATIVE_TERM_FREQUENCY).index()] = 0;
      values[instances.attribute(WORD_DISTANCE).index()] = 0;
    }
    values[values.length - 1] = relation.getRelevance() > 1 ? nominal.indexOf(MATCH) : nominal.indexOf(NO_MATCH);
    DenseInstance instance = new DenseInstance(1.0, values);
    instances.add(instance);
    LOG.trace("Done!");
  }

  /**
   * Adds {@link #SURFACE_TEXT_AND_POS_TAG_OF_TWO_PRECEDING_AND_FOLLOWING_TOKENS_AROUND_THE_DESC_CANDIDATE},
   * {@link #SURFACE_TEXT_AND_POS_TAG_OF_THREE_PRECEDING_AND_FOLLOWING_TOKENS_AROUND_THE_PAIRED_MATH_EXPR} and
   * {@link #SURFACE_TEXT_OF_THE_FIRST_VERB_THAT_APPEARS_BETWEEN_THE_DESC_CANDIDATE_AND_THE_TARGET_MATH_EXPR} to values.
   *
   * @param values    values object.
   * @param instances instances where the values will be added.
   * @param relation  the relation from whitch to extract the features.
   */
  private void addStringFeatures(double[] values, Instances instances, Relation relation) {
    if (NO_STRING) {
      addStringValue(values, instances, SURFACE_TEXT_AND_POS_TAG_OF_TWO_PRECEDING_AND_FOLLOWING_TOKENS_AROUND_THE_DESC_CANDIDATE, "");
      addStringValue(values, instances, SURFACE_TEXT_AND_POS_TAG_OF_THREE_PRECEDING_AND_FOLLOWING_TOKENS_AROUND_THE_PAIRED_MATH_EXPR, "");
      addStringValue(values, instances, SURFACE_TEXT_OF_THE_FIRST_VERB_THAT_APPEARS_BETWEEN_THE_DESC_CANDIDATE_AND_THE_TARGET_MATH_EXPR, "");
    } else {
      int wordDistance = relation.getIdentifierPosition() - relation.getWordPosition();
      //Surface text and POS tag of two preceding and following tokens around the desc candidate
      List<Word> pre = Lists.newArrayList(relation.getSentence().getWords().subList(Math.max(0, relation.getWordPosition() - 2), relation.getWordPosition()));
      List<Word> post = Lists.newArrayList(relation.getSentence().getWords().subList(relation.getWordPosition() + 1, Math.min(relation.getWordPosition() + 3, relation.getSentence().getWords().size())));
      //replace the occurrences of the identifier
      replaceWord(wordDistance, pre, post, IDENTIFIER_TEXT);
      String twoBeforeAndAfter = wordListToSimpleString(pre) + " " + wordListToSimpleString(post);
      addStringValue(values, instances, SURFACE_TEXT_AND_POS_TAG_OF_TWO_PRECEDING_AND_FOLLOWING_TOKENS_AROUND_THE_DESC_CANDIDATE, twoBeforeAndAfter);

      //Surface text and POS tag of three preceding and following tokens around the paired math expr
      pre = Lists.newArrayList(relation.getSentence().getWords().subList(Math.max(0, relation.getIdentifierPosition() - 3), relation.getIdentifierPosition()));
      post = Lists.newArrayList(relation.getSentence().getWords().subList(relation.getIdentifierPosition() + 1, Math.min(relation.getIdentifierPosition() + 4, relation.getSentence().getWords().size())));
      //replace the occurrences of the definiens
      replaceWord(-wordDistance, pre, post, DEFINIENS_TEXT);
      String threePrecedingAndFollowing = wordListToSimpleString(pre) + " " + wordListToSimpleString(post);
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
      } else {
        addStringValue(values, instances, SURFACE_TEXT_OF_THE_FIRST_VERB_THAT_APPEARS_BETWEEN_THE_DESC_CANDIDATE_AND_THE_TARGET_MATH_EXPR, "");
      }
    }
  }

  /**
   * Adds {@link #SURFACE_TEXT_AND_POS_TAG_OF_DEPENDENCY_WITH_LENGTH_3_FROM_DEFINIEN}, {@link #SURFACE_TEXT_AND_POS_TAG_OF_DEPENDENCY_WITH_LENGTH_3_FROM_IDENTIFIER}, {@link #INCOMING_TO_DEFINIEN} and
   * {@link #INCOMING_TO_IDENTIFIER} to values.
   *
   * @param values    values object.
   * @param graphs    map to store graphs that were computed previously. may be null.
   * @param instances instances where the values will be added.
   * @param relation  the relation from which to extract the features.
   */
  private void addDependencyTreeFeatures(DependencyParser parser, Map<Sentence, GrammaticalStructure> graphs, double[] values, Instances instances, Relation relation, double maxSentenceLength) {
    if (NO_DEP) {
      addStringValue(values, instances, SURFACE_TEXT_AND_POS_TAG_OF_DEPENDENCY_WITH_LENGTH_3_FROM_IDENTIFIER, "");
      addStringValue(values, instances, SURFACE_TEXT_AND_POS_TAG_OF_DEPENDENCY_WITH_LENGTH_3_FROM_DEFINIEN, "");
      values[instances.attribute(GRAPH_DISTANCE).index()] = 0;
      values[instances.attribute(INCOMING_TO_IDENTIFIER).index()] = 0;
      values[instances.attribute(INCOMING_TO_DEFINIEN).index()] = 0;
    } else {
      GrammaticalStructure dependencyTree;
      if (graphs != null && graphs.containsKey(relation.getSentence())) {
        dependencyTree = graphs.get(relation.getSentence());
      } else {
        List<TaggedWord> taggedSentence = new ArrayList<>();
        for (Word word : relation.getSentence().getWords()) {
          taggedSentence.add(new TaggedWord(word.getWord(), word.getPosTag()));
        }
        //create dependency graph
        dependencyTree = parser.predict(taggedSentence);
        //store to avoid recomputation
        if (graphs != null)
          graphs.put(relation.getSentence(), dependencyTree);
      }
      SemanticGraph semanticGraph = new SemanticGraph(dependencyTree.typedDependencies());
      IndexedWord identifier = semanticGraph.getNodeByIndex(relation.getIdentifierPosition() + 1);
      IndexedWord definiens = semanticGraph.getNodeByIndex(relation.getWordPosition() + 1);
      //shortest edge path for distance
      List<SemanticGraphEdge> edgesOnPath = semanticGraph.getShortestUndirectedPathEdges(identifier, definiens);
      int distance = edgesOnPath.size();
      values[instances.attribute(GRAPH_DISTANCE).index()] = (double) distance / maxSentenceLength;
      //shortest node path for dependencies
      List<IndexedWord> fromIdentifier = semanticGraph.getShortestUndirectedPathNodes(identifier, definiens);
      removeUnwanted(fromIdentifier);
      List<IndexedWord> fromDefinien = new ArrayList<>(fromIdentifier);
      Collections.reverse(fromDefinien);
      //we don't want the first, since that would be the identifier
      replaceWord(definiens.index(), fromIdentifier, DEFINIENS_TEXT);
      replaceWord(identifier.index(), fromDefinien, IDENTIFIER_TEXT);

      List<Word> threeFromIdentifier = getDependencyWithLengthOfThree(fromIdentifier);

      addStringValue(values, instances, SURFACE_TEXT_AND_POS_TAG_OF_DEPENDENCY_WITH_LENGTH_3_FROM_IDENTIFIER, wordListToSimpleString(threeFromIdentifier));

      List<Word> threeFromDefinien = getDependencyWithLengthOfThree(fromDefinien);

      addStringValue(values, instances, SURFACE_TEXT_AND_POS_TAG_OF_DEPENDENCY_WITH_LENGTH_3_FROM_DEFINIEN, wordListToSimpleString(threeFromDefinien));

      values[instances.attribute(INCOMING_TO_IDENTIFIER).index()] = edgesOnPath.get(0).getDependent().equals(identifier) ? 1 : 0;

      values[instances.attribute(INCOMING_TO_DEFINIEN).index()] = edgesOnPath.get(edgesOnPath.size() - 1).getDependent().equals(definiens) ? 1 : 0;
    }
    if (NO_STRING) {
      addStringValue(values, instances, SURFACE_TEXT_AND_POS_TAG_OF_DEPENDENCY_WITH_LENGTH_3_FROM_IDENTIFIER, "");
      addStringValue(values, instances, SURFACE_TEXT_AND_POS_TAG_OF_DEPENDENCY_WITH_LENGTH_3_FROM_DEFINIEN, "");
    }
  }

  /**
   * Get a list of three words, beginning from the second word. Converts {@link IndexedWord} to {@link Word}
   *
   * @param fromIdentifierOrDefiniens List of indexed words starting with the source (identifier or definiens).
   * @return list with at most three words.
   */
  public List<Word> getDependencyWithLengthOfThree(List<IndexedWord> fromIdentifierOrDefiniens) {
    return fromIdentifierOrDefiniens.subList(
      Math.min(1, fromIdentifierOrDefiniens.size()),
      Math.min(4, fromIdentifierOrDefiniens.size())
    ).stream().map(iw -> new Word(iw.word(), iw.tag())).collect(Collectors.toList());
  }

  /**
   * Replace the surface test of the word with the specific index in the sentence.
   * I.e. to hide the surface text of the definiens and identifier from the machine learner.
   *
   * @param index index of the word in the sentence.
   * @param words list of words.
   * @param text  the new surface text.
   */
  public void replaceWord(int index, List<IndexedWord> words, String text) {
    for (IndexedWord iw : words) {
      if (iw.index() == index) {
        iw.setWord(text);
        break;
      }
    }

  }

  /**
   * Replace teh surface test of the word with the specific index in the sentence. Pre is in reverse order and post is in order.
   * I.e. to hide the surface text of the definiens and identifier from the machine learner.
   *
   * @param index index of the word in the sentence.
   * @param pre   list of words before the identifier or definiens.
   * @param post  list of words after the identifier or definiens.
   * @param text  the new surface text.
   */
  public void replaceWord(int index, List<Word> pre, List<Word> post, String text) {
    Word replacement;
    if (index > 0 && index <= post.size()) {
      replacement = new Word(text, post.get(index - 1).getPosTag());
      post.remove(index - 1);
      post.add(index - 1, replacement);
    }
    if (index < 0 && Math.abs(index) <= pre.size()) {
      int size = pre.size();
      replacement = new Word(text, pre.get(size + index).getPosTag());
      pre.remove(size + index);
      pre.add(size + index, replacement);
    }
  }

  /**
   * Convenience method to add a string value.
   *
   * @param data
   * @param instances
   * @param field
   * @param string
   */
  public void addStringValue(double[] data, Instances instances, String field, String string) {
    data[instances.attribute(field).index()] = instances.attribute(field).addStringValue(string);
  }

  /**
   * Gets a string from a list of words.
   *
   * @param words the words to convert.
   * @return String containing surface text and pos tag of the words.
   */
  public String wordListToSimpleString(List<Word> words) {
    StringBuilder stringBuilder = new StringBuilder();
    for (Word w : words) {
      //do not include things that the tokenizer eats anyway
      if (!tokenisationDelimiters.contains(w.getWord()) && !tokenisationDelimitersPOSTags.contains(w.getPosTag())) {
        stringBuilder.append(w.getWord().replaceAll(" ", "_").replaceAll(tokenisationDelimitersRegex, ""));
        stringBuilder.append("_");
        stringBuilder.append(w.getPosTag());
        stringBuilder.append(" ");
      }
    }
    return stringBuilder.toString();
  }

  private void removeUnwanted(List<IndexedWord> words) {
    Iterator i = words.iterator();
    while (i.hasNext()) {
      IndexedWord w = (IndexedWord) i.next();
      if (tokenisationDelimitersPOSTags.contains(w.tag())) {
        i.remove();
      } else if (tokenisationDelimiters.contains(w.word())) {
        i.remove();
      }
    }
  }

  /**
   * {@link weka.core.tokenizers.NGramTokenizer#m_Delimiters}
   */
  private static String tokenisationDelimiters = " \r\n\t\\.,;:'\"\\(\\)\\?\\!";
  private static String tokenisationDelimitersRegex = "[ \r\n\t.,;:'\"()?!]";
  private static List<String> tokenisationDelimitersPOSTags =
    Arrays.asList(new String[]{"-LRB-", "-RRB-", "$", "#", ".", ",", ":", "\"", "(", ")", "``", "'", "`", "\'\'"});

  public static double average(double[] doubles) {
    return Arrays.stream(doubles).sum() / doubles.length;
  }

  public Map<Sentence, GrammaticalStructure> getPrecomputedGraphStore() {
    return new HashMap<Sentence, GrammaticalStructure>();
  }
}
