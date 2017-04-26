package com.formulasearchengine.mathosphere.mathpd.text;

/**
 * Found as an example but modified it to be better usable.
 *
 * @author Vincent Stange
 */
public class BoyerMooreSearch {

    private static final int ALPHABET_SIZE = 64000;

    /* Initialize this array only once */
    private int[] charTable = new int[ALPHABET_SIZE];

    public int search(char[] pattern, char[] text, int startIdx, int endIdx) {
        int[] charTable = makeCharTable(pattern);
        int[] offsetTable = makeOffsetTable(pattern);

        // evaluate the end index, maximum is the end of the text
        endIdx = endIdx != 0 ? Math.min(endIdx, text.length) : text.length;

        for (int i = pattern.length + startIdx - 1, j; i < endIdx; ) {
            for (j = pattern.length - 1; pattern[j] == text[i]; i--, j--) {
                if (j == 0) {
                    return i;
                }
            }

            i += Math.max(charTable[text[i]], offsetTable[pattern.length - j - 1]);
        }
        return -1;
    }

    private int[] makeCharTable(char[] pattern) {
        for (int i = 0; i < charTable.length; i++) {
            charTable[i] = pattern.length;
        }

        for (int i = 0; i < pattern.length - 1; i++) {
            charTable[pattern[i]] = pattern.length - i - 1;
        }

        return charTable;
    }

    private static int[] makeOffsetTable(char[] pattern) {
        int[] offsetTable = new int[pattern.length];
        int lastPrefixPosition = pattern.length;

        for (int i = pattern.length - 1; i >= 0; --i) {
            if (isPrefix(pattern, i + 1)) {
                lastPrefixPosition = i + 1;
            }
            offsetTable[pattern.length - i - 1] = lastPrefixPosition - i + pattern.length - 1;
        }

        for (int i = 0; i < pattern.length - 1; i++) {
            int suffixLen = suffixLength(pattern, i);
            offsetTable[suffixLen] = pattern.length - i - 1 + suffixLen;
        }
        return offsetTable;
    }

    private static boolean isPrefix(char[] pattern, int p) {
        for (int i = p, j = 0; i < pattern.length; i++, j++) {
            if (pattern[i] != pattern[j]) {
                return false;
            }
        }
        return true;
    }

    private static int suffixLength(char[] pattern, int p) {
        int len = 0;
        for (int i = p, j = pattern.length - 1; i >= 0 && pattern[i] == pattern[j]; --i, --j) {
            len += 1;
        }
        return len;
    }

}
