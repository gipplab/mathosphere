package com.formulasearchengine.mathosphere.mlp.text;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.CharUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.util.Map;

public class UnicodeMap {

  private static final Map<Integer, String> MAP = buildMap();

  private static Map<Integer, String> buildMap() {
    // see table here
    // http://unicode-table.com/en/blocks/letterlike-symbols/

    ImmutableMap.Builder<Integer, String> unicode2tex = ImmutableMap.builder();
    try {
      FileReader in = new FileReader(UnicodeMap.class.getClassLoader().getResource("unicode2tex.csv").getFile());
      Iterable<CSVRecord> records = CSVFormat.RFC4180.withHeader().parse(in);
      for (CSVRecord record : records) {
        String sUni = record.get("unicode");
        String sTex = record.get("latex");
        unicode2tex.put(Integer.parseInt(sUni,16), sTex);
      }
    } catch (java.io.IOException e) {
      LOGGER.error("unicode2tex-problem");
      e.printStackTrace();
    }
    return unicode2tex.build();
  }
  private static final Logger LOGGER = LoggerFactory.getLogger(UnicodeMap.class);
  public static String string2TeX(String in) {

    int[] chars = in.codePoints().toArray();
    StringBuilder res = new StringBuilder();

    for (int code : chars) {

      res.append(char2TeX(code));
    }

    return res.toString().trim();
  }

  public static String char2TeX(int codePoint) {
    if (MAP.containsKey(codePoint)) {
      return "{" + MAP.get(codePoint) + "}";
    } else {
      if (CharUtils.isAsciiPrintable((char)codePoint)){
        return CharUtils.toString((char) codePoint);
      }
      LOGGER.debug("invalid char",codePoint);
      return "";
    }
  }
}
