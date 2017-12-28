package com.formulasearchengine.mathosphere.pomlp.comparison;

import com.formulasearchengine.mathosphere.pomlp.convertor.Converters;

/**
 * @author Andre Greiner-Petter
 */
public class ComparisonError {

    private Converters converter;
    private int index;
    private Exception exception;

    public ComparisonError(Converters converter, int index, Exception exception ){
        this.converter = converter;
        this.index = index;
        this.exception = exception;
    }

    public Converters getConverter() {
        return converter;
    }

    public int getIndex() {
        return index;
    }

    public Exception getException() {
        return exception;
    }

    @Override
    public String toString(){
        String stack = "";
        for ( StackTraceElement s : exception.getStackTrace() ){
            stack += s.toString() + System.lineSeparator();
        }
        return converter.name() + ":" + index + " - "
                + exception.getLocalizedMessage() + ": "
                + exception.getMessage()
                + System.lineSeparator() + stack;
    }
}
