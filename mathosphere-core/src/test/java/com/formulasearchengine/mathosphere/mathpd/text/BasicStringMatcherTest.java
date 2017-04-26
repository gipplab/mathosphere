package com.formulasearchengine.mathosphere.mathpd.text;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Moritz on 25.04.2017.
 */
public class BasicStringMatcherTest {

    private final String s1 = "This is the unique string one with more than six words.";
    private final String s2 = "This is the unique string   one with more than six words.";


    @org.junit.Test
    public void compare() throws Exception {
        final BasicStringMatcher matcher = new BasicStringMatcher();
        final List<int[]> ints = matcher.compare(s1, s2);
        assertEquals(5, ints.size());
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

    @org.junit.Test
    public void score() throws Exception {
        final BasicStringMatcher matcher = new BasicStringMatcher();
        final double ints = matcher.scoreSimilarity(s1, s2);
        assertEquals(1, ints, .01);
    }
}