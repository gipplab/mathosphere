package com.formulasearchengine.mathosphere.mlp.contracts;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;

import com.formulasearchengine.mathosphere.mlp.cli.BaseConfig;
import com.formulasearchengine.mathosphere.mlp.pojos.ParsedWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.Relation;
import com.formulasearchengine.mathosphere.mlp.pojos.Sentence;
import com.formulasearchengine.mathosphere.mlp.pojos.WikiDocumentOutput;
import com.formulasearchengine.mathosphere.mlp.pojos.Word;

import org.apache.flink.api.common.functions.MapFunction;

import java.util.*;

public class CreateCandidatesMapper implements MapFunction<ParsedWikiDocument, WikiDocumentOutput> {

  private final BaseConfig config;
  private double alpha;
  private double beta;
  private double gamma;
  private static final int MAX_CANDIDATES = 20;

  public CreateCandidatesMapper(BaseConfig config) {
    this.config = config;
    //copy alpha, beta and gamma for convince
    this.alpha = config.getAlpha();
    this.beta = config.getBeta();
    this.gamma = config.getGamma();
  }

  @Override
  public WikiDocumentOutput map(ParsedWikiDocument doc) throws Exception {
    Set<String> identifiers = doc.getIdentifiers().elementSet();
    List<Relation> relations = Lists.newArrayList();
    for (String identifier : identifiers) {
      List<Relation> candidates = generateCandidates(doc, identifier);
      if(config.getDefinitionMerging()){
        selfMerge(candidates);
      } else {
        Collections.sort(candidates);
        Collections.reverse(candidates);
      }
      int count = 0;
      for (Relation rel : candidates) {
        if (rel.getScore() >= config.getThreshold()) {
          count++;
          relations.add(rel);
        }
        if (count >= MAX_CANDIDATES) {
          break;
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

  /**
   * Find a list of possible definitions for an identifier. As described in section 2 step 4 of
   * https://www.google.co.jp/url?sa=t&rct=j&q=&esrc=s&source=web&cd=4&cad=rja&uact=8&ved=0ahUKEwjbo8bF5J3PAhWMcT4KHesdCRMQFgg0MAM&url=https%3A%2F%2Fwww.gipp.com%2Fwp-content%2Fpapercite-data%2Fpdf%2Fschubotz16.pdf&usg=AFQjCNG8WcokDbLBSdzddbijH-bJh4w5sA&sig2=ofIftBvBlsOdwikq2d1fag
   *
   * @param doc Where to search for definitions
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
        relation.setDefinition(word, doc);
        relation.setWordPosition(wordIdx);
        relation.setScore(score);
        // relation.setSentence(sentence);

        result.add(relation);
      }
    }

    return result;
  }

  /**
   * Find a list of possible definitions for an identifier. As described in section 2 step 5 of
   * https://www.google.co.jp/url?sa=t&rct=j&q=&esrc=s&source=web&cd=4&cad=rja&uact=8&ved=0ahUKEwjbo8bF5J3PAhWMcT4KHesdCRMQFgg0MAM&url=https%3A%2F%2Fwww.gipp.com%2Fwp-content%2Fpapercite-data%2Fpdf%2Fschubotz16.pdf&usg=AFQjCNG8WcokDbLBSdzddbijH-bJh4w5sA&sig2=ofIftBvBlsOdwikq2d1fag
   *
   * @param distance Number of tokens between identifier and definiens.
   * @param frequency The term frequency of the possible definiendum.
   * @param maxFrequency The max term frequency within this document. For normalisation of the term frequency.
   * @param sentenceIdx The number of sentences between the definiens candidate and the sentence in which the identifier occurs for the first time
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
        (e1, e2) -> Integer.compare(e1.getCount(), e2.getCount()));
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
    if (word.length() < 3) {
      return false;
    }
    if (word.contains("<")) {
      // remove tags and so
      //TODO: Make a white-list of allowed chars.
      return false;
    }
    // we're only interested in nouns, entities and links
    return posTag.matches("NN[PS]{0,2}|NP\\+?|NN\\+|LNK");

  }

  /**
   * Find all sentences with the given identifier.
   * @return {@link ArrayList} with the sentences containing the identifier.
   */
  public static List<Sentence> findSentencesWithIdentifier(List<Sentence> sentences, String identifier) {
    List<Sentence> result = Lists.newArrayList();

    for (Sentence sentence : sentences) {
      if (sentence.contains(identifier)) {
        result.add(sentence);
      }
    }

    return result;
  }
}
