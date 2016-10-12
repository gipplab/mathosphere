package com.formulasearchengine.mathosphere.mlp.evaluation;

import com.formulasearchengine.mathosphere.mlp.pojos.GoldEntry;

import java.util.List;

/**
 * Created by Leo on 11.10.2016.
 */
public class IdentifierExtractionResult {
  private final String qID;
  private final String math_inputtex;
  private final List<String> identifier;
  private final List<String> tp;
  private final List<String> fp;
  private final List<String> fn;

  public String getMath_inputtex() {
    return math_inputtex;
  }

  public List<String> getTp() {
    return tp;
  }

  public List<String> getFp() {
    return fp;
  }

  public List<String> getFn() {
    return fn;
  }


  public String getqID() {
    return qID;
  }

  public List<String> getIdentifier() {
    return identifier;
  }

  public IdentifierExtractionResult(String qID, String mathInputTex, List<String> identifiers, List<String> tp, List<String> fp, List<String> fn) {
    this.qID = qID;
    this.math_inputtex = mathInputTex;
    this.identifier = identifiers;
    this.tp = tp;
    this.fp = fp;
    this.fn = fn;
  }
}
