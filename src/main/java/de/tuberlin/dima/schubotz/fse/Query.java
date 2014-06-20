package de.tuberlin.dima.schubotz.fse;


import com.sun.org.apache.xerces.internal.dom.DocumentImpl;

import java.util.Map;

public class Query {
    public String name;
    public Map<String, String> keywords;
    public Map<String, DocumentImpl> formulae;

    @Override
    public String toString() {
        return name;
    }
}
