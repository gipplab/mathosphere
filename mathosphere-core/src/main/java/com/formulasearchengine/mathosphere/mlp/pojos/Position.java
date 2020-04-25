package com.formulasearchengine.mathosphere.mlp.pojos;

import com.formulasearchengine.mathosphere.mlp.text.PosTag;

/**
 * @author Andre Greiner-Petter
 */
public class Position {
    private int section;
    private int line;
    private int word;

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
}
