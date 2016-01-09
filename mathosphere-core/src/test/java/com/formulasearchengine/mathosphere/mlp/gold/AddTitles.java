package com.formulasearchengine.mathosphere.mlp.gold;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Moritz on 02.01.2016.
 */
public class AddTitles {

  @Test
  @Ignore
  public void addTitles() throws IOException {
    final File file = new File(getClass().getResource("gold.json").getFile());
    Map<Integer, String> titles = new HashMap<>();
    FileReader in = new FileReader(new File(getClass().getResource("titles.csv").getFile()));
    Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(in);
    for (CSVRecord record : records) {
      titles.put(Integer.valueOf(record.get(0)), record.get(1));
    }
    ObjectMapper mapper = new ObjectMapper();
    List userData = mapper.readValue(file, List.class);
    for (Object o : userData) {
      final Map entry = (Map) o;
      Map formula = (Map) entry.get("formula");
      formula.put("title", titles.get(Integer.parseInt(String.valueOf(formula.get("oldId")))));
    }
    mapper.writeValue(file, userData);
  }
}
