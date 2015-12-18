package com.formulasearchengine.mathosphere.mlp.text;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class WikdataLinkMap {

  private static final Logger LOGGER = LoggerFactory.getLogger(WikdataLinkMap.class);
  private final Map<String, String> map;

  public WikdataLinkMap(String fn) {
    map = buildMap(fn, true);
  }

  public WikdataLinkMap(String fn, boolean unique) {
    map = buildMap(fn, unique);
  }

  private static Map<String, String> buildMap(String fn, boolean unique) {
    // see table here
    // http://unicode-table.com/en/blocks/letterlike-symbols/
    Map<String, String> keys = new HashMap<>();
    ImmutableMap.Builder<String, String> title2Data = ImmutableMap.builder();
    try {
      FileReader in = new FileReader(fn);
      Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(in);
      for (CSVRecord record : records) {
        String sUni = record.get(0);
        String sTex = record.get(1);
        if (!unique) {
          if (keys.containsKey(sUni)) {
            int qNew = Integer.parseInt(sTex.replaceAll("Q(\\d+)", "$1"));
            int qOld = Integer.parseInt(keys.get(sUni).replaceAll("Q(\\d+)", "$1"));
            if (qNew > qOld) {
              continue;
            }
          }
          keys.put(sUni, sTex);
        } else {
          title2Data.put(sUni, sTex);
        }
      }
    } catch (java.io.IOException e) {
      LOGGER.error("title2Data-problem");
      e.printStackTrace();
    }
    if (!unique) {
      title2Data.putAll(keys);
    }
    return title2Data.build();
  }

  public String title2Data(String in) {
    in = in.replaceAll("\\[\\[([^\\|]+)\\|?(.*?)\\]\\]", "$1").trim().toLowerCase();
    if (map.containsKey(in)) {
      return map.get(in);
    } else {
      // some heuristics to improve mapping
      in = in.replaceAll("('s|\\(.*?\\))", "").trim();
    }
    return map.get(in.trim().toLowerCase());
  }

}
