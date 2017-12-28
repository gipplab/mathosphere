package com.formulasearchengine.mathosphere.pomlp.convertor;

import java.nio.file.Path;

public enum Converters {
    POM(        0,  "pom",          ".xml", new POMConverter()),
    SnuggleTeX( 1,  "snuggletex",   ".mml", new SnuggleTexConverter()),
    LatexML(    2,  "latexml",      ".mml", new LatexmlGenerator()),
    Mathematical(3, "mathematical", ".mml", new MathematicalRubyConverter()),
    MathToWeb(  4,  "mathtoweb",    ".mml", new MathToWebConverter());

    // just the position of this element in this enum (it's easier that way...)
    private final int position;

    // name of the converter (used for the sub directories)
    private final String name;

    // the file extension (usually only mml or xml)
    private final String fileEnding;

    // the parser class
    private final Parser parser;

    // the sub path to the directory, should be initialized first to set a base dir
    private Path subPath;

    // skip this in the generation process
    private boolean skip = false;

    // is the generated file XML or MML?
    private final boolean xmlMode;

    Converters(int pos, String name, String fileEnding, Parser parser){
        this.position = pos;
        this.name = name;
        this.fileEnding = fileEnding;
        this.parser = parser;
        this.xmlMode = fileEnding.contains("xml");
    }

    public Path initSubPath( Path baseDir ){
        subPath = baseDir.resolve(name);
        return subPath;
    }

    public int getPosition(){
        return position;
    }

    public Path getFile( int index ){
        return subPath.resolve( index + fileEnding );
    }

    public String fileEnding(){
        return fileEnding;
    }

    public Parser getParser(){
        return parser;
    }

    public Path getSubPath(){
        return subPath;
    }

    public boolean isMML(){
        return !xmlMode;
    }

    public boolean skip(){
        return this.skip;
    }

    public void setSkipMode( boolean skip ){
        this.skip = skip;
    }
}
