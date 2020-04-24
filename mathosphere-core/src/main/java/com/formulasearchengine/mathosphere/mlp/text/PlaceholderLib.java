package com.formulasearchengine.mathosphere.mlp.text;

import java.util.regex.Pattern;

/**
 * @author Andre Greiner-Petter
 */
public class PlaceholderLib {
    public static final String PREFIX_FORMULA = "FORMULA_";
    public static final String PREFIX_LINK = "LINK_";
    public static final String PREFIX_CITE = "CITE_";

    public static final Pattern PATTERN = Pattern.compile(
            "((?:"+PREFIX_FORMULA+"|"+PREFIX_LINK+"|"+PREFIX_CITE+")\\S+)"
    );
}
