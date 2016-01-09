package com.formulasearchengine.mathosphere.mlp.rus;

import com.formulasearchengine.mathosphere.mlp.rus.RuleBasedPosTagger.PosTag;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;

import java.io.FileReader;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class RuleBasedPosTaggerTest {

  RuleBasedPosTagger tagger = new RuleBasedPosTagger();

  String text = "Вы сейчас строите предсказатель на основе набора окончаний а правила подбираете вручную "
      + "на основании своих предположений и наблюдений Со словарем этот же набор правил окончание "
      + "часть речи можно построить автоматически более полно и правильно что и делается в phpmorphy "
      + "и большинстве других морф анализаторов для русского языка Благо словари для русского языка "
      + "доступные и хорошие Кроме того раз уж есть словарь то необязательно полагаться только на "
      + "предсказатель если слово есть в словаре то можно и точный разбор вернуть ну с учетом "
      + "неустранимой на уровне отдельных слов неоднозначности разбора";
  HashMap<String, String> expecteTags;

  @Before
  public void setUp() throws Exception {
    FileReader in = new FileReader(getClass().getResource("exp-tags.csv").getFile());
    Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(in);
    expecteTags = new HashMap<>();
    for (CSVRecord record : records) {
      String word = record.get(0);
      String type = record.get(1);
      expecteTags.put(word, type);
    }

  }

  @Test
  public void testPosTag() {
    String[] split = text.split("\\s+");

    for (String token : split) {
      String tag = tagger.posTag(token).getPennTag();
      assertEquals(expecteTags.get(token), tag);
    }

  }

  @Test
  public void someTestCases() {
    assertEquals(PosTag.CONJUCTION, tagger.posTag("и"));
    assertEquals(PosTag.CONJUCTION, tagger.posTag("поскольку"));
    assertEquals(PosTag.PREPOSITION, tagger.posTag("причем"));
    assertEquals(PosTag.PREPOSITION, tagger.posTag("между"));
    assertEquals(PosTag.VERB, tagger.posTag("называется"));
    assertEquals(PosTag.VERB, tagger.posTag("делится"));
    assertEquals(PosTag.ADVERB, tagger.posTag("только"));
    assertEquals(PosTag.PREPOSITION, tagger.posTag("пусть"));
    assertEquals(PosTag.NOUN, tagger.posTag("время"));
  }

}
