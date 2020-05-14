package com.formulasearchengine.mathosphere.mlp.text;

import com.formulasearchengine.mathosphere.mlp.pojos.DocumentMetaLib;
import com.formulasearchengine.mathosphere.mlp.pojos.MathTag;
import com.formulasearchengine.mathosphere.mlp.pojos.WikiCitation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sweble.wikitext.parser.nodes.*;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Andre Greiner-Petter
 */
public class RawText {
    private static final Logger LOG = LogManager.getLogger(RawText.class.getName());

    private final static Pattern MATH_END_PATTERN = Pattern.compile("^\\s*(.*)\\s*([.,;!?]+)\\s*$");
    private static final Pattern MULTI_SPACE_PATTERN = Pattern.compile("\\s+");

    private final DocumentMetaLib metaLib;
    private final LinkedList<String> sections;

    private StringBuilder sectionBuilder;
    private StringBuilder lineBuilder;

    private boolean suppressOutput = false;
    private boolean wantSpace = false;
    private boolean pastWord = false;

    private MathTag previousMathTag;
    private String previousMathTagEnding;

    public RawText() {
        this.metaLib = new DocumentMetaLib();
        this.sections = new LinkedList<>();
    }

    public DocumentMetaLib getMetaLibrary() {
        return metaLib;
    }

    public List<String> getText() {
        return sections;
    }

    public void suppressOutput(boolean suppressOutput) {
        this.suppressOutput = suppressOutput;
    }

    public void wantSpace() {
        if (this.pastWord) {
            this.wantSpace = true;
        }
    }

    public void resetBuilder() {
        this.sectionBuilder = new StringBuilder();
        this.lineBuilder = new StringBuilder();
        this.sections.clear();
        this.wantSpace = false;
        this.pastWord = false;
    }

    /**
     * Flushes the last pending addons that are not yet in the text.
     */
    public void flush() {
        finishSection();
    }

    public void addMathTag(
            String content,
            WikiTextUtils.MathMarkUpType type
    ) {
        if ( previousMathTag != null ) {
            try {
                if ( previousMathTagEnding != null ) {
                    previousMathTag.extendContent(previousMathTagEnding+" ");
                    previousMathTagEnding = null;
                }

                content = setOrAddMathEnd(content);
                previousMathTag.extendContent(content);
            } catch (IllegalArgumentException iae) {
                LOG.warn("Unable to extend previous mathematical expression. Continue as usually.");
            }
        } else {
            content = setOrAddMathEnd(content);
            previousMathTag = new MathTag(content, type);
        }
    }

    public void addCitation(String content, String attribute) {
        WikiCitation cite = new WikiCitation(
                attribute,
                content
        );

        metaLib.addCite(cite);
        wantSpace();
        write(cite.placeholder());
    }

    private String setOrAddMathEnd(String mathExpression) {
        Matcher m = MATH_END_PATTERN.matcher(mathExpression);
        if ( m.matches() ){
            mathExpression = m.group(1);
            previousMathTagEnding = m.group(2);
        } else previousMathTagEnding = null;
        return mathExpression;
    }

    /**
     * Adds the holding math element to the current line.
     */
    private void addPreviousMath() {
        if ( previousMathTag != null ) {
            metaLib.addFormula(previousMathTag);
            lineBuilder
                    .append(" ")
                    .append(previousMathTag.placeholder())
                    .append(" ");
            previousMathTag = null;
        }

        if ( previousMathTagEnding != null ) {
            lineBuilder.append(previousMathTagEnding);
            previousMathTagEnding = null;
        }
    }

    public void write(char[] cs) {
        write(String.valueOf(cs));
    }

    public void write(char ch) {
        writeWord(String.valueOf(ch));
    }

    public void write(int num) {
        writeWord(String.valueOf(num));
    }

    public void write(String s) {
        if (suppressOutput || s.isBlank()){
            return;
        }

        if (Character.isSpaceChar(s.charAt(0))) {
            wantSpace();
        }

        String[] words = MULTI_SPACE_PATTERN.split(s);
        for (int i = 0; i < words.length; ) {
            writeWord(words[i]);
            if (++i < words.length) {
                wantSpace();
            }
        }

        final char lastChar = s.charAt(s.length() - 1);
        if (Character.isSpaceChar(lastChar) || lastChar == '\n') {
            wantSpace();
        }
    }

    private void writeWord(String s) {
        if ( s.matches("\\s*FORMULA.*") ) {
            LOG.error("Not allowed to add FORMULA here!");
        }
        addPreviousMath();

        int length = s.length();
        if (length == 0) {
            return;
        }

        if (wantSpace) {
            lineBuilder.append(' ');
        }

        lineBuilder.append(s);
        wantSpace = false;
        pastWord = true;
    }

    public void finishLine() {
        addPreviousMath();
        sectionBuilder.append(lineBuilder.toString());
        sectionBuilder.append(" ");
        lineBuilder = new StringBuilder();
    }

    public void finishSection() {
        addPreviousMath();
        sectionBuilder.append(lineBuilder.toString());
        lineBuilder = new StringBuilder();

        sections.add(sectionBuilder.toString());
        sectionBuilder.setLength(0);
    }
}
