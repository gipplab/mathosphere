package com.formulasearchengine.mathosphere.mlp.text;

import java.util.regex.Pattern;

/**
 * @author Andre Greiner-Petter
 */
public class PlaceholderLib {
    public static final String FORMULA = "FORMULA";
    public static final String LINK = "LINK";
    public static final String CITE = "CITE";


    public static final String PREFIX_FORMULA = FORMULA+"_";
    public static final String PREFIX_LINK = LINK+"_";
    public static final String PREFIX_CITE = CITE+"_";

    public static final Pattern PATTERN = Pattern.compile(
            "((?:"+PREFIX_FORMULA+"|"+PREFIX_LINK+"|"+PREFIX_CITE+")\\S+)"
    );
}
