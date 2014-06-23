package de.tuberlin.dima.schubotz.fse;


import java.util.Map;

public class Query {
    public String name;
    public Map<String, String> keywords;
    public Map<String, String> formulae;

    @Override
    public String toString() {
        return name;
    }
}
