package com.formulasearchengine.mathosphere.mlp.text;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.CharUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

public class UnicodeMap {

  private static  Map<Integer, String> MAP = null;

  private static void buildMap() {
    // see table here
    // http://unicode-table.com/en/blocks/letterlike-symbols/

    ImmutableMap.Builder<Integer, String> unicode2tex = ImmutableMap.builder();
    try {
      Reader in = new InputStreamReader(UnicodeMap.class.getClassLoader().getResourceAsStream("unicode2tex.csv"));
      Iterable<CSVRecord> records = CSVFormat.RFC4180.withHeader().parse(in);
      for (CSVRecord record : records) {
        String sUni = record.get("unicode");
        String sTex = record.get("latex");
        unicode2tex.put(Integer.parseInt(sUni, 16), sTex);
      }
    } catch (java.io.IOException e) {
      LOGGER.error("unicode2tex-problem");
      e.printStackTrace();
    }
    MAP =  unicode2tex.build();
  }

  private static final Logger LOGGER = LogManager.getLogger(UnicodeMap.class.getName());

  public static String string2TeX(String in) {
    int[] chars = in.codePoints().toArray();
    StringBuilder res = new StringBuilder();

    boolean previousHitIntroducedMacro = false;
    for (int code : chars) {
      String s = normalizeMathTex(char2TeX(code));
      if ( previousHitIntroducedMacro && s.matches("[a-zA-Z]+") ) res.append(" ");
      previousHitIntroducedMacro = s.matches( "\\\\[a-zA-Z]+" );
      res.append(s);
    }
    String s = res.toString().trim();
    if (chars.length == 1) {
      s = s.replaceAll("^\\{(.*)\\}$", "$1");
    }

    return s;
  }

  public static String normalizeMathTex(String in) {
    if ( in.matches( "\\\\dashv" ) ) in = "-"; // well, let's be honest... there is no visible difference between "-" and dashv
    else if ( in.matches( "\\\\hspace\\{.*}" ) ) in = " ";
    return in;
  }

  public static String char2TeX(int codePoint) {
    if ( MAP == null){
      buildMap();
    }
    if (CharUtils.isAsciiPrintable((char) codePoint)) {
      return CharUtils.toString((char) codePoint);
    }
    final String tex = MAP.get(codePoint);
    if (tex != null) {
      if (tex.endsWith("}") || tex.length() == 1) {
        return tex;
      }
//      return "{" + tex + "}";
      return tex;
    }

    LOGGER.debug("invalid char", codePoint);
    return "";
  }
}
