package mlp.rus;

import mlp.rus.RuleBasedPosTagger.PosTag;
import org.junit.Test;

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

  @Test
  public void testPosTag() {
    String[] split = text.split("\\s+");

    for (String token : split) {
      String tag = tagger.posTag(token).getPennTag();
      System.out.println(token + " " + tag);
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
