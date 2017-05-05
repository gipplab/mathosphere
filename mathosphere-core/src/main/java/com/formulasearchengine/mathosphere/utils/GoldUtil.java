package com.formulasearchengine.mathosphere.utils;

import com.formulasearchengine.mlp.evaluation.pojo.GoldEntry;
import com.formulasearchengine.mlp.evaluation.pojo.IdentifierDefinition;

import java.util.List;
import java.util.NoSuchElementException;

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
}
