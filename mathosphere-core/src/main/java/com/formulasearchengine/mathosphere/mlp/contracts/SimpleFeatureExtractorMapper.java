package com.formulasearchengine.mathosphere.mlp.contracts;

import com.formulasearchengine.mathosphere.mlp.cli.FlinkMlpCommandConfig;
import com.formulasearchengine.mathosphere.mlp.pojos.*;
import com.formulasearchengine.mathosphere.mlp.text.SimplePatternMatcher;
import com.formulasearchengine.mathosphere.mlp.text.UnicodeMap;
import com.formulasearchengine.mathosphere.mlp.text.WikiTextUtils;
import com.formulasearchengine.mathosphere.utils.GoldUtil;
import com.formulasearchengine.mlp.evaluation.pojo.GoldEntry;
import com.formulasearchengine.mlp.evaluation.pojo.IdentifierDefinition;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.util.Collector;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.formulasearchengine.mathosphere.utils.GoldUtil.matchesGold;

/**
 * Extracts simple features like pattern matching and word counts.
 * Use this class to generate the features of the document.
 */
public class SimpleFeatureExtractorMapper implements FlatMapFunction<ParsedWikiDocument, WikiDocumentOutput> {

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
    public void flatMap(ParsedWikiDocument parsedWikiDocument, Collector<WikiDocumentOutput> collector) throws Exception {
        List<Relation> allIdentifierDefininesCandidates = new ArrayList<>();
        List<Sentence> sentences = parsedWikiDocument.getSentences();

        Map<String, Integer> identifierSentenceDistanceMap =
                findSentencesWithIdentifierFirstOccurrences(sentences, parsedWikiDocument.getIdentifiers());

        Multiset<String> frequencies = aggregateWords(sentences);
        int maxFrequency = getMaxFrequency(frequencies, parsedWikiDocument.getTitle());

        if (extractionForTraining) {
            getIdentifiersWithGoldInfo(
                    parsedWikiDocument,
                    collector,
                    allIdentifierDefininesCandidates,
                    sentences,
                    identifierSentenceDistanceMap,
                    frequencies,
                    maxFrequency
            );
        } else {
            WikiDocumentOutput out = getAllIdentifiers(
                    parsedWikiDocument,
                    allIdentifierDefininesCandidates,
                    sentences,
                    identifierSentenceDistanceMap,
                    frequencies,
                    maxFrequency
            );
            if ( out != null )
                collector.collect( out );
        }
    }

    private void getIdentifiersWithGoldInfo(ParsedWikiDocument doc, Collector<WikiDocumentOutput> collector, List<Relation> allIdentifierDefininesCandidates, List<Sentence> sentences, Map<String, Integer> identifierSentenceDistanceMap, Multiset<String> frequencies, double maxFrequency) {
        List<GoldEntry> matchingEntries = goldEntries
                .stream()
                .filter(
                        e -> e
                                .getTitle()
                                .equals(
                                        doc.getTitle().replaceAll(" ", "_"))
                                )
                .collect( Collectors.toList() );
        for ( GoldEntry entry : matchingEntries ){
            WikiDocumentOutput output = getIdentifiersWithGoldInfoInternal(
                    entry,
                    doc,
                    allIdentifierDefininesCandidates,
                    sentences,
                    identifierSentenceDistanceMap,
                    frequencies,
                    maxFrequency
            );
            if ( output != null )
                collector.collect( output );
        }
    }

    private WikiDocumentOutput getIdentifiersWithGoldInfoInternal(
            GoldEntry goldEntry,
            ParsedWikiDocument doc,
            List<Relation> allIdentifierDefininesCandidates,
            List<Sentence> sentences,
            Map<String, Integer> identifierSentenceDistanceMap,
            Multiset<String> frequencies,
            double maxFrequency
    ) {
        // get all math tags from document
        Stream<MathTag> stream = doc.getFormulas().stream();
        stream = stream.filter(
                e -> e.getMarkUpType().equals(WikiTextUtils.MathMarkUpType.LATEX) ||
                        e.getMarkUpType().equals(WikiTextUtils.MathMarkUpType.MATHML));

        final Integer fid = Integer.parseInt(goldEntry.getFid());

        List<MathTag> list = stream.collect(Collectors.toList());
        MathTag seed = null;
        if (list == null) {
            LOGGER.warn("List of math tags is null!");
        } else if (fid == -1) {
            try {
                // DLMF case -> need to find fid based extractions
                // thats hacky and dirty, but anyway...
                List<MathTag> tags = GoldUtil.getMMLMathTagFromGoldEntry(goldEntry, list);
                seed = tags.get(0);
            } catch (Exception e) {
                LOGGER.warn("Cannot find math tag of given gold entry.");
                return null;
            }
        } else {
            seed = list.get(fid);
        }

        Multiset<String> seedIdentifiers = seed.getIdentifiers(config);

        // update gold entries with unicode characters
        if (seed.getMarkUpType() == WikiTextUtils.MathMarkUpType.MATHML) {
            updateGouldiEntryToUnicode(seedIdentifiers, goldEntry);
        }

        for (int i = 0; i < sentences.size(); i++) {
            Sentence sentence = sentences.get(i);
            if (!sentence.getIdentifiers().isEmpty()) {
                LOGGER.debug("sentence {}", sentence);
            }

            Set<String> identifiers = sentence.getIdentifiers(); // both unicode characters from MML OR simple TeX
            identifiers.retainAll(seedIdentifiers.elementSet()); // also unicode characters from MML OR simple TeX

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
        result.setMaxSentenceLength(doc.getSentences().stream().map(s -> s.getWords().size()).max(Comparator.naturalOrder()).get());
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
        LOGGER.info("extracted {} relations from {}", allIdentifierDefininesCandidates.size(), doc.getTitle());
        //TODO hardcoded QID just for tests
        WikiDocumentOutput result = new WikiDocumentOutput(doc.getTitle(), "101", allIdentifierDefininesCandidates, doc.getIdentifiers());
        Optional<Integer> lengthOfLongestSentence = doc.getSentences().stream().map(s -> s.getWords().size()).max(Comparator.naturalOrder());
        //one as save value, since 0 would lead to NAN in division.
        result.setMaxSentenceLength(lengthOfLongestSentence.isPresent() ? lengthOfLongestSentence.get() : 1);
        return result;
    }

    private void updateGouldiEntryToUnicode( Multiset<String> unicodeIdentifier, GoldEntry entry ){
        HashMap<String, String> backTranslation = new HashMap<>();
        for (String uniString : unicodeIdentifier) {
            String asciString = UnicodeMap.string2TeX(uniString);
            backTranslation.put(asciString, uniString);
        }

        ArrayList<IdentifierDefinition> defs = entry.getDefinitions();
        for (int i = 0; i < defs.size(); i++) {
            String backTrans = backTranslation.get(defs.get(i).getIdentifier());
            if (backTrans != null) {
                defs.get(i).setIdentifier(backTrans);
            }
        }
    }

    public static Map<String, Integer> findSentencesWithIdentifierFirstOccurrences(List<Sentence> sentences, Collection<String> identifiers) {
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
