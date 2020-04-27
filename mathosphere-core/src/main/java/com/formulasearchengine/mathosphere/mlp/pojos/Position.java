package com.formulasearchengine.mathosphere.mlp.pojos;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Andre Greiner-Petter
 */
public class Position implements Comparable<Position> {
    private int section;
    private int line;
    private int word;

    private DocumentMetaLib lib;

    public Position() {
        this(0, 0);
    }

    public Position(int section) {
        this(section, 0, 0);
    }

    public Position(int section, int line) {
        this(section, line, 0);
    }

    public Position(int section, int line, int word) {
        this.section = section;
        this.line = line;
        this.word = word;
    }

    public int getSection() {
        return section;
    }

    public int getLine() {
        return line;
    }

    public int getWord() {
        return word;
    }

    public void setSection(int section) {
        this.section = section;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public void setWord(int word) {
        this.word = word;
    }

    public void setDocumentLib(DocumentMetaLib lib) {
        this.lib = lib;
    }

    public int getSentenceDistance(Position p2) {
        if ( this.compareTo(p2) == 0 ) return 0;
        List<Position> p = new LinkedList<>();
        p.add(this);
        p.add(p2);

        p.sort(Position::compareTo);

        int startLine = p.get(0).line;
        int endLine = startLine;
        for ( int i = p.get(0).section+1; i < p.get(1).section; i++ ) {
            endLine += lib.getSectionLength(i);
        }
        endLine += p.get(1).line;
        return endLine - startLine;
    }

    @Override
    public int compareTo(Position position) {
        int d = section - position.section;
        if ( d != 0 ) return d;
        d = line - position.line;
        if ( d != 0 ) return d;
        d = word - position.word;
        return d;
    }
}
