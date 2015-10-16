package com.formulasearchengine.mathosphere.mlp.rus;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Simple rule-based POS tagger for Russian that looks only at the ending or the
 * beginning of a word token to determine the part of speech.
 * <p>
 * The initial implementation taken from http://habrahabr.ru/post/152389/ and
 * rewritten to Java.
 */
public class RuleBasedPosTagger {

  /**
   * Tags taken from Penn Treebank
   * http://www.comp.leeds.ac.uk/amalgam/tagsets/upenn.html
   */
  public static enum PosTag {
    ADJECTIVE("JJ"),
    PARTICIPLE("VBG"),
    VERB("VB"),
    NOUN("NN"),
    ADVERB("RB"),
    NUMERAL("NUM"),
    CONJUCTION("CC"),
    PREPOSITION("IN"),
    QUOTES_OPEN("``"),
    QUOTES_CLOSE("''"),
    COMMA(","),
    DASH("--"),
    END_OF_SENTENCE("."),
    OTHER("X");

    private final String tag;

    private PosTag(String tag) {
      this.tag = tag;
    }

    public String getPennTag() {
      return tag;
    }
  }

  private final Map<PosTag, List<String>> rules = buildRulesMap();
  private final Map<String, PosTag> lookup = buildLookupMap();

  private static Map<PosTag, List<String>> buildRulesMap() {
    ImmutableMap.Builder<PosTag, List<String>> builder = ImmutableMap.builder();
    builder.put(PosTag.ADJECTIVE, Arrays.asList("ее", "ие", "ые", "ое", "ими", "ыми", "ей", "ий", "ый",
      "ой", "ем", "им", "ым", "ом", "его", "ого", "ему", "ому", "их", "ых", "ую", "юю", "ая", "яя",
      "ою", "ею"));
    builder.put(PosTag.PARTICIPLE, Arrays.asList("ивш", "ывш", "ующ", "ем", "нн", "вш", "ющ", "ущи",
      "ющи", "ящий", "щих", "щие", "ляя"));
    builder.put(PosTag.VERB, Arrays.asList("ила", "ыла", "ена", "ейте", "уйте", "ите", "или", "ыли",
      "ей", "уй", "ил", "ыл", "им", "ым", "ен", "ило", "ыло", "ено", "ят", "ует", "уют", "ит",
      "ыт", "ены", "ить", "ыть", "ишь", "ую", "ю", "ла", "на", "ете", "йте", "ли", "й", "л", "ем",
      "н", "ло", "ет", "ют", "ны", "ть", "ешь", "нно", "ться", "тся"));
    builder.put(PosTag.NOUN, Arrays.asList("а", "ев", "ов", "ье", "иями", "ями", "ами", "еи", "ии", "и",
      "ией", "ей", "ой", "ий", "й", "иям", "ям", "ием", "ем", "ам", "ом", "о", "у", "ах", "иях",
      "ях", "ы", "ь", "ию", "ью", "ю", "ия", "ья", "я", "ок", "мва", "яна", "ровать", "ег", "ги",
      "га", "сть", "сти", "не", "ремя", "емени", "еменем"));
    builder.put(PosTag.ADVERB, Arrays.asList("чно", "еко", "соко", "боко", "роко", "имо", "мно", "жно",
      "жко", "ело", "тно", "льно", "здо", "зко", "шо", "хо", "но", "ько"));
    builder.put(PosTag.NUMERAL, Arrays.asList("чуть", "много", "мало", "еро", "вое", "рое", "еро", "сти",
      "одной", "двух", "трех", "семи", "пяти", "ьми", "ати", "дного", "сто", "ста", "тысяча",
      "тысячи", "две", "три", "одна", "умя", "тью", "тремя", "тью", "мью", "тью", "одним"));
    builder.put(PosTag.CONJUCTION, Arrays.asList("более", "менее", "очень", "крайне", "скоре", "некотор",
      "кажд", "други", "котор", "когд", "однак", "если", "чтоб", "хот", "смотря", "как", "также",
      "так", "зато", "что", "или", "потом", "эт", "тог", "тоже", "словно", "ежели", "кабы", "коли",
      "ничем", "чем", "и"));
    builder.put(PosTag.PREPOSITION, Arrays.asList("в", "на", "по", "из"));

    return builder.build();
  }

