package de.tuberlin.dima.schubotz.fse;

import org.w3c.dom.Document;

import java.util.Map;

public class Query {
    public String name;
    public Map<String, String> keywords;
    public Map<String, Document> formulae;

    @Override
    public String toString() {
        return name;
    }
}
