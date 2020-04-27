package com.formulasearchengine.mathosphere.mlp.contracts;

import com.formulasearchengine.mathosphere.mlp.cli.BaseConfig;
import com.formulasearchengine.mathosphere.mlp.pojos.*;
import com.formulasearchengine.mathosphere.mlp.text.PosTag;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Mapper that finds a list of possible identifiers and their definitions. As described in section 2 step 4 of
 * https://www.gipp.com/wp-content/papercite-data/pdf/schubotz16.pdf
 */
public class CreateCandidatesMapper implements MapFunction<ParsedWikiDocument, WikiDocumentOutput> {
  private static final Logger LOG = LogManager.getLogger(CreateCandidatesMapper.class.getName());

  private final BaseConfig config;
  private double alpha;
  private double beta;
  private double gamma;

  public CreateCandidatesMapper(BaseConfig config) {
    this.config = config;
    //copy alpha, beta and gamma for convince
    this.alpha = config.getAlpha();
    this.beta = config.getBeta();
    this.gamma = config.getGamma();
  }

  @Override
  public WikiDocumentOutput map(ParsedWikiDocument doc) {
    if ( config.useMOI() ) {
      return moiMapping(doc);
    } else return identifierMapping(doc);
  }

  private WikiDocumentOutput moiMapping( ParsedWikiDocument doc ) {
    LOG.info("Start MOI-definiens mapping.");
    List<Relation> relations = Lists.newArrayList();

    Collection<MathTag> formulae = doc.getFormulae();
    if ( formulae == null || formulae.isEmpty() )
      return new WikiDocumentOutput(doc.getTitle(), relations, doc.getFormulaeMap());

    Collection<MathTag> math = doc.getFormulae();
    for ( MathTag m : math ) {
      List<Relation> candidates = generateCandidates(doc, m);
      if(config.getDefinitionMerging()){
        selfMerge(candidates);
      } else {
        Collections.sort(candidates);
        Collections.reverse(candidates);
      }
      for (Relation rel : candidates) {
        if (rel.getScore() >= config.getThreshold()) {
          relations.add(rel);
        }
      }
    }

    return new WikiDocumentOutput(doc.getTitle(), relations, doc.getFormulaeMap());
  }

  private WikiDocumentOutput identifierMapping(ParsedWikiDocument doc) {
    LOG.info("Start identifier-definiens mapping.");
    List<Relation> relations = Lists.newArrayList();
    Multiset<String> idents = doc.getIdentifiers();
    if ( idents == null ) {
      LOG.warn("No identifiers available.");
      return new WikiDocumentOutput(doc.getTitle(), relations, HashMultiset.create());
    }
    Set<String> identifiers = doc.getIdentifiers().elementSet();
    for (String identifier : identifiers) {
      List<Relation> candidates = generateCandidates(doc, identifier);
      if(config.getDefinitionMerging()){
        selfMerge(candidates);
      } else {
        Collections.sort(candidates);
        Collections.reverse(candidates);
      }
      for (Relation rel : candidates) {
        if (rel.getScore() >= config.getThreshold()) {
          relations.add(rel);
        }
      }
    }
    return new WikiDocumentOutput(doc.getTitle(), relations, doc.getIdentifiers());
  }

  private void selfMerge(List<Relation> candidates) {
    Collections.sort(candidates,Relation::compareNameScore);
    final Iterator<Relation> iterator = candidates.iterator();
    Relation lastLower = null;
    Relation lastElement = null;
    double decayFactor;
    int multiplicity = 1;
    while (iterator.hasNext()) {
      final Relation relation = iterator.next();
      Relation lower = new Relation(relation.getIdentifier(), relation.getDefinition().toLowerCase());
      if (lastLower != null && lower.compareToName(lastLower) == 0) {
        multiplicity++;
        decayFactor = Math.pow(2, -1.3*multiplicity);
        lastElement.setScore(lastElement.getScore() + relation.getScore() * decayFactor);
        iterator.remove();
      } else {
        multiplicity = 1;
        relation.setScore(.722 * relation.getScore());
        lastElement = relation;
        lastLower = lower;
      }
    }
    candidates.sort(Relation::compareTo);
  }