  private static Map<String, PosTag> buildLookupMap() {
    ImmutableMap.Builder<String, PosTag> builder = ImmutableMap.builder();

    builder.put("и", PosTag.CONJUCTION);
    builder.put("а", PosTag.CONJUCTION);
    builder.put("но", PosTag.CONJUCTION);
    builder.put("когда", PosTag.CONJUCTION);
    builder.put("лишь", PosTag.CONJUCTION);
    builder.put("пока", PosTag.CONJUCTION);
    builder.put("едва", PosTag.CONJUCTION);
    builder.put("зато", PosTag.CONJUCTION);
    builder.put("либо", PosTag.CONJUCTION);
    builder.put("или", PosTag.CONJUCTION);
    builder.put("что", PosTag.CONJUCTION);
    builder.put("чтобы", PosTag.CONJUCTION);
    builder.put("как", PosTag.CONJUCTION);
    builder.put("если", PosTag.CONJUCTION);
    builder.put("ли", PosTag.CONJUCTION);
    builder.put("поскольку", PosTag.CONJUCTION);

    builder.put("без", PosTag.PREPOSITION);
    builder.put("в", PosTag.PREPOSITION);
    builder.put("до", PosTag.PREPOSITION);
    builder.put("для", PosTag.PREPOSITION);
    builder.put("за", PosTag.PREPOSITION);
    builder.put("из", PosTag.PREPOSITION);
    builder.put("к", PosTag.PREPOSITION);
    builder.put("на", PosTag.PREPOSITION);
    builder.put("над", PosTag.PREPOSITION);
    builder.put("о", PosTag.PREPOSITION);
    builder.put("об", PosTag.PREPOSITION);
    builder.put("от", PosTag.PREPOSITION);
    builder.put("под", PosTag.PREPOSITION);
    builder.put("пред", PosTag.PREPOSITION);
    builder.put("при", PosTag.PREPOSITION);
    builder.put("причем", PosTag.PREPOSITION);
    builder.put("про", PosTag.PREPOSITION);
    builder.put("с", PosTag.PREPOSITION);
    builder.put("у", PosTag.PREPOSITION);
    builder.put("через", PosTag.PREPOSITION);
    builder.put("между", PosTag.PREPOSITION);
    builder.put("пусть", PosTag.PREPOSITION);
    builder.put("потому", PosTag.PREPOSITION);

    builder.put("--", PosTag.DASH);
    builder.put(",", PosTag.COMMA);
    builder.put(".", PosTag.END_OF_SENTENCE);
    builder.put("!", PosTag.END_OF_SENTENCE);
    builder.put("?", PosTag.END_OF_SENTENCE);
    builder.put("``", PosTag.QUOTES_OPEN);
    builder.put("''", PosTag.QUOTES_CLOSE);

    return builder.build();

  }

  public PosTag posTag(String input) {
    String token = input.toLowerCase();// TODO: set locale

    if (lookup.containsKey(token)) {
      return lookup.get(token);
    }

    Map<PosTag, Integer> resLens = Maps.newHashMap();

    for (Map.Entry<PosTag, List<String>> group : rules.entrySet()) {
      PosTag groupKey = group.getKey();
      List<String> values = group.getValue();

      if (groupKey == PosTag.PARTICIPLE) {
        applyParticipleRule(token, resLens, groupKey, values);
      } else if (groupKey == PosTag.CONJUCTION) {
        applyConjunctionRule(token, resLens, groupKey, values);
      } else {
        applyGeneralRule(token, resLens, groupKey, values);
      }
    }

    List<Entry<PosTag, Integer>> result = sortResults(resLens);
    if (result.isEmpty()) {
      return PosTag.OTHER;
    } else {
      Entry<PosTag, Integer> tag = result.get(0);
      return tag.getKey();
    }
  }

  private void applyParticipleRule(String token, Map<PosTag, Integer> resLens, PosTag groupKey,
                                   List<String> values) {
    int wordLength = token.length();
    for (String part : values) {
      int lenPart = part.length();
      // participle with 40% or more of the token's length on the right
      if (token.indexOf(part) >= (2 * wordLength / 5)) {
        resLens.put(groupKey, lenPart);
      }
    }
  }

  private void applyConjunctionRule(String token, Map<PosTag, Integer> resLens, PosTag groupKey,
                                    List<String> values) {
    // conjunction, the beginning of word
    for (String part : values) {
      int lenPart = part.length();
      if (token.startsWith(part)) {
        if (resLens.containsKey(groupKey)) {
          if (resLens.get(groupKey) < lenPart) {
            resLens.put(groupKey, lenPart);
          }
        } else {
          resLens.put(groupKey, lenPart);
        }
      }
    }
  }

  private void applyGeneralRule(String token, Map<PosTag, Integer> resLens, PosTag groupKey,
                                List<String> values) {
    for (String part : values) {
      int lenPart = part.length();

      if (token.endsWith(part)) {
        if (resLens.containsKey(groupKey)) {
          if (resLens.get(groupKey) < lenPart) {
            resLens.put(groupKey, lenPart);
          }
        } else {
          resLens.put(groupKey, lenPart);
        }
      }

      if (token.equals(part)) {
        resLens.put(groupKey, 99);
      }
    }
  }

  private List<Entry<PosTag, Integer>> sortResults(Map<PosTag, Integer> resLens) {
    List<Entry<PosTag, Integer>> result = Lists.newArrayList(resLens.entrySet());
    result.sort(new Comparator<Map.Entry<PosTag, Integer>>() {
      @Override
      public int compare(Map.Entry<PosTag, Integer> o1, Map.Entry<PosTag, Integer> o2) {
        Integer value1 = o1.getValue();
        Integer value2 = o2.getValue();
        if (value1.equals(value2)) {
          int key1 = o1.getKey().ordinal();
          int key2 = o2.getKey().ordinal();
          return -Integer.compare(key1, key2);
        }

        return -value1.compareTo(value2);
      }
    });
    return result;
  }

}
