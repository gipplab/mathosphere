package com.formulasearchengine.mathosphere.mlp.text;

import com.formulasearchengine.mathosphere.mlp.cli.EvalCommandConfig;
import com.formulasearchengine.mathosphere.mlp.pojos.*;
import com.formulasearchengine.mlp.evaluation.pojo.GoldEntry;
import com.formulasearchengine.mlp.evaluation.pojo.IdentifierDefinition;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import org.apache.flink.api.common.functions.MapFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class SimpleFeatureExtractor implements MapFunction<ParsedWikiDocument, WikiDocumentOutput> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleFeatureExtractor.class);
  public static final String MATCH = "match";
  public static final String NO_MATCH = "no match";

  private final EvalCommandConfig config;
  private final List<GoldEntry> goldEntries;

  public SimpleFeatureExtractor(EvalCommandConfig config, List<GoldEntry> goldEntries) {
    this.config = config;
    this.goldEntries = goldEntries;
  }

  @Override
  public WikiDocumentOutput map(ParsedWikiDocument doc) throws Exception {
    List<Relation> foundFeatures = Lists.newArrayList();
    List<Sentence> sentences = doc.getSentences();

    Map<String, Integer> identifierSentenceDistanceMap = findSentencesWithIdentifierFirstOccurrences(sentences, doc.getIdentifiers());

    Multiset<String> frequencies = aggregateWords(sentences);

    int maxFrequency = calculateMax(frequencies);

    GoldEntry goldEntry = goldEntries.stream().filter(e -> e.getTitle().equals(doc.getTitle().replaceAll(" ", "_"))).findFirst().get();
    final Integer fid = Integer.parseInt(goldEntry.getFid());
    final MathTag seed = doc.getFormulas()
      .stream().filter(e -> e.getMarkUpType().equals(WikiTextUtils.MathMarkUpType.LATEX)).collect(Collectors.toList())
      .get(fid);
    for (int i = 0; i < sentences.size(); i++) {
      Sentence sentence = sentences.get(i);
      if (!sentence.getIdentifiers().isEmpty()) {
        LOGGER.debug("sentence {}", sentence);
      }
      Set<String> identifiers = sentence.getIdentifiers();
      identifiers.retainAll(seed.getIdentifiers(config).elementSet());
      SimplePatternMatcher matcher = SimplePatternMatcher.generatePatterns(identifiers);
      Collection<Relation> foundMatches = matcher.match(sentence, doc);
      for (Relation match : foundMatches) {
        LOGGER.debug("found match {}", match);
        int freq = frequencies.count(match.getSentence().getWords().get(match.getWordPosition()).toLowerCase());
        match.setRelativeTermFrequency((double) freq / (double) maxFrequency);
        if (i - identifierSentenceDistanceMap.get(match.getIdentifier()) < 0) {
          throw new RuntimeException("Cannot find identifier before first occurence");
        }
        match.setDistanceFromFirstIdentifierOccurence((double) (i - identifierSentenceDistanceMap.get(match.getIdentifier())) / (double) doc.getSentences().size());
        match.setRelevance(matchesGold(match, goldEntry) ? 2 : 0);
        foundFeatures.add(match);
      }
    }
    LOGGER.info("extracted {} relations from {}", foundFeatures.size(), doc.getTitle());
    WikiDocumentOutput result = new WikiDocumentOutput(doc.getTitle(), goldEntry.getqID(), foundFeatures, null);
    result.setMaxSentenceLength(doc.getSentences().stream().map(s -> s.getWords().size()).max(Comparator.naturalOrder()).get());
    return result;
  }


  public boolean matchesGold(Relation relation, GoldEntry gold) {
    return matchesGold(relation.getIdentifier(), relation.getDefinition(), gold);
  }

  public boolean matchesGold(String identifier, String definiens, GoldEntry gold) {
    List<IdentifierDefinition> identifierDefinitions = gold.getDefinitions();
    return identifierDefinitions.contains(new IdentifierDefinition(identifier, definiens.replaceAll("\\[|\\]", "").trim().toLowerCase()));
  }

  private Map<String, Integer> findSentencesWithIdentifierFirstOccurrences(List<Sentence> sentences, Collection<String> identifiers) {
    Map<String, Integer> result = new HashMap<>();
    for (String identifier : identifiers) {
      for (int i = 0; i < sentences.size(); i++) {
        Sentence sentence = sentences.get(i);
        if (sentence.contains(identifier)) {
          result.put(identifier, i);
          break;
        }
      }
    }
    return result;
  }

  /**
   * Aggregates the words to make counting easy.
   *
   * @param sentences from witch to aggregate the words.
   * @return Multiset with an entry for every word.
   */
  private Multiset<String> aggregateWords(List<Sentence> sentences) {
    Multiset<String> counts = HashMultiset.create();
    for (Sentence sentence : sentences) {
      for (Word word : sentence.getWords()) {
        if (word.getWord().length() >= 3) {
          counts.add(word.getWord().toLowerCase());
        }
      }
    }
    return counts;
  }

  private int calculateMax(Multiset<String> frequencies) {
    Multiset.Entry<String> max = Collections.max(frequencies.entrySet(),
      (e1, e2) -> Integer.compare(e1.getCount(), e2.getCount()));
    return max.getCount();
  }
}