  private List<Relation> generateCandidates(ParsedWikiDocument doc, MathTag formula) {
    List<Relation> result = Lists.newArrayList();
    List<Tuple2<Sentence, Set<MathTag>>> sentences = findSentencesWithFormula(doc.getSentences(), formula);
    if ( sentences.isEmpty() ) return result;

    Multiset<String> wordFrequencies = HashMultiset.create();
    sentences.stream()
            .flatMap(f -> f.f0.getWords().stream())
            .filter(this::isGood)
            .map(Word::toLowerCase)
            .forEach(wordFrequencies::add);
    if ( wordFrequencies.isEmpty() ) return result;

    int maxNounFrequency = calculateMax(wordFrequencies);

    Position firstAppearancePosition = sentences.get(0).f0.getWords().get(0).getPosition();

    for (int sentenceIdx = 0; sentenceIdx < sentences.size(); sentenceIdx++) {
      Tuple2<Sentence, Set<MathTag>> entry = sentences.get(sentenceIdx);
      Sentence sentence = entry.f0;
      List<Word> definiens = sentence.getNouns();

//      List<Integer> positions = identifierPositions(words, identifier);

      for ( Word def : definiens ) {

        int distance = calculateClosestDistance(def, entry.f1);
        int freq = wordFrequencies.count(def.getWord().toLowerCase());
        double score = calculateScore(
                distance,
                freq,
                maxNounFrequency,
                firstAppearancePosition.getSentenceDistance(def.getPosition())
        );

        Relation relation = new Relation();
        relation.setMathTag(formula);
        relation.setIdentifierPosition(formula.getPositions().get(0).getWord());
        relation.setDefinition(def, doc);
        relation.setWordPosition(def.getPosition().getWord());
        relation.setScore(score);
        relation.setSentence(sentence);

        result.add(relation);
      }
    }

    return result;
  }

  /**
   * Find a list of possible definitions for an identifier. As described in section 2 step 4 of
   * https://www.gipp.com/wp-content/papercite-data/pdf/schubotz16.pdf
   *
   * @param doc        Where to search for definitions
   * @param identifier What to define.
   * @return {@link List<Relation>} with ranked definitions for the identifier.
   */
  private List<Relation> generateCandidates(ParsedWikiDocument doc, String identifier) {
    List<Sentence> sentences = findSentencesWithIdentifier(doc.getSentences(), identifier);
    if (sentences.isEmpty()) {
      return Collections.emptyList();
    }

    List<Relation> result = Lists.newArrayList();
    Multiset<String> frequencies = calcFrequencies(sentences);
    if (frequencies.isEmpty()) {
      return Collections.emptyList();
    }

    int maxFrequency = calculateMax(frequencies);

    for (int sentenceIdx = 0; sentenceIdx < sentences.size(); sentenceIdx++) {
      Sentence sentence = sentences.get(sentenceIdx);
      List<Word> words = sentence.getWords();
      List<Integer> positions = identifierPositions(words, identifier);

      for (int wordIdx = 0; wordIdx < words.size(); wordIdx++) {
        //Definiendum
        Word word = words.get(wordIdx);
        if (!isGood(word)) {
          continue;
        }

        int identifierPosition = closestIdentifierPosition(positions, wordIdx);
        int distance = Math.abs(identifierPosition - wordIdx);

        int freq = frequencies.count(word.toLowerCase());
        double score = calculateScore(distance, freq, maxFrequency, sentenceIdx);

        Relation relation = new Relation();
        relation.setIdentifier(identifier);
        relation.setIdentifierPosition(identifierPosition);
        relation.setDefinition(word);
        relation.setWordPosition(wordIdx);
        relation.setScore(score);
        relation.setSentence(sentence);

        result.add(relation);
      }
    }

    return result;
  }

  /**
   * Find a list of possible definitions for an identifier. As described in section 2 step 5 of
   * https://www.gipp.com/wp-content/papercite-data/pdf/schubotz16.pdf
   *
   * @param distance     Number of tokens between identifier and definiens.
   * @param frequency    The term frequency of the possible definiendum.
   * @param maxFrequency The max term frequency within this document. For normalisation of the term frequency.
   * @param sentenceIdx  The number of sentences between the definiens candidate and the sentence in which the identifier occurs for the first time
   * @return Score how likely the definiendum is the correct definition for the identifier.
   */
  private double calculateScore(int distance, int frequency, int maxFrequency, int sentenceIdx) {
    double std1 = Math.sqrt(Math.pow(5d, 2d) / (2d * Math.log(2)));
    double dist = gaussian(distance, std1);

    double std2 = Math.sqrt(Math.pow(3d, 2d) / (2d * Math.log(2)));
    double seq = gaussian(sentenceIdx, std2);

    double relativeFrequency = (double) frequency / (double) maxFrequency;
    return (alpha * dist + beta * seq + gamma * relativeFrequency) / (alpha + beta + gamma);
  }

