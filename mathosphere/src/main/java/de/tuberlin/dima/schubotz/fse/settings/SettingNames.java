package de.tuberlin.dima.schubotz.fse.settings;

import java.io.Serializable;

/**
 * Enum all settings here.
 */
public enum SettingNames implements Serializable {
    /**
     * Name of algorithm class to run
     */
    ALGORITHM(""),
    /**
     * Name of input class to run
     */
    INPUT(""),
    /**
     * Filename and path to data file (raw)
     */
    DATARAW_FILE("d"),
    /**
     * Filename and path to data file (tuple csv)
     */
    DATATUPLE_FILE("e"),
    /**
     * Filename and path to query file
     */
    QUERY_FILE("q"),
    /**
     * Runtag name
     */
    RUNTAG("r"),
    /**
     * Filename and path to latex docs map file
     */
    LATEX_DOCS_MAP("l"),
    /**
     * Filename and path to keyword docs map file
     */
    KEYWORD_DOCS_MAP("k"),

    /**
     * Parellelization
     */
    NUM_SUB_TASKS("p"),

    /**
     * Output directory
     */
    OUTPUT_DIR("o"),

    /**
     * Document total
     */
    NUM_DOC("n"),
    PASSWORD("P"),
    //QUERY_SEPARATOR("qs"),
    //DOCUMENT_SEPARATOR("ds"),
    QUERYTUPLE_FILE("qt");


    private final String cmdLineOptionLetter;

    SettingNames(String letter) {
        cmdLineOptionLetter = letter;
    }

    public String getLetter() {
        return cmdLineOptionLetter;
    }
}
