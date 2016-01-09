package com.formulasearchengine.mathosphere.mlp.text;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class DefinitionUtils {

  private static final Set<String> DEFINITION_BLACKLIST = ImmutableSet.of("unit", "units", "value", "values",
      "axis", "axes", "factor", "factors", "line", "lines",
      "point", "points", "number", "numbers", "variable", "variables", "respect", "case", "cases",
      "vector", "vectors", "element", "elements", "example",
      "integer", "integers", "term", "terms", "parameter", "parameters", "coefficient", "coefficients",
      "formula", "times", "product", "matrices", "expression", "complex", "real", "zeros", "bits",
      "sign",
      "if and only if",
      "alpha", "beta", "gamma", "delta", "epsilon", "zeta", "eta", "theta", "iota", "kappa", "lambda",
      "mu", "nu", "xi", "omicron", "pi", "rho", "sigma", "tau", "upsilon", "phi", "chi", "psi", "omega");

  public static boolean isValid(String definition) {
    if (definition.length() <= 3) {
      return false;
    }

    if (DEFINITION_BLACKLIST.contains(definition.toLowerCase())) {
      return false;
    } else {
      return true;
    }
  }
}
