package com.formulasearchengine.mathosphere.pomlp.util;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Static class contains path constants
 */
public final class PomlpInternalPaths {

    /**
     * The base path to the latex-grammar project.
     */
    public static final Path LatexGrammarBaseDir = Paths.get( "lib", "latex-grammar" );

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
    private PomlpInternalPaths(){}
}
