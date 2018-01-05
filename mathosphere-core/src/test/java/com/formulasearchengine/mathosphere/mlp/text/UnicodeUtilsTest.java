package com.formulasearchengine.mathosphere.mlp.text;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class UnicodeUtilsTest {

  private static final Logger LOGGER = LogManager.getLogger(UnicodeUtilsTest.class.getName());

  String capitals = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  String smalls = "abcdefghijklmnopqrstuvwxyz";

  String greekCapitals = "ΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡϴΣΤΥΦΧΨΩ\u2207";
  String greekSmalls = "αβγδεζηθικλμνξοπρςστυφχψω∂ϵϑϰϕϱϖ";

  String digits = "0123456789";

  @Test
  public void normalizeString_boldA() {
    String boldA = "\uD835\uDC00";
    String result = UnicodeUtils.normalizeString(boldA);
    assertEquals("A", result);
  }

  @Test
  public void normalizeString_mathLatinLetters() throws Exception {
    List<String> lines = readTestData();

    for (int i = 0; i < 26; i++) {
      String expected = i % 2 == 0 ? capitals : smalls;
      String mathString = lines.get(i);
      LOGGER.info("test data {}", mathString);

      String result = UnicodeUtils.normalizeString(mathString);
      assertEquals(expected, result);
    }
  }

  @Test
  public void normalizeString_greekLetters() throws Exception {
    List<String> lines = readTestData();

    for (int i = 26; i < 36; i++) {
      String expected = i % 2 == 0 ? greekCapitals : greekSmalls;
      String mathString = lines.get(i);
      LOGGER.info("test data {}", mathString);

      String result = UnicodeUtils.normalizeString(mathString);
      assertEquals(expected, result);
    }
  }

  @Test
  public void normalizeString_digits() throws Exception {
    List<String> lines = readTestData();

    for (int i = 36; i < 41; i++) {
      String mathString = lines.get(i);
      LOGGER.info("test data {}", mathString);
      String result = UnicodeUtils.normalizeString(mathString);
      assertEquals(digits, result);
    }
  }

  @Test
  public void normalize_letterLikeSymbols() throws Exception {
    List<String> lines = readTestData("unicode2.txt");
    for (String line : lines) {
      String[] split = line.split(" ");
      String expected = split[0];
      String actual = UnicodeUtils.normalizeString(split[1]);
      assertEquals(expected, actual);
    }
  }

  private List<String> readTestData(String res) throws IOException {
    InputStream is = UnicodeUtils.class.getResourceAsStream(res);
    return IOUtils.readLines(is);
  }

  private List<String> readTestData() throws IOException {
    return readTestData("unicode.txt");
  }

}
