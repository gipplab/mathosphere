package com.formulasearchengine.mathosphere.mlp.text;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Lists;

import org.apache.commons.lang3.CharUtils;

import java.lang.Character.UnicodeBlock;
import java.util.List;
import java.util.Map;

public class UnicodeUtils {

  private static final int BOLD_A = 119808;
  private static final int MONOSPACE_z = 120483;

  private static final int BOLD_ALPHA = 120488;
  private static final int BOLD_ITALIC_VAR_PI_SMALL = 120777;

  private static final int BOLD_0 = 120782;
  private static final int MONOSPACE_9 = 120831;

  private static final List<String> LATIN_NORMAL = asList("ABCDEFGHIJKLMNOPQRSTUVWXYZ"
      + "abcdefghijklmnopqrstuvwxyz");
  private static final List<String> GREEK_NORMAL = asList("ΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡϴΣΤΥΦΧΨΩ" + "\u2207"
      + "αβγδεζηθικλμνξοπρςστυφχψω∂ϵϑϰϕϱϖ");
  private static final List<String> DIGIT_NORMAL = asList("0123456789");

  private static final Map<Integer, String> LETTER_LIKE_MAPPING = buildLetterLikeMap();

  private static Map<Integer, String> buildLetterLikeMap() {
    // see table here
    // http://unicode-table.com/en/blocks/letterlike-symbols/

    Builder<Integer, String> letterLike = ImmutableMap.builder();
    letterLike.put(0x2102, "C");
    letterLike.put(0x210A, "g");
    letterLike.put(0x210B, "H");
    letterLike.put(0x210C, "H");
    letterLike.put(0x210D, "H");
    letterLike.put(0x210F, "h");
    letterLike.put(0x2110, "I");
    letterLike.put(0x2111, "I");
    letterLike.put(0x2112, "L");
    letterLike.put(0x2113, "l");
    letterLike.put(0x2115, "N");
    letterLike.put(0x2118, "P");
    letterLike.put(0x2119, "P");
    letterLike.put(0x211A, "Q");
    letterLike.put(0x211B, "R");
    letterLike.put(0x211C, "R");
    letterLike.put(0x211D, "R");
    letterLike.put(0x2124, "Z");
    letterLike.put(0x2126, "Ω");
    letterLike.put(0x212C, "B");
    letterLike.put(0x212D, "C");
    letterLike.put(0x212F, "e");
    letterLike.put(0x2130, "E");
    letterLike.put(0x2131, "F");
    letterLike.put(0x2133, "M");
    letterLike.put(0x2134, "o");
    letterLike.put(0x213C, "π");
    letterLike.put(0x213D, "γ");
    letterLike.put(0x213E, "Γ");
    letterLike.put(0x213F, "Π");
    letterLike.put(0x2140, "Σ");
    letterLike.put(0x2145, "D");
    letterLike.put(0x2146, "d");
    letterLike.put(0x2147, "e");
    letterLike.put(0x2148, "i");
    letterLike.put(0x2149, "j");

    return letterLike.build();
  }

  public static String normalizeString(String in) {
    int[] chars = in.codePoints().toArray();
    StringBuilder res = new StringBuilder(in.length());

    for (int code : chars) {
      res.append(normalizeCharacter(code));
    }

    return res.toString();
  }

  public static String normalizeCharacter(int codePoint) {
    if (!Character.isValidCodePoint(codePoint)) {
      return "";
    }

    // TODO: long search? maybe replace with own implementation
    UnicodeBlock block = Character.UnicodeBlock.of(codePoint);
    if (block == Character.UnicodeBlock.MATHEMATICAL_ALPHANUMERIC_SYMBOLS) {
      return normalizeMath(codePoint);
    }

    if (block == Character.UnicodeBlock.LETTERLIKE_SYMBOLS) {
      // TODO why replacing planck constant by h?
      return normalizeLetterLike(codePoint);
    }

    return codePointToString(codePoint);
  }

  private static String normalizeMath(int codePoint) {
    // see here
    // http://unicode-table.com/en/blocks/mathematical-alphanumeric-symbols/

    if (isMathLatin(codePoint)) {
      return processLatin(codePoint);
    }

    if (isMathGreek(codePoint)) {
      return processGreek(codePoint);
    }

    if (isMathDigit(codePoint)) {
      return processDigit(codePoint);
    }

    return codePointToString(codePoint);
  }

  /**
   * Detects the following types of mathematical unicode chars: <ul> <li>bold</li> <li>italic</li>
   * <li>bold italic</li> <li>script</li> <li>bold script</li> <li>fraktur</li>
   * <li>double-struck</li> <li>bold fraktur</li> <li>sans-serif</li> <li>sans-serif bold</li>
   * <li>sans-serif italic</li> <li>sans-serif bold italic</li> <li>monospace</li> </ul>
   *
   * @param codePoint character code to check
   * @return <code>true</code> if the code belongs to one of the mentioned categories
   */
  public static boolean isMathLatin(int codePoint) {
    return BOLD_A <= codePoint && codePoint <= MONOSPACE_z;
  }

  /**
   * Detects the following types of mathematical unicode chars of the Greek alphabet: <ul>
   * <li>bold</li> <li>italic</li> <li>bold italic</li> </ul> <p> Also handles extra symbols like
   * \varphi, \varpi, \nabla
   *
   * @param codePoint character code to check
   * @return <code>true</code> if the code belongs to one of the mentioned categories
   */
  public static boolean isMathGreek(int codePoint) {
    return BOLD_ALPHA <= codePoint && codePoint <= BOLD_ITALIC_VAR_PI_SMALL;
  }

  /**
   * Detects the following types of mathematical unicode chars for digits: <ul> <li>bold</li>
   * <li>double-struck</li> <li>sans-serif</li> <li>sans-serif bold</li> <li>monospace</li> </ul>
   *
   * @param codePoint character code to check
   * @return <code>true</code> if the code belongs to one of the mentioned categories
   */
  public static boolean isMathDigit(int codePoint) {
    return BOLD_0 <= codePoint && codePoint <= MONOSPACE_9;
  }

  private static String processLatin(int codePoint) {
    return replace(codePoint, BOLD_A, LATIN_NORMAL);
  }

  private static String processGreek(int codePoint) {
    return replace(codePoint, BOLD_ALPHA, GREEK_NORMAL);
  }

  private static String processDigit(int codePoint) {
    return replace(codePoint, BOLD_0, DIGIT_NORMAL);
  }

  private static String replace(int codePoint, int firstCharacterInGroup, List<String> referenceList) {
    int relative = codePoint - firstCharacterInGroup;
    int taget = relative % referenceList.size();
    return referenceList.get(taget);
  }

  private static List<String> asList(String string) {
    List<String> list = Lists.newArrayListWithCapacity(string.length());
    string.chars().forEach(c -> list.add(CharUtils.toString((char) c)));
    return list;
  }

  private static String codePointToString(int codePoint) {
    if (Character.isBmpCodePoint(codePoint)) {
      return CharUtils.toString((char) codePoint);
    }

    char[] chars = Character.toChars(codePoint);
    return new String(chars);
  }

  private static String normalizeLetterLike(int codePoint) {
    if (LETTER_LIKE_MAPPING.containsKey(codePoint)) {
      return LETTER_LIKE_MAPPING.get(codePoint);
    } else {
      return codePointToString(codePoint);
    }
  }

}
