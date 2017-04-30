package com.formulasearchengine.mathosphere.mathpd.text;

import org.apache.flink.api.java.tuple.Tuple2;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

/**
 * Created by Moritz on 25.04.2017.
 */
public class BasicStringMatcherTest {

    private final Map<Integer, String> testCases;
    private final BasicStringMatcher matcher = new BasicStringMatcher(2, 4);

    public BasicStringMatcherTest() {
        testCases = new java.util.HashMap<>();
        testCases.put(1, "word word word");
        testCases.put(2, "word   öäüßß\0 word word");
        testCases.put(3, "word not a word");
    }

    @org.junit.Test
    public void compare() throws Exception {
        Map<Tuple2<Integer, Integer>, Integer> expectedMatchLengths = new HashMap<>();
        expectedMatchLengths.put(Tuple2.of(1, 1), 1);
        expectedMatchLengths.put(Tuple2.of(1, 2), 1);
        expectedMatchLengths.put(Tuple2.of(1, 3), 0);
        Function<Tuple2<Integer, Integer>, Integer> f = entry -> {
            try {
                return matcher.compare(testCases.get(entry.f0), testCases.get(entry.f1)).size();
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        };
        runTest(expectedMatchLengths, f);
    }

    @org.junit.Test
    public void getMatches() throws Exception {

    }

    @org.junit.Test
    public void isOverlapping() throws Exception {

    }

    @org.junit.Test
    public void reconcileOverlappings() throws Exception {

    }

    private <T> void runTest(Map<Tuple2<Integer, Integer>, T> expected, Function<Tuple2<Integer, Integer>, T> f) throws Exception {
        for (Entry<Tuple2<Integer, Integer>, T> entry : expected.entrySet()) {
            final String message = "Test tuple " + entry.getKey().f0 + ", " + entry.getKey().f1;
            if (entry.getValue() instanceof Double) {
                assertEquals(message, (Double) entry.getValue(), (Double) f.apply(entry.getKey()), 0.1);
            } else {
                assertEquals(message, entry.getValue(), f.apply(entry.getKey()));
            }
        }
    }

    @org.junit.Test
    public void score() throws Exception {
        Map<Tuple2<Integer, Integer>, Double> expectedMatchLengths = new HashMap<>();
        expectedMatchLengths.put(Tuple2.of(1, 1), 0.6);
        expectedMatchLengths.put(Tuple2.of(1, 2), 0.6);
        expectedMatchLengths.put(Tuple2.of(1, 3), 0.0);
        Function<Tuple2<Integer, Integer>, Double> f = entry -> {
            try {
                return matcher.scoreSimilarity(testCases.get(entry.f0), testCases.get(entry.f1));
            } catch (Exception e) {
                e.printStackTrace();
                return -1.;
            }
        };
        runTest(expectedMatchLengths, f);
    }
}