package com.formulasearchengine.mathosphere.pomlp.util;

import com.formulasearchengine.mathosphere.pomlp.util.config.PathBuilder;
import gov.nist.drmf.interpreter.common.GlobalPaths;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Static class contains path constants
 */
public final class PomlpPathConstants {

    /**
     * The base path to the latex-grammar project.
     */
    public static final Path LatexGrammarBaseDir = Paths
            .get("") // local path
            .toAbsolutePath()
            .getParent()
            .resolve( "lib" )
            .resolve( "latex-grammar" );

    public static final Path LatexGrammarReferenceDir = LatexGrammarBaseDir
            .resolve(
                    GlobalPaths.PATH_REFERENCE_DATA // -> reference folder
            );

    public static final Path LatexGrammarLexiconFolder = LatexGrammarBaseDir
            .resolve(
                    GlobalPaths.PATH_LEXICONS // -> lexicon folder
            );

    /**
     * The path to the resources folder
     */
    public static final Path ResourceDir =
            new PathBuilder()
                    .initResourcesPath()
                    .build();

    /**
     * Subdirectory path for the xml exports in the resources folder
     */
    public static final Path ResourceXMLExportsDir =
            new PathBuilder( ResourceDir )
                    .addSubPath("XMLExports")
                    .build();

    /**
     * Unable access to final, static class
     */
    private PomlpPathConstants(){}
}
