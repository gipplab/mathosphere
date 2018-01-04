package com.formulasearchengine.mathosphere.pomlp.gouldi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties( ignoreUnknown = true )
public class JsonGouldiBean {

    @JsonProperty("math_inputtex")
    private String mathTex;

    @JsonProperty("title")
    private String title;

    @JsonProperty("correct_tex")
    private String correctTex;

    @JsonProperty("uri")
    private String uri;

    @JsonProperty("correct_mml")
    private String mml;

    private Map<String, List<IdentifierExplanations>> definitions;

    public JsonGouldiBean(){}

    @JsonProperty("definitions")
    private void unpackNested( Map<String, Object> defs ){
        definitions = new HashMap<>();
        for ( String key : defs.keySet() ){
            ArrayList<Object> identifierList = (ArrayList<Object>)defs.get(key);

            List<IdentifierExplanations> list = IdentifierExplanations.buildIdentifierList( identifierList );
            definitions.put( key, list );

        }
    }

    public Map<String, List<IdentifierExplanations>> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(Map<String, List<IdentifierExplanations>> definitions) {
        this.definitions = definitions;
    }

    public String getOriginalTex() {
        return mathTex;
    }

    public void setMathTex(String mathTex) {
        this.mathTex = mathTex;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCorrectTex() {
        return correctTex;
    }

    public void setCorrectTex(String correctTex) {
        this.correctTex = correctTex;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getMml() {
        return mml;
    }

    public void setMml(String mml) {
        this.mml = mml;
    }
}
