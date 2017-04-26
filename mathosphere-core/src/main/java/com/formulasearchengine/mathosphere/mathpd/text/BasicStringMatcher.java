package com.formulasearchengine.mathosphere.mathpd.text;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic String Matching Algorithm based on the {@link BoyerMooreSearch}.
 * All matches are found, including repetitions after the first match.
 * <br />
 * Findings are independent of the capitalization, otherwise the
 * tokenizer is not worth mentioning.
 * <p>
 * Description: <br/>
 * Basic String Matching finds exact text matches with n-words, including
 * repetitions after the first match.
 *
 * @author Vincent Stange
 */
public class BasicStringMatcher {

    /* Matches should at least find some words to increase significance */
    private int minWordLength;

    /* Minimum character length of each pattern found to increase significance */
    private int minPatternLength;

    /**
     * Creates a BasicStringMatcher with default parameters.
     */
    public BasicStringMatcher() {
        minWordLength = 6;
        minPatternLength = 12;
    }

    /**
     * Creates a BasicStringMatcher
     *
     * @param minWordLength    Minimum word of a pattern.
     * @param minPatternLength Minimum character length of each pattern found.
     */
    public BasicStringMatcher(int minWordLength, int minPatternLength) {
        this.minWordLength = minWordLength;
        this.minPatternLength = minPatternLength;
    }

    /**
     * A match-array is
     * [start index text 1, end index text 1 , start index text 2, end index text 2 ]
     *
     * @param text1 Text 1
     * @param text2 Text 2
     * @return list of matches.
     * @throws Exception ArrayOutOfBoundException
     *                   The alphabet in BoyerMoore is too small?
     */
    public List<int[]> getMatches(String text1, String text2) throws Exception {
        return reconcileOverlappings(compare(text1, text2));
    }

    /**
     * Compares two texts and returns every match with the exact length
     * of x words - whereby x is the parameter minWordLength.
     *
     * @param text1 Text 1
     * @param text2 Text 2
     * @return ordered list of matches between text1 and text2
     * @throws Exception I would assume arrayoutofbound.
     *                   The alphabet in BoyerMoore is too small?
     */
    List<int[]> compare(String text1, String text2) throws Exception {
        // prepare our result
        BoyerMooreSearch bms = new BoyerMooreSearch();
        List<int[]> matches = new ArrayList<>();

        // prepare text1 - simplistic tokenizer
        text1 = normalizeString(text1);
        String[] split1 = text1.split(" ");
        // prepare text2
        text2 = normalizeString(text2);
        char[] text2Array = text2.toCharArray();

        int startIdxA = 0; // our current pointer for document A
        int startIdxB = 0; // our current pointer for document B
        for (int i = 0; i < (split1.length - minWordLength); ) {

            // prepare pattern string
            String sw = "";
            int wc = 0;
            for (; wc < minWordLength; wc++) {
                sw = sw + (wc == 0 ? "" : " ") + split1[i + wc];
            }
            char[] pattern = sw.toCharArray();
            int patternLng = sw.length();

            // search for each pattern (minimum requirement)
            if (patternLng >= minPatternLength) {
                startIdxB = bms.search(pattern, text2Array, startIdxB, 0);
                if (startIdxB != -1) {
                    matches.add(new int[]{startIdxA, startIdxA + patternLng, startIdxB, startIdxB + patternLng, patternLng});

                    // jump over to the next words in docB after a complete match
                    startIdxB += patternLng + 1;
                    continue;
                }
            }

            startIdxA += split1[i].length() + 1;
            startIdxB = 0; // start anew in docB
            i++; // at last just take the next word in docA
        }

        return matches;
    }

    private String normalizeString(String text1) {
        text1 = text1.replaceAll("([\n\r ]+)", " ").toLowerCase();
        return text1;
    }

    /**
     * Mashes overlapping matches and saves every unique match into the textPattern.
     *
     * @param tmpMatches Ordered list of overlapping matches.
     */
    List<int[]> reconcileOverlappings(List<int[]> tmpMatches) {
        List<int[]> resultMatches = new ArrayList<>();
        if (tmpMatches.size() > 1) {
            int[] curMatch = tmpMatches.get(0);
            for (int i = 1; i < tmpMatches.size() - 1; i++) {
                int[] nextMatch = tmpMatches.get(i);
                if (isOverlapping(curMatch, nextMatch)) {
                    // expand and mash up
                    curMatch = new int[]{
                            Math.min(curMatch[0], nextMatch[0]),
                            Math.max(curMatch[1], nextMatch[1]),
                            Math.min(curMatch[2], nextMatch[2]),
                            Math.max(curMatch[3], nextMatch[3]),
                            Math.max(curMatch[1], nextMatch[1]) - Math.min(curMatch[0], nextMatch[0])
                    };
                } else {
                    // save as a unique match and move on
                    resultMatches.add(curMatch);
                    curMatch = nextMatch;
                }
            }
            // save the last match
            resultMatches.add(curMatch);
        }
        return resultMatches;
    }

    /**
     * Are two matches overlapping in both documents?
     * <p>
     * I use a simplification since I know the matches are ordered.
     * This means that all positions in match2 are after match1
     * or at the same position.
     *
     * @param m1 previous match
     * @param m2 succeeding match
     * @return do they overlap? [..[__]_]
     */
    boolean isOverlapping(int[] m1, int[] m2) {
        if (m1[0] <= m2[0] && m2[0] <= m1[1]) { // [..[__] in docA
            return m1[2] <= m2[2] && m2[2] <= m1[3]; // [..[__] in docB
        }
        return false;
    }

}