  private static double gaussian(double x, double std) {
    return Math.exp(-x * x / (2 * std * std));
  }

  /**
   * Find all occurrences of the identifier in the sentence.
   */
  public static List<Integer> identifierPositions(List<Word> sentence, String identifier) {
    List<Integer> result = Lists.newArrayList();

    for (int wordIdx = 0; wordIdx < sentence.size(); wordIdx++) {
      Word word = sentence.get(wordIdx);
      if (Objects.equals(identifier, word.getWord())) {
        result.add(wordIdx);
      }
    }

    return result;
  }

  public static int closestIdentifierPosition(List<Integer> positions, int wordIdx) {
    if (positions.isEmpty()) {
      return -1;
    }
    Iterator<Integer> it = positions.iterator();
    int bestPos = it.next();
    int bestDist = Math.abs(wordIdx - bestPos);

    while (it.hasNext()) {
      int pos = it.next();
      int dist = Math.abs(wordIdx - pos);
      if (dist < bestDist) {
        bestDist = dist;
        bestPos = pos;
      }
    }

    return bestPos;
  }

  public static int calculateMax(Multiset<String> frequencies) {
    Entry<String> max = Collections.max(frequencies.entrySet(),
            Comparator.comparingInt(Entry::getCount));
    return max.getCount();
  }

  private Multiset<String> calcFrequencies(List<Sentence> sentences) {
    Multiset<String> counts = HashMultiset.create();

    for (Sentence sentence : sentences) {
      for (Word word : sentence.getWords()) {
        if (isGood(word)) {
          counts.add(word.getWord().toLowerCase());
        }
      }
    }

    return counts;
  }

  private boolean isGood(Word in) {
    String word = in.getWord();
    String posTag = in.getPosTag();

   /* if (!DefinitionUtils.isValid(word)) {
      return false;
    }

    if ("ID".equals(posTag)) {
      return false;
    }*/
//    if (word.length() < 3) {
//      return false;
//    }
//    if (word.contains("<")) {
//      // remove tags and so
//      //TODO: Make a white-list of allowed chars.
//      return false;
//    }
    // we're only interested in nouns, entities and links
    return posTag.matches(PosTag.DEFINIEN_REGEX);
  }

  public static int calculateClosestDistance(Word def, Set<MathTag> formulae) {
    Position wordP = def.getPosition();
    return formulae.stream()
            .flatMap(f -> f.getPositions().stream())
            .filter( f -> wordP.getSection() == f.getSection() && wordP.getSentence() == f.getSentence())
            .map( f -> Math.abs(wordP.compareTo(f)) )
            .min( Integer::compareTo )
            .orElse(Integer.MIN_VALUE);
  }

  /**
   * First approach, identify sentences that share all identifiers that are given in the formula.
   * @param sentences
   * @param formula
   * @return
   */
  public static List<Tuple2<Sentence, Set<MathTag>>> findSentencesWithFormula(List<Sentence> sentences, MathTag formula) {
    // first approach
    List<Tuple2<Sentence, Set<MathTag>>> out = new LinkedList<>();

    for ( Sentence s : sentences ) {
      Set<MathTag> allMath = s.getFormulaWithAllIdentifiers(formula);
      if ( !allMath.isEmpty() ) {
        Tuple2<Sentence, Set<MathTag>> entry = new Tuple2<>(s, allMath);
        out.add(entry);
      }
    }

    return out;

//    Set<String> idents = formula.getIdentifiers().elementSet();
//    return findSentencesWithIdentifier(sentences, idents.toArray(new String[0]));
  }

  /**
   * Find all sentences with the given identifier.
   *
   * @return {@link ArrayList} with the sentences containing the identifier.
   */
  public static List<Sentence> findSentencesWithIdentifier(List<Sentence> sentences, String... identifier) {
    List<Sentence> result = Lists.newArrayList();

    for (Sentence sentence : sentences) {
      if (sentence.containsIdentifier(identifier)) {
        result.add(sentence);
      }
    }

    return result;
  }
}
