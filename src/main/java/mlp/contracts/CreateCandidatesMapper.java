package mlp.contracts;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import mlp.Config;
import mlp.pojos.Relation;
import mlp.pojos.Sentence;
import mlp.pojos.WikiDocument;
import mlp.pojos.Word;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;

public class CreateCandidatesMapper implements FlatMapFunction<WikiDocument, Relation> {

    private final static Set<String> BLACKLIST = ImmutableSet.of("behavior", "infinity", "sum", "other", "=",
            "|", "·", "≥", "≤", "≠", "lim", "ƒ", "×", "/", "\\", "-", "function", "functions", "equation",
            "equations", "solution", "solutions", "result", "results");

    private double alpha;
    private double beta;
    private double gamma;

    public CreateCandidatesMapper(double alpha, double beta, double gamma) {
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
    }

    public CreateCandidatesMapper(Config config) {
        this(config.getAlpha(), config.getBeta(), config.getGamma());
    }

    @Override
    public void flatMap(WikiDocument doc, Collector<Relation> out) throws Exception {
        Set<String> identifiers = doc.getIdentifiers();

        for (String identifier : identifiers) {
            List<Relation> relations = generateCandidates(doc, identifier);
            relations.forEach(rel -> out.collect(rel));
        }
    }

    private List<Relation> generateCandidates(WikiDocument doc, String identifier) {
        List<Sentence> sentences = findSentencesWithIdentifier(doc.getSentences(), identifier);
        if (sentences.isEmpty()) {
            return Collections.emptyList();
        }

        List<Relation> result = Lists.newArrayList();
        Multiset<String> frequencies = calcFrequencies(sentences);
        int maxFrequency = calculateMax(frequencies);

        for (int sentenceIdx = 0; sentenceIdx < sentences.size(); sentenceIdx++) {
            Sentence sentence = sentences.get(sentenceIdx);
            List<Word> words = sentence.getWords();
            List<Integer> positions = identifierPositions(words, identifier);

            for (int wordIdx = 0; wordIdx < words.size(); wordIdx++) {
                Word word = words.get(wordIdx);
                if (!isGood(word)) {
                    continue;
                }

                int identifierPosition = closestIdentifierPosition(positions, wordIdx);
                int distance = Math.abs(identifierPosition - wordIdx);

                int freq = frequencies.count(word.word.toLowerCase());
                double score = calculateScore(distance, freq, maxFrequency, sentenceIdx);

                Relation relation = new Relation();
                relation.setDocumentTitle(doc.getTitle());
                relation.setIdentifier(identifier);
                relation.setIdentifierPosition(identifierPosition);
                relation.setWord(word);
                relation.setWordPosition(wordIdx);
                relation.setScore(score);
                relation.setSentence(sentence);

                result.add(relation);
            }
        }

        return result;
    }

    private double calculateScore(int distance, int frequency, int maxFrequency, int sentenceIdx) {
        double std1 = Math.sqrt(Math.pow(5d, 2d) / (2d * Math.log(2)));
        double dist = gaussian(distance, std1);

        double std2 = Math.sqrt(Math.pow(3d, 2d) / (2d * Math.log(2)));
        double seq = gaussian(sentenceIdx, std2);

        double relativeFrequency = (double) frequency / (double) maxFrequency;
        return (alpha * dist + beta * seq + gamma * relativeFrequency) / (alpha + beta + gamma);
    }

    private static double gaussian(double x, double std) {
        return Math.exp(- x * x / (2 * std * std));
    }

    public static List<Integer> identifierPositions(List<Word> sentence, String identifier) {
        List<Integer> result = Lists.newArrayList();

        for (int wordIdx = 0; wordIdx < sentence.size(); wordIdx++) {
            Word word = sentence.get(wordIdx);
            if (Objects.equals(identifier, word.word)) {
                result.add(wordIdx);
            }
        }

        return result;
    }

    public static int closestIdentifierPosition(List<Integer> positions, int wordIdx) {
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
                    counts.add(word.word.toLowerCase());
                }
            }
        }

        return counts;
    }

    private boolean isGood(Word in) {
        String word = in.word;
        String posTag = in.posTag;

        if (BLACKLIST.contains(word.toLowerCase())) {
            return false;
        }

        if ("ID".equals(posTag)) {
            return false;
        }

        // we're only interested in nouns, entities and links
        if (posTag.matches("NN[PS]{0,2}|NP\\+?|NN\\+|LNK")) {
            return true;
        }

        return false;
    }

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
