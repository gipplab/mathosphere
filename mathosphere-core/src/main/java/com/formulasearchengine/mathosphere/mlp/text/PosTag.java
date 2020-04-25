package com.formulasearchengine.mathosphere.mlp.text;

public class PosTag {
  public static final String IDENTIFIER = "ID";
  public static final String LINK = "LNK";
  public static final String MATH = "MATH";
  public static final String CITE = "CITE";
  public static final String SYMBOL = "SYM";

  public static final String ADJECTIVE = "JJ";
  public static final String ADJECTIVE_COMPARATIVE = "JJR";
  public static final String ADJECTIVE_SUPERLATIVE = "JJS";
  public static final String ADJECTIVE_SEQUENCE = "JJ+";
  public static final String ANY_ADJECTIVE_REGEX = "JJ[RS+]?";

  public static final String NOUN = "NN";
  public static final String NOUN_PHRASE = "NP";
  public static final String NOUN_PROPER = "NNP";
  public static final String NOUN_PLURAL = "NNS";
  public static final String NOUN_PROPER_PLURAL = "NNPS";
  public static final String ANY_NOUN_REGEX = "N(?:P|N[PS]?|NPS)";

  public static final String QUOTE = "``";
  public static final String UNQUOTE = "''";

//  public static final String NOUN_SEQUENCE = "NN+";
//  public static final String NOUN_SEQUENCE_PHRASE = "NP+";

  public static final String FOREIGN_WORD = "FW";

  public static final String SUFFIX = "-SUF";
}
