package com.formulasearchengine.mathosphere.pomlp.gouldi;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class IdentifierExplanations {
    private String qID, explanation;

    public IdentifierExplanations(){}

    public static List<IdentifierExplanations> buildIdentifierList(ArrayList<Object> arr){
        List<IdentifierExplanations> list = new LinkedList<>();
        for ( Object obj : arr ){
            if ( obj instanceof String ) {
                IdentifierExplanations ie = new IdentifierExplanations();
                ie.setExplanation( (String)obj );
                list.add(ie);
            } else { // must be object of map<string, string>
                Map<String, String> qidMappings = (Map<String, String>)obj;
                for ( String qID : qidMappings.keySet() ){
                    IdentifierExplanations i = new IdentifierExplanations();
                    i.setqID(qID);
                    i.setExplanation(qidMappings.get(i));
                    list.add(i);
                }
            }
        }
        return list;
    }

    public String getqID() {
        return qID;
    }

    public void setqID(String qID) {
        this.qID = qID;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
