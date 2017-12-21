package com.formulasearchengine.mathosphere.pomlp.util;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class Constants {

    public static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uu-LLL-d-HH:mm:ss");

    public static final String NL = System.lineSeparator();

    public static final String TAB = "  ";

    public static final String EXPR_NODE = "expression";

    public static final String TERM_NODE = "term";

    public static final String ATTR_PRIME_TAG = "prime-tag";

    public static final String ATTR_SEC_TAG = "secondary-tags";

}
