package com.formulasearchengine.mathosphere.mlp.pojos;

import com.formulasearchengine.mathosphere.mlp.text.PosTag;

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

        // if both are in the same section, just count the sentences between them
        if ( p.get(0).section == p.get(1).section ) {
            return p.get(1).sentence - p.get(0).sentence;
        }

        // otherwise, we must count the sentences until the end of the section first
        int startSection = p.get(0).section;
        int numberOfSectionsInStart = lib.getNumberOfSentencesInSection(startSection);
        int startLine = p.get(0).sentence;

        // +1 because the last line has 1 distance to next section
        int distanceToNextSection = numberOfSectionsInStart - startLine + 1;

        // now sum up sentences in between both sections.
        int inBetween = 0;
        for ( int i = p.get(0).section+1; i <= p.get(1).section-1; i++ ) {
            inBetween += lib.getNumberOfSentencesInSection(i);
        }

        // for the end, the number of the sentence is also the distance to the beginning of the section
        // sentence 3 has a distance of 3 to the beginning of the section
        int distanceToEnd = p.get(1).sentence;
        return distanceToNextSection + inBetween + distanceToEnd;
    }

    public static boolean inSameSentence(Position p1, Position p2) {
        return p1.section == p2.section && p1.sentence == p2.sentence;
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
