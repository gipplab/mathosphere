package com.formulasearchengine.mathosphere.utils;

import com.formulasearchengine.mathosphere.mlp.pojos.MathTag;
import com.formulasearchengine.mlp.evaluation.pojo.GoldEntry;
import com.formulasearchengine.mlp.evaluation.pojo.IdentifierDefinition;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Leo on 21.01.2017.
 */
public class GoldUtil {
  private GoldUtil() {
  }

  /**
   * Checks if a identifier - definiens pair is in the specified gold data.
   *
   * @param identifier
   * @param definiens
   * @param gold
   * @return true if it is contained.
   */
  public static boolean matchesGold(String identifier, String definiens, GoldEntry gold) {
    List<IdentifierDefinition> identifierDefinitions = gold.getDefinitions();
    return identifierDefinitions.contains(new IdentifierDefinition(identifier, definiens.replaceAll("\\[|\\]", "").trim().toLowerCase()));
  }

  /**
   * Gets the GoldEntry for the supplied title.
   *
   * @param goldEntries The parsed gold file.
   * @param title       The title of the document.
   * @return the entry or throws an {@link NoSuchElementException} if the title is not in the list.
   */
  public static GoldEntry getGoldEntryByTitle(List<GoldEntry> goldEntries, String title) {
    return goldEntries.stream().filter(e -> e.getTitle().equals(title.replaceAll(" ", "_"))).findFirst().get();
  }

  private static final Pattern MML_TEX_TAG = Pattern.compile("alttext=\"(.*?)\"");
  private static final Pattern KERN_TEX = Pattern.compile("^[\\\\;,.?\\[({]*(.*?)[\\\\;,.?\\])}]*$");

  /**
   * Try to extract correct math tag element by a given gold entry. The reason is, that usually we don't have a
   * fid or another position element in the gold entry to know where exactly the gold entry is.
   * Note the result might be empty if no match were found or it contains multiple matches.
   *
   * @param gold the gold entry you want to find in the {@param mmlTags}
   * @param mmlTags the math tags that will be searched
   * @return list of found matches
   */
  public static List<MathTag> getMMLMathTagFromGoldEntry(GoldEntry gold, List<MathTag> mmlTags ) {
    String inputTex = gold.getMathInputTex();
    Matcher rawGoldMatcher = KERN_TEX.matcher(inputTex);
    String rawGold = rawGoldMatcher.group(1);

    return mmlTags
            .stream()
            .filter(
                    mtag -> {
                      Matcher alttext = MML_TEX_TAG.matcher(mtag.getContent());
                      if (alttext.find()) {
                        Matcher tex = KERN_TEX.matcher(alttext.group(1));
                        return tex.matches() && rawGold.matches(tex.group(1));
                      } else {
                        return false;
                      }
                    }
            )
            .collect( Collectors.toList() );
  }
}
