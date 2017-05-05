package com.formulasearchengine.mlp.evaluation.pojo;

import java.io.Serializable;

/**
 * Created by Leo on 06.12.2016.
 * <p>
 * Holds the score of an evaluation.
 */
public class ScoreSummary implements Serializable {

  /**
   * Number of true positives.
   */
  public int tp;
  /**
   * Number of true positives.
   */
  public int duplicateTp;
  /**
   * Number of false positives.
   */
  public int fp;
  /**
   * Number of false negatives.
   */
  public int fn;
  /**
   * Number of found links to wikidata.
   *
   * @see <a href="https://www.wikidata.org/wiki/Wikidata:Main_Page">https://www.wikidata.org</a>
   */
  public int wikidatalinks;

  public ScoreSummary(int tp, int duplicateTp, int fn, int fp, int wikidatalinks) {
    this.duplicateTp = duplicateTp;
    this.fp = fp;
    this.fn = fn;
    this.wikidatalinks = wikidatalinks;
    this.tp = tp;
  }

  @Override
  public String toString() {
    return String.format("tp: %d, fn: %d, fp: %d, wikidatalinks: %d, duplicate tp: %d", tp, fn, fp, wikidatalinks, duplicateTp);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ScoreSummary that = (ScoreSummary) o;

    if (tp != that.tp) return false;
    if (duplicateTp != that.duplicateTp) return false;
    if (fp != that.fp) return false;
    if (fn != that.fn) return false;
    return wikidatalinks == that.wikidatalinks;

  }

  @Override
  public int hashCode() {
    int result = tp;
    result = 31 * result + duplicateTp;
    result = 31 * result + fp;
    result = 31 * result + fn;
    result = 31 * result + wikidatalinks;
    return result;
  }
}
