package com.formulasearchengine.mathosphere.mlp.pojos;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Andre Greiner-Petter
 */
public class Position implements Comparable<Position> {
    private int section;
    private int sentence;
    private int word;

    private DocumentMetaLib lib;

    public Position() {
        this(0, 0);
    }

    public Position(int section) {
        this(section, 0, 0);
    }

    public Position(int section, int sentence) {
        this(section, sentence, 0);
    }

    public Position(int section, int sentence, int word) {
        this.section = section;
        this.sentence = sentence;
        this.word = word;
    }

    public int getSection() {
        return section;
    }

    public int getSentence() {
        return sentence;
    }

    public int getWord() {
        return word;
    }

    public void setSection(int section) {
        this.section = section;
    }

    public void setSentence(int sentence) {
        this.sentence = sentence;
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

        int startLine = p.get(0).sentence;
        int endLine = startLine;
        for ( int i = p.get(0).section+1; i < p.get(1).section; i++ ) {
            endLine += lib.getSectionLength(i);
        }
        endLine += p.get(1).sentence;
        return endLine - startLine;
    }

    @Override
    public int compareTo(Position position) {
        int d = section - position.section;
        if ( d != 0 ) return d;
        d = sentence - position.sentence;
        if ( d != 0 ) return d;
        d = word - position.word;
        return d;
    }

    @Override
    public String toString(){
        return "Sec: " + section + "; Sen: " + sentence + "; Word: " + word;
    }
}
