package com.formulasearchengine.mathosphere.mlp.text;

import com.formulasearchengine.mathosphere.mlp.cli.FlinkMlpCommandConfig;
import com.formulasearchengine.mathosphere.mlp.pojos.*;
import com.formulasearchengine.mlp.evaluation.pojo.GoldEntry;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.formulasearchengine.mathosphere.utils.GoldUtil.getGoldEntryByTitle;
import static com.formulasearchengine.mathosphere.utils.GoldUtil.matchesGold;

/**
 * Extracts simple features like pattern matching and word counts.
 * Use this class to generate the features of the document.
 */
public class SimpleFeatureExtractorMapper implements MapFunction<ParsedWikiDocument, WikiDocumentOutput> {

  private static final Logger LOGGER = LogManager.getLogger(SimpleFeatureExtractorMapper.class.getName());

  private final FlinkMlpCommandConfig config;
  private final List<GoldEntry> goldEntries;
  private final boolean extractionForTraining;

  public SimpleFeatureExtractorMapper(FlinkMlpCommandConfig config, List<GoldEntry> goldEntries) {
    this.config = config;
    this.goldEntries = goldEntries;
    extractionForTraining = goldEntries != null && !goldEntries.isEmpty();
  }

  @Override
  public WikiDocumentOutput map(ParsedWikiDocument doc) throws Exception {
    List<Relation> allIdentifierDefininesCandidates = new ArrayList<>();
    List<Sentence> sentences = doc.getSentences();

    Map<String, Integer> identifierSentenceDistanceMap = findSentencesWithIdentifierFirstOccurrences(sentences, doc.getIdentifiers());

    Multiset<String> frequencies = aggregateWords(sentences);

    int maxFrequency = getMaxFrequency(frequencies, doc.getTitle());
    GoldEntry goldEntry = getGoldEntryByTitle(goldEntries, doc.getTitle());
    if (extractionForTraining && goldEntry != null) {
      return getIdentifiersWithGoldInfo(doc, allIdentifierDefininesCandidates, sentences, identifierSentenceDistanceMap, frequencies, maxFrequency);
    } else {
      return getAllIdentifiers(doc, allIdentifierDefininesCandidates, sentences, identifierSentenceDistanceMap, frequencies, maxFrequency);
    }
  }

  private WikiDocumentOutput getIdentifiersWithGoldInfo(ParsedWikiDocument doc, List<Relation> allIdentifierDefininesCandidates, List<Sentence> sentences, Map<String, Integer> identifierSentenceDistanceMap, Multiset<String> frequencies, double maxFrequency) {
    GoldEntry goldEntry = getGoldEntryByTitle(goldEntries, doc.getTitle());
    final Integer fid = Integer.parseInt(goldEntry.getFid());
    Stream<MathTag> stream = doc.getFormulas().stream();
    stream = stream.filter( e -> e.getMarkUpType().equals(WikiTextUtils.MathMarkUpType.LATEX) );
    List<MathTag> list = stream.collect( Collectors.toList() );
    MathTag seed = null;
    if ( list == null || list.size() <= fid ){
      LOGGER.warn("FID is bigger than the list of documents... this will produce an IndexedOutOfBoundsException!");
    } else {
      seed = list.get(fid);
    }

//    MathTag seed = doc.getFormulas()
//      .stream().filter(e -> e.getMarkUpType().equals(WikiTextUtils.MathMarkUpType.LATEX)).collect(Collectors.toList())
//      .get(fid);
    for (int i = 0; i < sentences.size(); i++) {
      Sentence sentence = sentences.get(i);
      if (!sentence.getIdentifiers().isEmpty()) {
        LOGGER.debug("sentence {}", sentence);
      }
      Set<String> identifiers = sentence.getIdentifiers();
      //only identifiers that were extracted by the MPL pipeline
      identifiers.retainAll(seed.getIdentifiers(config).elementSet());
      SimplePatternMatcher matcher = SimplePatternMatcher.generatePatterns(identifiers);
      Collection<Relation> foundMatches = matcher.match(sentence, doc);
      for (Relation match : foundMatches) {
        List<String> identifiersInGold = goldEntry.getDefinitions().stream().map(id -> id.getIdentifier()).collect(Collectors.toList());
        //take only the identifiers that were extracted correctly to avoid false negatives in the training set.
        if (identifiersInGold.contains(match.getIdentifier())) {
          LOGGER.debug("found match {}", match);
          int freq = frequencies.count(match.getSentence().getWords().get(match.getWordPosition()).toLowerCase());
          match.setRelativeTermFrequency((double) freq / maxFrequency);
          if (i - identifierSentenceDistanceMap.get(match.getIdentifier()) < 0) {
            throw new RuntimeException("Cannot find identifier before first occurence");
          }
          match.setDistanceFromFirstIdentifierOccurence((double) (i - identifierSentenceDistanceMap.get(match.getIdentifier())) / (double) doc.getSentences().size());
          match.setRelevance(matchesGold(match.getIdentifier(), match.getDefinition(), goldEntry) ? 2 : 0);
          allIdentifierDefininesCandidates.add(match);
        }
      }
    }
    LOGGER.info("extracted {} relations from {}", allIdentifierDefininesCandidates.size(), doc.getTitle());
    WikiDocumentOutput result = new WikiDocumentOutput(doc.getTitle(), goldEntry.getqID(), allIdentifierDefininesCandidates, null);
    result.setMaxSentenceLength(doc.getSentences().stream().map(s -> s.getWords().size()).max(Comparator.naturalOrder()).orElse(0));
    return result;
  }

