package com.formulasearchengine.mathosphere.pomlp.convertor;

import java.nio.file.Path;

public enum Converters {
    POM(        "pom",          ".xml", new MathParser(),           true),
    SnuggleTeX( "snuggletex",   ".mml", new SnuggleTexConverter(),  true),
    LatexML(    "latexml",      ".mml", new LatexmlGenerator(),     true);

    private final String name;
    private final String fileEnding;
    private final ParseAndExportable parser;
    private Path subPath;
    private boolean skip;

    Converters( String name, String fileEnding, ParseAndExportable parser, boolean skip ){
        this.name = name;
        this.fileEnding = fileEnding;
        this.parser = parser;
        this.skip = skip;
    }

    public Path initSubPath( Path baseDir ){
        subPath = baseDir.resolve(name);
        return subPath;
    }

    public String fileEnding(){
        return fileEnding;
    }

    public ParseAndExportable getParser(){
        return parser;
    }

    public Path getSubPath(){
        return subPath;
    }

    public boolean skip(){
        return this.skip;
    }

    public void skipMode( boolean skip ){
        this.skip = skip;
    }
}
