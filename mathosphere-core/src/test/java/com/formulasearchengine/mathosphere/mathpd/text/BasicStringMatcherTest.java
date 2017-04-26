package com.formulasearchengine.mathosphere.mathpd.text;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Moritz on 25.04.2017.
 */
public class BasicStringMatcherTest {
    @org.junit.Test
    public void getMatches() throws Exception {

    }

    @org.junit.Test
    public void compare() throws Exception {
        final String s1 = "This is the unique string one with more than six words.";
        final String s2 = "This is the unique string   one with more than six words.";
        final BasicStringMatcher matcher = new BasicStringMatcher();
        final List<int[]> ints = matcher.compare(s1, s2);
        assertEquals(5,ints.size());
    }

    @org.junit.Test
    public void reconcileOverlappings() throws Exception {

    }

    @org.junit.Test
    public void isOverlapping() throws Exception {

    }
}