  private WikiDocumentOutput getAllIdentifiers(ParsedWikiDocument doc, List<Relation> allIdentifierDefininesCandidates, List<Sentence> sentences, Map<String, Integer> identifierSentenceDistanceMap, Multiset<String> frequencies, double maxFrequency) {
    for (int i = 0; i < sentences.size(); i++) {
      Sentence sentence = sentences.get(i);
      if (!sentence.getIdentifiers().isEmpty()) {
        LOGGER.debug("sentence {}", sentence);
      }
      Set<String> identifiers = sentence.getIdentifiers();
      //only identifiers that were extracted by the MPL pipeline
      SimplePatternMatcher matcher = SimplePatternMatcher.generatePatterns(identifiers);
      Collection<Relation> foundMatches = matcher.match(sentence, doc);
      for (Relation match : foundMatches) {
        //take only the identifiers that were extracted correctly to avoid false negatives in the training set.
        LOGGER.debug("found match {}", match);
        int freq = frequencies.count(match.getSentence().getWords().get(match.getWordPosition()).toLowerCase());
        match.setRelativeTermFrequency((double) freq / maxFrequency);
        if (i - identifierSentenceDistanceMap.get(match.getIdentifier()) < 0) {
          throw new RuntimeException("Cannot find identifier before first occurence");
        }
        match.setDistanceFromFirstIdentifierOccurence((double) (i - identifierSentenceDistanceMap.get(match.getIdentifier())) / (double) doc.getSentences().size());
        allIdentifierDefininesCandidates.add(match);
      }
    }
    WikiDocumentOutput result = new WikiDocumentOutput(doc.getTitle(), "-1", allIdentifierDefininesCandidates, doc.getIdentifiers());
    Optional<Integer> lengthOfLongestSentence = doc.getSentences().stream().map(s -> s.getWords().size()).max(Comparator.naturalOrder());
    //one as save value, since 0 would lead to NAN in division.
    result.setMaxSentenceLength(lengthOfLongestSentence.isPresent() ? lengthOfLongestSentence.get() : 1);
    return result;
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

  private int getMaxFrequency(Multiset<String> frequencies, String title) {
    try {
      Multiset.Entry<String> max = Collections.max(frequencies.entrySet(),
        (e1, e2) -> Integer.compare(e1.getCount(), e2.getCount()));
      return max.getCount();
    } catch (NoSuchElementException e) {
      //no max present
      LOGGER.error("Error in " + title + "Message: " + e.getMessage(), e);
      //1 as save value if anything goes wrong
      return 1;
    }
  }
}
